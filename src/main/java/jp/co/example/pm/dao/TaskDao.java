/*
 * [役割] TaskDao: タスク情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] タスク情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] TaskDao: タスク情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] タスク情報。
 * [影響] DBアクセス。
 */
public class TaskDao {
  public List<Task> listByProject(long projectId) throws SQLException {
    String sql =
        "SELECT * FROM tasks WHERE project_id = ? ORDER BY COALESCE(parent_id, 0), sort_order";
    List<Task> out = new ArrayList<>();
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

  public long insert(Connection con, Task t) throws SQLException {
    String sql =
        "INSERT INTO tasks (project_id, parent_id, sort_order, name, description, status, priority, assigned_user_id, task_type, start_date, end_date, due_date, progress, planned_hours, actual_hours, baseline_start_date, baseline_end_date, baseline_planned_hours, is_milestone) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, t.getProjectId());
      if (t.getParentId() != null) {
        ps.setLong(2, t.getParentId());
      } else {
        ps.setNull(2, Types.BIGINT);
      }
      ps.setInt(3, t.getSortOrder() == null ? 0 : t.getSortOrder());
      ps.setString(4, t.getName());
      ps.setString(5, t.getDescription());
      ps.setString(6, t.getStatus() == null ? "TODO" : t.getStatus());
      ps.setInt(7, t.getPriority() == null ? 3 : t.getPriority());
      if (t.getAssignedUserId() != null) {
        ps.setLong(8, t.getAssignedUserId());
      } else {
        ps.setNull(8, Types.BIGINT);
      }
      ps.setString(9, t.getTaskType() == null ? "TASK" : t.getTaskType());
      if (t.getStartDate() != null) {
        ps.setDate(10, Date.valueOf(t.getStartDate()));
      } else {
        ps.setNull(10, Types.DATE);
      }
      if (t.getEndDate() != null) {
        ps.setDate(11, Date.valueOf(t.getEndDate()));
      } else {
        ps.setNull(11, Types.DATE);
      }
      if (t.getDueDate() != null) {
        ps.setDate(12, Date.valueOf(t.getDueDate()));
      } else {
        ps.setNull(12, Types.DATE);
      }
      ps.setInt(13, t.getProgress() == null ? 0 : t.getProgress());
      ps.setBigDecimal(14, safeDecimal(t.getPlannedHours()));
      ps.setBigDecimal(15, safeDecimal(t.getActualHours()));
      ps.setNull(16, Types.DATE);
      ps.setNull(17, Types.DATE);
      ps.setNull(18, Types.DECIMAL);
      ps.setBoolean(19, t.isMilestone());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert task");
  }

  public void update(Connection con, Task t) throws SQLException {
    String sql =
        "UPDATE tasks SET parent_id=?, sort_order=?, name=?, description=?, status=?, priority=?, assigned_user_id=?, task_type=?, start_date=?, end_date=?, due_date=?, progress=?, planned_hours=?, actual_hours=?, is_milestone=? WHERE id=?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      if (t.getParentId() != null) {
        ps.setLong(1, t.getParentId());
      } else {
        ps.setNull(1, Types.BIGINT);
      }
      ps.setInt(2, t.getSortOrder() == null ? 0 : t.getSortOrder());
      ps.setString(3, t.getName());
      ps.setString(4, t.getDescription());
      ps.setString(5, t.getStatus() == null ? "TODO" : t.getStatus());
      ps.setInt(6, t.getPriority() == null ? 3 : t.getPriority());
      if (t.getAssignedUserId() != null) {
        ps.setLong(7, t.getAssignedUserId());
      } else {
        ps.setNull(7, Types.BIGINT);
      }
      ps.setString(8, t.getTaskType() == null ? "TASK" : t.getTaskType());
      if (t.getStartDate() != null) {
        ps.setDate(9, Date.valueOf(t.getStartDate()));
      } else {
        ps.setNull(9, Types.DATE);
      }
      if (t.getEndDate() != null) {
        ps.setDate(10, Date.valueOf(t.getEndDate()));
      } else {
        ps.setNull(10, Types.DATE);
      }
      if (t.getDueDate() != null) {
        ps.setDate(11, Date.valueOf(t.getDueDate()));
      } else {
        ps.setNull(11, Types.DATE);
      }
      ps.setInt(12, t.getProgress() == null ? 0 : t.getProgress());
      ps.setBigDecimal(13, safeDecimal(t.getPlannedHours()));
      ps.setBigDecimal(14, safeDecimal(t.getActualHours()));
      ps.setBoolean(15, t.isMilestone());
      ps.setLong(16, t.getId());
      ps.executeUpdate();
    }
  }

  public void deleteByProjectExcept(Connection con, long projectId, Collection<Long> keepIds)
      throws SQLException {
    if (keepIds == null || keepIds.isEmpty()) {
      try (PreparedStatement ps = con.prepareStatement("DELETE FROM tasks WHERE project_id = ?")) {
        ps.setLong(1, projectId);
        ps.executeUpdate();
      }
      return;
    }
    StringBuilder sb = new StringBuilder("DELETE FROM tasks WHERE project_id = ? AND id NOT IN (");
    int keepCount = keepIds.size();
    for (int i = 0; i < keepCount; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append('?');
    }
    sb.append(')');
    try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
      ps.setLong(1, projectId);
      int idx = 2;
      for (Long id : keepIds) {
        ps.setLong(idx++, id);
      }
      ps.executeUpdate();
    }
  }

  public void setBaselineForProject(Connection con, long projectId) throws SQLException {
    String sql =
        "UPDATE tasks SET baseline_start_date = start_date, baseline_end_date = end_date, baseline_planned_hours = planned_hours WHERE project_id = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      ps.executeUpdate();
    }
  }

  private Task map(ResultSet rs) throws SQLException {
    Task t = new Task();
    t.setId(rs.getLong("id"));
    t.setProjectId(rs.getLong("project_id"));
    long parent = rs.getLong("parent_id");
    if (!rs.wasNull()) {
      t.setParentId(parent);
    }
    t.setSortOrder(rs.getInt("sort_order"));
    t.setName(rs.getString("name"));
    t.setDescription(rs.getString("description"));
    t.setStatus(rs.getString("status"));
    t.setPriority(rs.getInt("priority"));
    long assignee = rs.getLong("assigned_user_id");
    if (!rs.wasNull()) {
      t.setAssignedUserId(assignee);
    }
    t.setTaskType(rs.getString("task_type"));
    Date start = rs.getDate("start_date");
    if (start != null) {
      t.setStartDate(start.toLocalDate());
    }
    Date end = rs.getDate("end_date");
    if (end != null) {
      t.setEndDate(end.toLocalDate());
    }
    Date due = rs.getDate("due_date");
    if (due != null) {
      t.setDueDate(due.toLocalDate());
    }
    t.setProgress(rs.getInt("progress"));
    t.setPlannedHours(rs.getBigDecimal("planned_hours"));
    t.setActualHours(rs.getBigDecimal("actual_hours"));
    Date baselineStart = rs.getDate("baseline_start_date");
    if (baselineStart != null) {
      t.setBaselineStartDate(baselineStart.toLocalDate());
    }
    Date baselineEnd = rs.getDate("baseline_end_date");
    if (baselineEnd != null) {
      t.setBaselineEndDate(baselineEnd.toLocalDate());
    }
    t.setBaselinePlannedHours(rs.getBigDecimal("baseline_planned_hours"));
    t.setMilestone(rs.getBoolean("is_milestone"));
    return t;
  }

  private BigDecimal safeDecimal(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
