/*
 * [役割] SessionKeys: セッションキー定数を保持する。
 * [入力] Web層。
 * [出力] 文字列定数。
 * [依存] なし。
 */
package jp.co.example.pm.web;

/**
 * [目的] SessionKeys: セッションキー定数を保持する。
 * [入力] Web層。
 * [出力] 文字列定数。
 * [影響] 副作用なし。
 */
public final class SessionKeys {
  private SessionKeys() {}

  public static final String USER_ID = "user_id";
  public static final String LOGIN_ID = "login_id";
  public static final String USER_NAME = "user_name";
  public static final String ROLE = "role";
}
