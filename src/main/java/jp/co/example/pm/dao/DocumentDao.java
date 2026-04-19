/*
 * [役割] DocumentDao: ドキュメント情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] ドキュメント情報。
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
import jp.co.example.pm.model.DocumentFile;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] DocumentDao: ドキュメント情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] ドキュメント情報。
 * [影響] DBアクセス。
 */
public class DocumentDao {
  public List<DocumentFile> listByProject(long projectId) throws SQLException {
    String sql =
        "SELECT id, project_id, task_id, filename, content_type, uploaded_by, created_at FROM documents WHERE project_id = ? ORDER BY id DESC";
    List<DocumentFile> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(mapMeta(rs));
        }
      }
    }
    return out;
  }

  public DocumentFile find(long id) throws SQLException {
    String sql = "SELECT * FROM documents WHERE id = ?";
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

  public long insert(DocumentFile d) throws SQLException {
    String sql =
        "INSERT INTO documents (project_id, task_id, filename, content_type, data, uploaded_by) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, d.getProjectId());
      if (d.getTaskId() != null) {
        ps.setLong(2, d.getTaskId());
      } else {
        ps.setNull(2, java.sql.Types.BIGINT);
      }
      ps.setString(3, d.getFilename());
      ps.setString(4, d.getContentType());
      ps.setBytes(5, d.getData());
      if (d.getUploadedBy() != null) {
        ps.setLong(6, d.getUploadedBy());
      } else {
        ps.setNull(6, java.sql.Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert document");
  }

  public void delete(long id) throws SQLException {
    String sql = "DELETE FROM documents WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  private DocumentFile map(ResultSet rs) throws SQLException {
    DocumentFile d = mapMeta(rs);
    d.setData(rs.getBytes("data"));
    return d;
  }

  private DocumentFile mapMeta(ResultSet rs) throws SQLException {
    DocumentFile d = new DocumentFile();
    d.setId(rs.getLong("id"));
    d.setProjectId(rs.getLong("project_id"));
    long taskId = rs.getLong("task_id");
    if (!rs.wasNull()) {
      d.setTaskId(taskId);
    }
    d.setFilename(rs.getString("filename"));
    d.setContentType(rs.getString("content_type"));
    long uploaded = rs.getLong("uploaded_by");
    if (!rs.wasNull()) {
      d.setUploadedBy(uploaded);
    }
    Timestamp created = rs.getTimestamp("created_at");
    if (created != null) {
      d.setCreatedAt(created.toLocalDateTime());
    }
    return d;
  }
}
