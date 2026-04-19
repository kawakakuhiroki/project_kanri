/*
 * [役割] WebUtil: Web層の共通部品。
 * [入力] Web層から呼び出される。
 * [出力] 処理結果を返す/設定する。
 * [依存] Servlet API。
 */
package jp.co.example.pm.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * [目的] WebUtil: Web層の共通部品。
 * [入力] HTTPリクエスト（パラメータ/セッション）。
 * [出力] JSP forward / JSON。
 * [影響] レスポンス出力。
 */
public final class WebUtil {
  private WebUtil() {}

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static void forward(HttpServletRequest req, HttpServletResponse res, String jsp) {
    try {
      req.getRequestDispatcher("/WEB-INF/jsp/" + jsp).forward(req, res);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readJson(HttpServletRequest req, Class<T> type) throws IOException {
    try (InputStream in = req.getInputStream()) {
      return MAPPER.readValue(in, type);
    }
  }

  public static void writeJson(HttpServletResponse res, int status, Object body)
      throws IOException {
    res.setStatus(status);
    res.setContentType("application/json; charset=UTF-8");
    try (OutputStream out = res.getOutputStream()) {
      MAPPER.writeValue(out, body);
    }
  }
}
