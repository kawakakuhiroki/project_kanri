/*
 * [役割] ApiChatServlet: チャットAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [依存] ChatDao。
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
import jp.co.example.pm.dao.ChatDao;
import jp.co.example.pm.model.ChatMessage;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiChatServlet: チャットAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiChatServlet", urlPatterns = "/api/chat")
public class ApiChatServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final ChatDao chatDao = new ChatDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    int limit = 100;
    try {
      List<ChatMessage> list = chatDao.listByProject(projectId, limit);
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
    ChatMessage input = WebUtil.readJson(req, ChatMessage.class);
    if (input.getMessage() == null || input.getMessage().isBlank()) {
      throw new IllegalArgumentException("メッセージは必須です。");
    }
    Long userId = (Long) req.getSession().getAttribute(SessionKeys.USER_ID);
    input.setProjectId(projectId);
    input.setUserId(userId);
    try {
      chatDao.insert(input);
      resp.setStatus(HttpServletResponse.SC_CREATED);
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
}
