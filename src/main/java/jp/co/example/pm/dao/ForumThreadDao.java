/*
 * [役割] ForumThreadDao: フォーラムスレッドのDBアクセス。
 * [入力] Service/Web層。
 * [出力] スレッド情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.ForumThread;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ForumThreadDao: フォーラムスレッドのDBアクセス。
 * [入力] Service/Web層。
 * [出力] スレッド情報。
 * [影響] DBアクセス。
 */
public class ForumThreadDao {
  public List<ForumThread> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM forum_threads WHERE project_id = ? ORDER BY id DESC";
    List<ForumThread> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(map(rs));
        }
      }
    }
    return out;
  }

  public long insert(ForumThread t) throws SQLException {
    String sql = "INSERT INTO forum_threads (project_id, title, created_by) VALUES (?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, t.getProjectId());
      ps.setString(2, t.getTitle());
      if (t.getCreatedBy() != null) {
        ps.setLong(3, t.getCreatedBy());
      } else {
        ps.setNull(3, java.sql.Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert thread");
  }

  public ForumThread find(long id) throws SQLException {
    String sql = "SELECT * FROM forum_threads WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return map(rs);
        }
      }
    }
    return null;
  }

  private ForumThread map(ResultSet rs) throws SQLException {
    ForumThread t = new ForumThread();
    t.setId(rs.getLong("id"));
    t.setProjectId(rs.getLong("project_id"));
    t.setTitle(rs.getString("title"));
    long createdBy = rs.getLong("created_by");
    if (!rs.wasNull()) {
      t.setCreatedBy(createdBy);
    }
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      t.setCreatedAt(createdAt.toLocalDateTime());
    }
    return t;
  }
}
