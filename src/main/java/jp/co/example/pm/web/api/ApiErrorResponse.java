/*
 * [役割] ApiErrorResponse: APIエラーのレスポンスDTO。
 * [入力] ApiExceptionFilterで生成される。
 * [出力] JSONとしてクライアントへ返却。
 * [依存] JSONシリアライザ。
 */
package jp.co.example.pm.web.api;

import java.util.List;

/**
 * [目的] APIエラーの構造（code/message/details）を保持する。
 * [入力] ApiExceptionFilterで生成される値。
 * [出力] JSONとして返却される。
 * [影響] 副作用なし。
 */
public class ApiErrorResponse {
  private final String code;
  private final String message;
  private final List<String> details;

  public ApiErrorResponse(String code, String message, List<String> details) {
    this.code = code;
    this.message = message;
    this.details = details == null ? List.of() : List.copyOf(details);
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public List<String> getDetails() {
    return details;
  }
}
