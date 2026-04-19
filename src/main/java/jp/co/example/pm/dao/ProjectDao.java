/*
 * [役割] ProjectDao: プロジェクト情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] プロジェクト情報。
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ProjectDao: プロジェクト情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] プロジェクト情報。
 * [影響] DBアクセス。
 */
public class ProjectDao {
  public List<Project> list() throws SQLException {
    String sql =
        "SELECT p.*, COALESCE(ROUND(AVG(t.progress)),0) AS progress, COUNT(t.id) AS task_count "
            + "FROM projects p "
            + "LEFT JOIN tasks t ON t.project_id = p.id "
            + "GROUP BY p.id "
            + "ORDER BY p.id DESC";
    List<Project> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        out.add(map(rs));
      }
    }
    return out;
  }

  public List<Project> listByUser(long userId) throws SQLException {
    String sql =
        "SELECT p.*, COALESCE(ROUND(AVG(t.progress)),0) AS progress, COUNT(t.id) AS task_count "
            + "FROM projects p "
            + "JOIN project_members pm ON pm.project_id = p.id "
            + "LEFT JOIN tasks t ON t.project_id = p.id "
            + "WHERE pm.user_id = ? "
            + "GROUP BY p.id "
            + "ORDER BY p.id DESC";
    List<Project> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(map(rs));
        }
      }
    }
    return out;
  }

  public Project find(long id) throws SQLException {
    String sql = "SELECT * FROM projects WHERE id = ?";
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

  public long insert(Project p) throws SQLException {
    String sql =
        "INSERT INTO projects (name, code, description, start_date, end_date, status, budget_revenue, budget_cost, workdays, daily_hours) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, p.getName());
      ps.setString(2, p.getCode());
      ps.setString(3, p.getDescription());
      if (p.getStartDate() != null) {
        ps.setDate(4, Date.valueOf(p.getStartDate()));
      } else {
        ps.setDate(4, null);
      }
      if (p.getEndDate() != null) {
        ps.setDate(5, Date.valueOf(p.getEndDate()));
      } else {
        ps.setDate(5, null);
      }
      ps.setString(6, p.getStatus() == null ? "ACTIVE" : p.getStatus());
      ps.setBigDecimal(7, safeDecimal(p.getBudgetRevenue(), BigDecimal.ZERO));
      ps.setBigDecimal(8, safeDecimal(p.getBudgetCost(), BigDecimal.ZERO));
      ps.setString(
          9, p.getWorkdays() == null || p.getWorkdays().isBlank() ? "1,2,3,4,5" : p.getWorkdays());
      ps.setBigDecimal(10, safeDecimal(p.getDailyHours(), new BigDecimal("8.00")));
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert project");
  }

  public void update(Project p) throws SQLException {
    String sql =
        "UPDATE projects SET name=?, code=?, description=?, start_date=?, end_date=?, status=?, budget_revenue=?, budget_cost=?, workdays=?, daily_hours=? WHERE id=?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, p.getName());
      ps.setString(2, p.getCode());
      ps.setString(3, p.getDescription());
      if (p.getStartDate() != null) {
        ps.setDate(4, Date.valueOf(p.getStartDate()));
      } else {
        ps.setDate(4, null);
      }
      if (p.getEndDate() != null) {
        ps.setDate(5, Date.valueOf(p.getEndDate()));
      } else {
        ps.setDate(5, null);
      }
      ps.setString(6, p.getStatus() == null ? "ACTIVE" : p.getStatus());
      ps.setBigDecimal(7, safeDecimal(p.getBudgetRevenue(), BigDecimal.ZERO));
      ps.setBigDecimal(8, safeDecimal(p.getBudgetCost(), BigDecimal.ZERO));
      ps.setString(
          9, p.getWorkdays() == null || p.getWorkdays().isBlank() ? "1,2,3,4,5" : p.getWorkdays());
      ps.setBigDecimal(10, safeDecimal(p.getDailyHours(), new BigDecimal("8.00")));
      ps.setLong(11, p.getId());
      ps.executeUpdate();
    }
  }

  public void updateBaselineSetAt(Connection con, long projectId) throws SQLException {
    try (PreparedStatement ps =
        con.prepareStatement("UPDATE projects SET baseline_set_at = NOW() WHERE id = ?")) {
      ps.setLong(1, projectId);
      ps.executeUpdate();
    }
  }

  public void delete(long id) throws SQLException {
    String sql = "DELETE FROM projects WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  private Project map(ResultSet rs) throws SQLException {
    Project p = new Project();
    p.setId(rs.getLong("id"));
    p.setName(rs.getString("name"));
    p.setCode(rs.getString("code"));
    p.setDescription(rs.getString("description"));
    Date start = rs.getDate("start_date");
    if (start != null) {
      p.setStartDate(start.toLocalDate());
    }
    Date end = rs.getDate("end_date");
    if (end != null) {
      p.setEndDate(end.toLocalDate());
    }
    p.setStatus(rs.getString("status"));
    p.setBudgetRevenue(rs.getBigDecimal("budget_revenue"));
    p.setBudgetCost(rs.getBigDecimal("budget_cost"));
    p.setWorkdays(rs.getString("workdays"));
    p.setDailyHours(rs.getBigDecimal("daily_hours"));
    Timestamp baselineSetAt = rs.getTimestamp("baseline_set_at");
    if (baselineSetAt != null) {
      p.setBaselineSetAt(baselineSetAt.toLocalDateTime());
    }
    try {
      p.setProgress(rs.getInt("progress"));
      p.setTaskCount(rs.getInt("task_count"));
    } catch (SQLException ignore) {
      // list以外では列がないため無視する。
    }
    return p;
  }

  private BigDecimal safeDecimal(BigDecimal v, BigDecimal defaultValue) {
    return v == null ? defaultValue : v;
  }
}
