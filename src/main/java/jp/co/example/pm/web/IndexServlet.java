/*
 * [役割] IndexServlet: ルートアクセスの誘導。
 * [入力] HTTPリクエスト。
 * [出力] リダイレクト。
 * [依存] Servlet API。
 */
package jp.co.example.pm.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * [目的] IndexServlet: ルートアクセスの誘導。
 * [入力] HTTPリクエスト。
 * [出力] リダイレクト。
 * [影響] レスポンス出力。
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/")
public class IndexServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String contextPath = req.getContextPath();
    String requestUri = req.getRequestURI();
    if (requestUri.equals(contextPath) || requestUri.equals(contextPath + "/")) {
      resp.sendRedirect(contextPath + "/projects");
      return;
    }

    // "/" マッピングは静的ファイル要求も受けるため、既定サーブレットへ処理を戻す。
    RequestDispatcher dispatcher = req.getServletContext().getNamedDispatcher("default");
    if (dispatcher == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    dispatcher.forward(req, resp);
  }
}
