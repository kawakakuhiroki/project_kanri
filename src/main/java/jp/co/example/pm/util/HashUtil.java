/*
 * [役割] HashUtil: 文字列のハッシュ生成を行う。
 * [入力] 文字列。
 * [出力] ハッシュ文字列。
 * [依存] MessageDigest。
 */
package jp.co.example.pm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * [目的] HashUtil: 文字列のハッシュ生成を行う。
 * [入力] 文字列。
 * [出力] SHA-256ハッシュ。
 * [影響] 副作用なし。
 */
public final class HashUtil {
  private HashUtil() {}

  public static String sha256(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
