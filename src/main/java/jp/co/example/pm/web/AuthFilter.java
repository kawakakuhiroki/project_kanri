/*
 * [役割] AuthFilter: 認証チェックを行う。
 * [入力] HTTPリクエスト。
 * [出力] 認証済みのみ通過。
 * [依存] Session。
 */
package jp.co.example.pm.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * [目的] AuthFilter: 認証チェックを行う。
 * [入力] HTTPリクエスト。
 * [出力] 認証済みのみ通過。
 * [影響] リダイレクト/エラー。
 */
public class AuthFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String path = req.getRequestURI();
    String ctx = req.getContextPath();

    if (path.startsWith(ctx + "/static/") || path.endsWith("/login") || path.endsWith("/logout")) {
      chain.doFilter(request, response);
      return;
    }

    Object userId = req.getSession().getAttribute(SessionKeys.USER_ID);
    if (userId == null) {
      if (path.startsWith(ctx + "/api/")) {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      } else {
        res.sendRedirect(ctx + "/login");
      }
      return;
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}
}
