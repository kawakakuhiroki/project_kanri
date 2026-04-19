/*
 * [役割] ApiWorkflowServlet: 承認ワークフローAPI。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] WorkflowDao。
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
import jp.co.example.pm.dao.WorkflowDao;
import jp.co.example.pm.model.WorkflowRequest;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiWorkflowServlet: 承認ワークフローAPI。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiWorkflowServlet", urlPatterns = "/api/workflow/*")
public class ApiWorkflowServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final WorkflowDao workflowDao = new WorkflowDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      List<WorkflowRequest> list = workflowDao.listByProject(projectId);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, list);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    WorkflowRequest input = WebUtil.readJson(req, WorkflowRequest.class);
    input.setProjectId(projectId);
    input.setRequesterId((Long) req.getSession().getAttribute(SessionKeys.USER_ID));
    try {
      long id = workflowDao.insert(input);
      input.setId(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, input);
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
    WorkflowRequest input = WebUtil.readJson(req, WorkflowRequest.class);
    input.setId(id);
    if (input.getApproverId() == null) {
      input.setApproverId((Long) req.getSession().getAttribute(SessionKeys.USER_ID));
    }
    try {
      WorkflowRequest existing = workflowDao.find(id);
      if (existing == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      AccessControl.requireProjectMember(req, existing.getProjectId());
      workflowDao.update(input);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, input);
    } catch (SQLException e) {
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
