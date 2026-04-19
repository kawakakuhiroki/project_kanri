/*
 * [役割] ApiForumServlet: フォーラムAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [依存] ForumThreadDao/ForumPostDao。
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
import jp.co.example.pm.dao.ForumPostDao;
import jp.co.example.pm.dao.ForumThreadDao;
import jp.co.example.pm.model.ForumPost;
import jp.co.example.pm.model.ForumThread;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiForumServlet: フォーラムAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiForumServlet", urlPatterns = "/api/forum/*")
public class ApiForumServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final ForumThreadDao threadDao = new ForumThreadDao();
  private final ForumPostDao postDao = new ForumPostDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    try {
      if (path != null && path.startsWith("/posts")) {
        long threadId = parseId(req.getParameter("threadId"), "threadId");
        ForumThread thread = threadDao.find(threadId);
        if (thread == null) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        AccessControl.requireProjectMember(req, thread.getProjectId());
        List<ForumPost> posts = postDao.listByThread(threadId);
        WebUtil.writeJson(resp, HttpServletResponse.SC_OK, posts);
        return;
      }
      long projectId = parseId(req.getParameter("projectId"), "projectId");
      AccessControl.requireProjectMember(req, projectId);
      List<ForumThread> threads = threadDao.listByProject(projectId);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, threads);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String path = req.getPathInfo();
    Long userId = (Long) req.getSession().getAttribute(SessionKeys.USER_ID);
    try {
      if (path != null && path.startsWith("/posts")) {
        long threadId = parseId(req.getParameter("threadId"), "threadId");
        ForumThread thread = threadDao.find(threadId);
        if (thread == null) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        AccessControl.requireProjectMember(req, thread.getProjectId());
        ForumPost post = WebUtil.readJson(req, ForumPost.class);
        post.setThreadId(threadId);
        post.setCreatedBy(userId);
        postDao.insert(post);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        return;
      }
      long projectId = parseId(req.getParameter("projectId"), "projectId");
      AccessControl.requireProjectMember(req, projectId);
      ForumThread thread = WebUtil.readJson(req, ForumThread.class);
      thread.setProjectId(projectId);
      thread.setCreatedBy(userId);
      long id = threadDao.insert(thread);
      thread.setId(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, thread);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private long parseId(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " は必須です。");
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(name + " が不正です。");
    }
  }
}
