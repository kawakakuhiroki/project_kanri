/*
 * [役割] ApiProjectsServlet: プロジェクトのCRUD API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] ProjectDao/WebUtil。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jp.co.example.pm.dao.ProjectMemberDao;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.ProjectMember;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiProjectsServlet: プロジェクトのCRUD API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiProjectsServlet", urlPatterns = "/api/projects/*")
public class ApiProjectsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final ProjectDao projectDao = new ProjectDao();
  private final ProjectMemberDao projectMemberDao = new ProjectMemberDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    try {
      if (path == null || "/".equals(path)) {
        if (AccessControl.isAdmin(req)) {
          List<Project> projects = projectDao.list();
          WebUtil.writeJson(resp, HttpServletResponse.SC_OK, projects);
          return;
        }
        long userId = AccessControl.requireUserId(req);
        List<Project> projects = projectDao.listByUser(userId);
        WebUtil.writeJson(resp, HttpServletResponse.SC_OK, projects);
        return;
      }
      long id = parseId(path);
      AccessControl.requireProjectMember(req, id);
      Project project = projectDao.find(id);
      if (project == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, project);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    AccessControl.requireManager(req);
    Project input = WebUtil.readJson(req, Project.class);
    if (input.getName() == null || input.getName().isBlank()) {
      throw new IllegalArgumentException("プロジェクト名は必須です。");
    }
    try {
      long id = projectDao.insert(input);
      Long userId = (Long) req.getSession().getAttribute(SessionKeys.USER_ID);
      String role = (String) req.getSession().getAttribute(SessionKeys.ROLE);
      if (userId != null) {
        ProjectMember member = new ProjectMember();
        member.setProjectId(id);
        member.setUserId(userId);
        member.setRole(role == null ? "MEMBER" : role);
        projectMemberDao.upsert(member);
      }
      Project created = projectDao.find(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, created);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    if (path == null || "/".equals(path)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    long id = parseId(path);
    AccessControl.requireManager(req);
    AccessControl.requireProjectMember(req, id);
    Project input = WebUtil.readJson(req, Project.class);
    input.setId(id);
    if (input.getName() == null || input.getName().isBlank()) {
      throw new IllegalArgumentException("プロジェクト名は必須です。");
    }
    try {
      projectDao.update(input);
      Project updated = projectDao.find(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, updated);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    if (path == null || "/".equals(path)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    long id = parseId(path);
    AccessControl.requireAdmin(req);
    try {
      projectDao.delete(id);
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private long parseId(String path) {
    String trimmed = path.startsWith("/") ? path.substring(1) : path;
    try {
      return Long.parseLong(trimmed);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("不正なIDです。");
    }
  }
}
