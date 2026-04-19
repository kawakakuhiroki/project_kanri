/*
 * [役割] WorkflowDao: 承認ワークフローのDBアクセス。
 * [入力] Service/Web層。
 * [出力] 承認情報。
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
import jp.co.example.pm.model.WorkflowRequest;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] WorkflowDao: 承認ワークフローのDBアクセス。
 * [入力] Service/Web層。
 * [出力] 承認情報。
 * [影響] DBアクセス。
 */
public class WorkflowDao {
  public List<WorkflowRequest> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM workflow_requests WHERE project_id = ? ORDER BY id DESC";
    List<WorkflowRequest> out = new ArrayList<>();
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

  public WorkflowRequest find(long id) throws SQLException {
    String sql = "SELECT * FROM workflow_requests WHERE id = ?";
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

  public long insert(WorkflowRequest r) throws SQLException {
    String sql =
        "INSERT INTO workflow_requests (project_id, task_id, requester_id, approver_id, status, message) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, r.getProjectId());
      if (r.getTaskId() != null) {
        ps.setLong(2, r.getTaskId());
      } else {
        ps.setNull(2, java.sql.Types.BIGINT);
      }
      if (r.getRequesterId() != null) {
        ps.setLong(3, r.getRequesterId());
      } else {
        ps.setNull(3, java.sql.Types.BIGINT);
      }
      if (r.getApproverId() != null) {
        ps.setLong(4, r.getApproverId());
      } else {
        ps.setNull(4, java.sql.Types.BIGINT);
      }
      ps.setString(5, r.getStatus() == null ? "PENDING" : r.getStatus());
      ps.setString(6, r.getMessage());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert workflow request");
  }

  public void update(WorkflowRequest r) throws SQLException {
    String sql =
        "UPDATE workflow_requests SET task_id=?, requester_id=?, approver_id=?, status=?, message=? WHERE id=?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      if (r.getTaskId() != null) {
        ps.setLong(1, r.getTaskId());
      } else {
        ps.setNull(1, java.sql.Types.BIGINT);
      }
      if (r.getRequesterId() != null) {
        ps.setLong(2, r.getRequesterId());
      } else {
        ps.setNull(2, java.sql.Types.BIGINT);
      }
      if (r.getApproverId() != null) {
        ps.setLong(3, r.getApproverId());
      } else {
        ps.setNull(3, java.sql.Types.BIGINT);
      }
      ps.setString(4, r.getStatus());
      ps.setString(5, r.getMessage());
      ps.setLong(6, r.getId());
      ps.executeUpdate();
    }
  }

  private WorkflowRequest map(ResultSet rs) throws SQLException {
    WorkflowRequest r = new WorkflowRequest();
    r.setId(rs.getLong("id"));
    r.setProjectId(rs.getLong("project_id"));
    long taskId = rs.getLong("task_id");
    if (!rs.wasNull()) {
      r.setTaskId(taskId);
    }
    long requester = rs.getLong("requester_id");
    if (!rs.wasNull()) {
      r.setRequesterId(requester);
    }
    long approver = rs.getLong("approver_id");
    if (!rs.wasNull()) {
      r.setApproverId(approver);
    }
    r.setStatus(rs.getString("status"));
    r.setMessage(rs.getString("message"));
    Timestamp created = rs.getTimestamp("created_at");
    if (created != null) {
      r.setCreatedAt(created.toLocalDateTime());
    }
    Timestamp updated = rs.getTimestamp("updated_at");
    if (updated != null) {
      r.setUpdatedAt(updated.toLocalDateTime());
    }
    return r;
  }
}
