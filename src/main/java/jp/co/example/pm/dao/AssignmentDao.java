/*
 * [役割] AssignmentDao: タスク割当のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 割当情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.TaskAssignment;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] AssignmentDao: タスク割当のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 割当情報。
 * [影響] DBアクセス。
 */
public class AssignmentDao {
  public List<TaskAssignment> listByProject(long projectId) throws SQLException {
    String sql =
        "SELECT ta.* FROM task_assignments ta "
            + "INNER JOIN tasks t ON t.id = ta.task_id "
            + "WHERE t.project_id = ?";
    List<TaskAssignment> out = new ArrayList<>();
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

  public void deleteByProject(Connection con, long projectId) throws SQLException {
    String sql =
        "DELETE ta FROM task_assignments ta "
            + "INNER JOIN tasks t ON t.id = ta.task_id "
            + "WHERE t.project_id = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      ps.executeUpdate();
    }
  }

  public void insert(Connection con, TaskAssignment a) throws SQLException {
    String sql =
        "INSERT INTO task_assignments (task_id, resource_id, allocation_hours) VALUES (?, ?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, a.getTaskId());
      ps.setLong(2, a.getResourceId());
      ps.setBigDecimal(3, safeDecimal(a.getAllocationHours()));
      ps.executeUpdate();
    }
  }

  private TaskAssignment map(ResultSet rs) throws SQLException {
    TaskAssignment a = new TaskAssignment();
    a.setId(rs.getLong("id"));
    a.setTaskId(rs.getLong("task_id"));
    a.setResourceId(rs.getLong("resource_id"));
    a.setAllocationHours(rs.getBigDecimal("allocation_hours"));
    return a;
  }

  private BigDecimal safeDecimal(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
