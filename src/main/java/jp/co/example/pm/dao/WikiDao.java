/*
 * [役割] WikiDao: WikiページのDBアクセス。
 * [入力] Service/Web層。
 * [出力] Wiki情報。
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
import jp.co.example.pm.model.WikiPage;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] WikiDao: WikiページのDBアクセス。
 * [入力] Service/Web層。
 * [出力] Wiki情報。
 * [影響] DBアクセス。
 */
public class WikiDao {
  public List<WikiPage> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM wiki_pages WHERE project_id = ? ORDER BY updated_at DESC";
    List<WikiPage> out = new ArrayList<>();
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

  public WikiPage find(long id) throws SQLException {
    String sql = "SELECT * FROM wiki_pages WHERE id = ?";
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

  public long upsert(WikiPage p) throws SQLException {
    if (p.getId() == null || p.getId() <= 0) {
      String sql =
          "INSERT INTO wiki_pages (project_id, title, content, version, updated_by) VALUES (?, ?, ?, ?, ?)";
      try (Connection con = DbUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        ps.setLong(1, p.getProjectId());
        ps.setString(2, p.getTitle());
        ps.setString(3, p.getContent());
        ps.setInt(4, p.getVersion() == null ? 1 : p.getVersion());
        if (p.getUpdatedBy() != null) {
          ps.setLong(5, p.getUpdatedBy());
        } else {
          ps.setNull(5, java.sql.Types.BIGINT);
        }
        ps.executeUpdate();
        try (ResultSet rs = ps.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      }
      throw new SQLException("Failed to insert wiki page");
    }
    String sql =
        "UPDATE wiki_pages SET title=?, content=?, version=?, updated_by=? WHERE id=?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, p.getTitle());
      ps.setString(2, p.getContent());
      ps.setInt(3, p.getVersion() == null ? 1 : p.getVersion());
      if (p.getUpdatedBy() != null) {
        ps.setLong(4, p.getUpdatedBy());
      } else {
        ps.setNull(4, java.sql.Types.BIGINT);
      }
      ps.setLong(5, p.getId());
      ps.executeUpdate();
      return p.getId();
    }
  }

  private WikiPage map(ResultSet rs) throws SQLException {
    WikiPage p = new WikiPage();
    p.setId(rs.getLong("id"));
    p.setProjectId(rs.getLong("project_id"));
    p.setTitle(rs.getString("title"));
    p.setContent(rs.getString("content"));
    p.setVersion(rs.getInt("version"));
    long updatedBy = rs.getLong("updated_by");
    if (!rs.wasNull()) {
      p.setUpdatedBy(updatedBy);
    }
    Timestamp updated = rs.getTimestamp("updated_at");
    if (updated != null) {
      p.setUpdatedAt(updated.toLocalDateTime());
    }
    return p;
  }
}
