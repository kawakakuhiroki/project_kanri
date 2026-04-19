/*
 * [役割] ApiTasksServlet: タスク管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] TaskDao/WebUtil。
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
import jp.co.example.pm.dao.TaskDao;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiTasksServlet: タスク管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiTasksServlet", urlPatterns = "/api/tasks/*")
public class ApiTasksServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final TaskDao taskDao = new TaskDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      List<Task> tasks = taskDao.listByProject(projectId);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, tasks);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    Task input = WebUtil.readJson(req, Task.class);
    if (input.getName() == null || input.getName().isBlank()) {
      throw new IllegalArgumentException("タスク名は必須です。");
    }
    input.setProjectId(projectId);
    try (var con = jp.co.example.pm.util.DbUtil.getConnection()) {
      long id = taskDao.insert(con, input);
      input.setId(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, input);
    } catch (Exception e) {
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
    Task input = WebUtil.readJson(req, Task.class);
    input.setId(id);
    if (input.getProjectId() == null) {
      throw new IllegalArgumentException("projectId は必須です。");
    }
    AccessControl.requireProjectMember(req, input.getProjectId());
    try (var con = jp.co.example.pm.util.DbUtil.getConnection()) {
      taskDao.update(con, input);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, input);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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

  private long parseId(String path) {
    String trimmed = path.startsWith("/") ? path.substring(1) : path;
    try {
      return Long.parseLong(trimmed);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("不正なIDです。");
    }
  }
}
