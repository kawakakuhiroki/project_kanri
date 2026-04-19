/*
 * [役割] ApiExceptionFilter: APIの例外をJSONで返す。
 * [入力] APIリクエスト。
 * [出力] JSONエラーレスポンス。
 * [依存] WebUtil。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiExceptionFilter: APIの例外をJSONで返す。
 * [入力] APIリクエスト。
 * [出力] JSONエラーレスポンス。
 * [影響] レスポンス出力。
 */
public class ApiExceptionFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (SecurityException e) {
      WebUtil.writeJson(
          (HttpServletResponse) response,
          HttpServletResponse.SC_FORBIDDEN,
          new ApiErrorResponse("FORBIDDEN", e.getMessage(), List.of()));
    } catch (IllegalArgumentException e) {
      WebUtil.writeJson(
          (HttpServletResponse) response,
          HttpServletResponse.SC_BAD_REQUEST,
          new ApiErrorResponse("BAD_REQUEST", e.getMessage(), List.of()));
    } catch (Exception e) {
      WebUtil.writeJson(
          (HttpServletResponse) response,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          new ApiErrorResponse("INTERNAL_ERROR", "処理に失敗しました。", List.of(e.getMessage())));
    }
  }

  @Override
  public void destroy() {}
}
