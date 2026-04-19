/*
 * [役割] IssuesServlet: 不具合管理画面を表示する。
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
 * [目的] IssuesServlet: 不具合管理画面を表示する。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward。
 * [影響] レスポンス出力。
 */
@WebServlet(name = "IssuesServlet", urlPatterns = "/issues")
public class IssuesServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("pageTitle", "不具合管理");
    req.setAttribute("activeTab", "issues");
    WebUtil.forward(req, resp, "issues.jsp");
  }
}
