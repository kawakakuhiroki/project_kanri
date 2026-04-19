/*
 * [役割] LogoutServlet: ログアウト処理を行う。
 * [入力] HTTPリクエスト。
 * [出力] リダイレクト。
 * [依存] Session。
 */
package jp.co.example.pm.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * [目的] LogoutServlet: ログアウト処理を行う。
 * [入力] HTTPリクエスト。
 * [出力] リダイレクト。
 * [影響] セッション破棄。
 */
@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getSession().invalidate();
    resp.sendRedirect(req.getContextPath() + "/login");
  }
}
