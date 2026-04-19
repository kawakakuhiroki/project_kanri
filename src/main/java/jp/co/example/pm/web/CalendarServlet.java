/*
 * [役割] CalendarServlet: カレンダー画面を表示する。
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
 * [目的] CalendarServlet: カレンダー画面を表示する。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward。
 * [影響] レスポンス出力。
 */
@WebServlet(name = "CalendarServlet", urlPatterns = "/calendar")
public class CalendarServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("pageTitle", "カレンダー");
    req.setAttribute("activeTab", "calendar");
    WebUtil.forward(req, resp, "calendar.jsp");
  }
}
