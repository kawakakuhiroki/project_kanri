/*
 * [役割] ApiPlanServlet: プロジェクト計画の取得/保存。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] DAO/WebUtil。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jp.co.example.pm.dao.AssignmentDao;
import jp.co.example.pm.dao.DependencyDao;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.dao.ResourceDao;
import jp.co.example.pm.dao.TaskDao;
import jp.co.example.pm.model.Dependency;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.Resource;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.model.TaskAssignment;
import jp.co.example.pm.util.DbUtil;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.WebUtil;
import jp.co.example.pm.web.dto.ProjectPlan;

/**
 * [目的] ApiPlanServlet: プロジェクト計画の取得/保存。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiPlanServlet", urlPatterns = "/api/plan")
public class ApiPlanServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final ProjectDao projectDao = new ProjectDao();
  private final TaskDao taskDao = new TaskDao();
  private final DependencyDao dependencyDao = new DependencyDao();
  private final ResourceDao resourceDao = new ResourceDao();
  private final AssignmentDao assignmentDao = new AssignmentDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      Project project = projectDao.find(projectId);
      if (project == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      List<Task> tasks = taskDao.listByProject(projectId);
      List<Dependency> deps = dependencyDao.listByProject(projectId);
      List<Resource> resources = resourceDao.listByProject(projectId);
      List<TaskAssignment> assignments = assignmentDao.listByProject(projectId);
      ProjectPlan plan = new ProjectPlan();
      plan.setProject(project);
      plan.setTasks(tasks);
      plan.setDependencies(deps);
      plan.setResources(resources);
      plan.setAssignments(assignments);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, plan);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    ProjectPlan plan = WebUtil.readJson(req, ProjectPlan.class);
    if (plan == null) {
      throw new IllegalArgumentException("計画データがありません。");
    }
    Project project = plan.getProject();
    List<Task> tasks = plan.getTasks();
    List<Dependency> deps = plan.getDependencies();
    List<Resource> resources = plan.getResources();
    List<TaskAssignment> assignments = plan.getAssignments();

    try (Connection con = DbUtil.getConnection()) {
      con.setAutoCommit(false);
      try {
        if (project != null) {
          project.setId(projectId);
          if (project.getName() == null || project.getName().isBlank()) {
            throw new IllegalArgumentException("プロジェクト名は必須です。");
          }
          projectDao.update(project);
        }

        Map<Long, Long> idMap = new HashMap<>();
        if (tasks != null) {
          for (Task t : tasks) {
            t.setProjectId(projectId);
            if (t.getId() == null || t.getId() <= 0) {
              Long tempId = t.getId() == null ? 0L : t.getId();
              t.setParentId(resolveParentId(t.getParentId(), idMap));
              long newId = taskDao.insert(con, t);
              idMap.put(tempId, newId);
              t.setId(newId);
            }
          }
          for (Task t : tasks) {
            t.setProjectId(projectId);
            t.setParentId(resolveParentId(t.getParentId(), idMap));
            if (t.getId() != null && t.getId() > 0) {
              taskDao.update(con, t);
            }
          }
        }

        Set<Long> keepIds = new HashSet<>();
        if (tasks != null) {
          for (Task t : tasks) {
            if (t.getId() != null && t.getId() > 0) {
              keepIds.add(t.getId());
            }
          }
        }
        taskDao.deleteByProjectExcept(con, projectId, keepIds);

        Map<Long, Long> resourceIdMap = new HashMap<>();
        if (resources != null) {
          for (Resource r : resources) {
            r.setProjectId(projectId);
            if (r.getName() == null || r.getName().isBlank()) {
              throw new IllegalArgumentException("リソース名は必須です。");
            }
            if (r.getId() == null || r.getId() <= 0) {
              Long tempId = r.getId() == null ? 0L : r.getId();
              long newId = resourceDao.insert(con, r);
              resourceIdMap.put(tempId, newId);
              r.setId(newId);
            }
          }
          for (Resource r : resources) {
            r.setProjectId(projectId);
            if (r.getId() != null && r.getId() > 0) {
              resourceDao.update(con, r);
            }
          }
        }
        Set<Long> keepResourceIds = new HashSet<>();
        if (resources != null) {
          for (Resource r : resources) {
            if (r.getId() != null && r.getId() > 0) {
              keepResourceIds.add(r.getId());
            }
          }
        }
        resourceDao.deleteByProjectExcept(con, projectId, keepResourceIds);

        dependencyDao.deleteByProject(con, projectId);
        if (deps != null) {
          for (Dependency d : deps) {
            d.setProjectId(projectId);
            d.setPredecessorId(resolveId(d.getPredecessorId(), idMap));
            d.setSuccessorId(resolveId(d.getSuccessorId(), idMap));
            if (d.getPredecessorId() == null || d.getSuccessorId() == null) {
              continue;
            }
            dependencyDao.insert(con, d);
          }
        }

        assignmentDao.deleteByProject(con, projectId);
        if (assignments != null) {
          for (TaskAssignment a : assignments) {
            a.setTaskId(resolveId(a.getTaskId(), idMap));
            a.setResourceId(resolveId(a.getResourceId(), resourceIdMap));
            if (a.getTaskId() == null || a.getResourceId() == null) {
              continue;
            }
            assignmentDao.insert(con, a);
          }
        }

        con.commit();
      } catch (Exception e) {
        con.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  private long parseProjectId(HttpServletRequest req) {
    String v = req.getParameter("projectId");
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("projectId は必須です。");
    }
    try {
      return Long.parseLong(v);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("projectId が不正です。");
    }
  }

  private Long resolveParentId(Long parentId, Map<Long, Long> idMap) {
    if (parentId == null) {
      return null;
    }
    if (parentId <= 0) {
      return idMap.get(parentId);
    }
    return parentId;
  }

  private Long resolveId(Long id, Map<Long, Long> idMap) {
    if (id == null) {
      return null;
    }
    if (id <= 0) {
      return idMap.get(id);
    }
    return id;
  }
}
