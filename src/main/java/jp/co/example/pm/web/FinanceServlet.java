/*
 * [役割] FinanceServlet: 予実管理画面を表示する。
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
 * [目的] FinanceServlet: 予実管理画面を表示する。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward。
 * [影響] レスポンス出力。
 */
@WebServlet(name = "FinanceServlet", urlPatterns = "/finance")
public class FinanceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("pageTitle", "予実管理");
    req.setAttribute("activeTab", "finance");
    WebUtil.forward(req, resp, "finance.jsp");
  }
}
