/*
 * [役割] ForumPostDao: フォーラム投稿のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 投稿情報。
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
import jp.co.example.pm.model.ForumPost;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ForumPostDao: フォーラム投稿のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 投稿情報。
 * [影響] DBアクセス。
 */
public class ForumPostDao {
  public List<ForumPost> listByThread(long threadId) throws SQLException {
    String sql = "SELECT * FROM forum_posts WHERE thread_id = ? ORDER BY id";
    List<ForumPost> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, threadId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(map(rs));
        }
      }
    }
    return out;
  }

  public void insert(ForumPost p) throws SQLException {
    String sql = "INSERT INTO forum_posts (thread_id, body, created_by) VALUES (?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, p.getThreadId());
      ps.setString(2, p.getBody());
      if (p.getCreatedBy() != null) {
        ps.setLong(3, p.getCreatedBy());
      } else {
        ps.setNull(3, java.sql.Types.BIGINT);
      }
      ps.executeUpdate();
    }
  }

  private ForumPost map(ResultSet rs) throws SQLException {
    ForumPost p = new ForumPost();
    p.setId(rs.getLong("id"));
    p.setThreadId(rs.getLong("thread_id"));
    p.setBody(rs.getString("body"));
    long createdBy = rs.getLong("created_by");
    if (!rs.wasNull()) {
      p.setCreatedBy(createdBy);
    }
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      p.setCreatedAt(createdAt.toLocalDateTime());
    }
    return p;
  }
}
