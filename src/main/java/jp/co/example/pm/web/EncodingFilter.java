/*
 * [役割] EncodingFilter: 文字コードをUTF-8に固定する。
 * [入力] HTTPリクエスト。
 * [出力] 文字コード設定。
 * [依存] Servlet API。
 */
package jp.co.example.pm.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * [目的] EncodingFilter: 文字コードをUTF-8に固定する。
 * [入力] HTTPリクエスト。
 * [出力] 文字コード設定。
 * [影響] リクエスト/レスポンスに副作用。
 */
public class EncodingFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}
}
