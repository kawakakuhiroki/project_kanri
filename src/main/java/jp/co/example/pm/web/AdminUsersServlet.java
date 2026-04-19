/*
 * [役割] AdminUsersServlet: ユーザー管理画面を表示する。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward。
 * [依存] WebUtil。
 */
package jp.co.example.pm.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * [目的] AdminUsersServlet: ユーザー管理画面を表示する。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward。
 * [影響] レスポンス出力。
 */
@WebServlet(name = "AdminUsersServlet", urlPatterns = "/admin/users")
public class AdminUsersServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String role = (String) req.getSession().getAttribute(SessionKeys.ROLE);
    if (!"ADMIN".equals(role)) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }
    req.setAttribute("pageTitle", "ユーザー管理");
    req.setAttribute("activeTab", "admin");
    WebUtil.forward(req, resp, "admin_users.jsp");
  }
}
