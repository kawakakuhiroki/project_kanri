/*
 * [役割] DbUtil: 共通ユーティリティ。
 * [入力] 各層からの呼び出し。
 * [出力] 共通処理結果を返す。
 * [依存] 設定/外部ライブラリ。
 */
package jp.co.example.pm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * [目的] DbUtil: 共通ユーティリティ。
 * [入力] 各層からの呼び出し。
 * [出力] DB接続を返す。
 * [影響] DB接続を作成する。
 */
public final class DbUtil {
  private DbUtil() {}

  static {
    // JDBCドライバを明示ロードして接続失敗を避ける。
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("MySQL JDBC Driver not found", e);
    }
  }

  // 設定値から接続を組み立て、呼び出し側の重複を避ける。
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        Config.get("db.url"), Config.get("db.user"), Config.get("db.password"));
  }
}
