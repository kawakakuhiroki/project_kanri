/*
 * [役割] IssueDao: 不具合情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 不具合情報。
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
import jp.co.example.pm.model.Issue;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] IssueDao: 不具合情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 不具合情報。
 * [影響] DBアクセス。
 */
public class IssueDao {
  public List<Issue> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM issues WHERE project_id = ? ORDER BY id DESC";
    List<Issue> out = new ArrayList<>();
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

  public Issue find(long id) throws SQLException {
    String sql = "SELECT * FROM issues WHERE id = ?";
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

  public long insert(Issue i) throws SQLException {
    String sql =
        "INSERT INTO issues (project_id, title, description, status, priority, severity, reporter_id, assignee_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, i.getProjectId());
      ps.setString(2, i.getTitle());
      ps.setString(3, i.getDescription());
      ps.setString(4, i.getStatus() == null ? "OPEN" : i.getStatus());
      ps.setInt(5, i.getPriority() == null ? 3 : i.getPriority());
      ps.setString(6, i.getSeverity() == null ? "MEDIUM" : i.getSeverity());
      if (i.getReporterId() != null) {
        ps.setLong(7, i.getReporterId());
      } else {
        ps.setNull(7, java.sql.Types.BIGINT);
      }
      if (i.getAssigneeId() != null) {
        ps.setLong(8, i.getAssigneeId());
      } else {
        ps.setNull(8, java.sql.Types.BIGINT);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert issue");
  }

  public void update(Issue i) throws SQLException {
    String sql =
        "UPDATE issues SET title=?, description=?, status=?, priority=?, severity=?, reporter_id=?, assignee_id=?, resolved_at=? WHERE id=?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, i.getTitle());
      ps.setString(2, i.getDescription());
      ps.setString(3, i.getStatus());
      ps.setInt(4, i.getPriority() == null ? 3 : i.getPriority());
      ps.setString(5, i.getSeverity());
      if (i.getReporterId() != null) {
        ps.setLong(6, i.getReporterId());
      } else {
        ps.setNull(6, java.sql.Types.BIGINT);
      }
      if (i.getAssigneeId() != null) {
        ps.setLong(7, i.getAssigneeId());
      } else {
        ps.setNull(7, java.sql.Types.BIGINT);
      }
      if (i.getResolvedAt() != null) {
        ps.setTimestamp(8, Timestamp.valueOf(i.getResolvedAt()));
      } else {
        ps.setNull(8, java.sql.Types.TIMESTAMP);
      }
      ps.setLong(9, i.getId());
      ps.executeUpdate();
    }
  }

  public void delete(long id) throws SQLException {
    String sql = "DELETE FROM issues WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  private Issue map(ResultSet rs) throws SQLException {
    Issue i = new Issue();
    i.setId(rs.getLong("id"));
    i.setProjectId(rs.getLong("project_id"));
    i.setTitle(rs.getString("title"));
    i.setDescription(rs.getString("description"));
    i.setStatus(rs.getString("status"));
    i.setPriority(rs.getInt("priority"));
    i.setSeverity(rs.getString("severity"));
    long reporter = rs.getLong("reporter_id");
    if (!rs.wasNull()) {
      i.setReporterId(reporter);
    }
    long assignee = rs.getLong("assignee_id");
    if (!rs.wasNull()) {
      i.setAssigneeId(assignee);
    }
    Timestamp created = rs.getTimestamp("created_at");
    if (created != null) {
      i.setCreatedAt(created.toLocalDateTime());
    }
    Timestamp updated = rs.getTimestamp("updated_at");
    if (updated != null) {
      i.setUpdatedAt(updated.toLocalDateTime());
    }
    Timestamp resolved = rs.getTimestamp("resolved_at");
    if (resolved != null) {
      i.setResolvedAt(resolved.toLocalDateTime());
    }
    return i;
  }
}
