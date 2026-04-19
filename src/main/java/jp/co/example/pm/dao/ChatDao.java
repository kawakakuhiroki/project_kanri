/*
 * [役割] ChatDao: チャット情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] チャット情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.ChatMessage;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ChatDao: チャット情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] チャット情報。
 * [影響] DBアクセス。
 */
public class ChatDao {
  public List<ChatMessage> listByProject(long projectId, int limit) throws SQLException {
    String sql =
        "SELECT * FROM chat_messages WHERE project_id = ? ORDER BY id DESC LIMIT ?";
    List<ChatMessage> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      ps.setInt(2, limit);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(map(rs));
        }
      }
    }
    return out;
  }

  public void insert(ChatMessage m) throws SQLException {
    String sql = "INSERT INTO chat_messages (project_id, user_id, message) VALUES (?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, m.getProjectId());
      if (m.getUserId() != null) {
        ps.setLong(2, m.getUserId());
      } else {
        ps.setNull(2, java.sql.Types.BIGINT);
      }
      ps.setString(3, m.getMessage());
      ps.executeUpdate();
    }
  }

  private ChatMessage map(ResultSet rs) throws SQLException {
    ChatMessage m = new ChatMessage();
    m.setId(rs.getLong("id"));
    m.setProjectId(rs.getLong("project_id"));
    long userId = rs.getLong("user_id");
    if (!rs.wasNull()) {
      m.setUserId(userId);
    }
    m.setMessage(rs.getString("message"));
    Timestamp created = rs.getTimestamp("created_at");
    if (created != null) {
      m.setCreatedAt(created.toLocalDateTime());
    }
    return m;
  }
}
