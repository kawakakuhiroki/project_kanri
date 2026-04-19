/*
 * [役割] Config: 共通ユーティリティ。
 * [入力] 各層からの呼び出し。
 * [出力] 共通処理結果を返す。
 * [依存] 設定/外部ライブラリ。
 */
package jp.co.example.pm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * [目的] Config: 共通ユーティリティ。
 * [入力] 各層からの呼び出し。
 * [出力] 設定値を返す。
 * [影響] 基本的に副作用なし。
 */
public final class Config {
  private static final Properties P = new Properties();

  static {
    try (InputStream in = Config.class.getClassLoader().getResourceAsStream("app.properties")) {
      if (in == null) {
        throw new IllegalStateException("app.properties not found");
      }
      P.load(in);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Config() {}

  // 設定値はシステムプロパティ→環境変数→設定ファイルの順に解決する。
  public static String get(String k) {
    String v = System.getProperty(k);
    if (v != null && !v.isBlank()) {
      return v;
    }
    String env = System.getenv(k.replace('.', '_').toUpperCase());
    if (env != null && !env.isBlank()) {
      return env;
    }
    return P.getProperty(k);
  }
}
