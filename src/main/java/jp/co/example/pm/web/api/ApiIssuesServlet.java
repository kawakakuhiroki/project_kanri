/*
 * [役割] ApiIssuesServlet: 不具合管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] IssueDao。
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
import jp.co.example.pm.dao.IssueDao;
import jp.co.example.pm.model.Issue;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiIssuesServlet: 不具合管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiIssuesServlet", urlPatterns = "/api/issues/*")
public class ApiIssuesServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final IssueDao issueDao = new IssueDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      List<Issue> list = issueDao.listByProject(projectId);
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
    Issue input = WebUtil.readJson(req, Issue.class);
    input.setProjectId(projectId);
    if (input.getTitle() == null || input.getTitle().isBlank()) {
      throw new IllegalArgumentException("タイトルは必須です。");
    }
    input.setReporterId((Long) req.getSession().getAttribute(SessionKeys.USER_ID));
    try {
      long id = issueDao.insert(input);
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
    Issue input = WebUtil.readJson(req, Issue.class);
    input.setId(id);
    try {
      Issue existing = issueDao.find(id);
      if (existing == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      AccessControl.requireProjectMember(req, existing.getProjectId());
      issueDao.update(input);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, input);
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
    try {
      Issue existing = issueDao.find(id);
      if (existing == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      AccessControl.requireProjectMember(req, existing.getProjectId());
      issueDao.delete(id);
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
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
