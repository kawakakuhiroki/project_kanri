/*
 * [役割] ProjectMemberDao: プロジェクト参加者のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 参加者情報。
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
import jp.co.example.pm.model.ProjectMember;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ProjectMemberDao: プロジェクト参加者のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 参加者情報。
 * [影響] DBアクセス。
 */
public class ProjectMemberDao {
  public List<ProjectMember> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM project_members WHERE project_id = ?";
    List<ProjectMember> out = new ArrayList<>();
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

  public List<Long> listProjectIdsByUser(long userId) throws SQLException {
    String sql = "SELECT project_id FROM project_members WHERE user_id = ?";
    List<Long> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(rs.getLong("project_id"));
        }
      }
    }
    return out;
  }

  public boolean isMember(long projectId, long userId) throws SQLException {
    String sql = "SELECT 1 FROM project_members WHERE project_id = ? AND user_id = ? LIMIT 1";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      ps.setLong(2, userId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private ProjectMember map(ResultSet rs) throws SQLException {
    ProjectMember m = new ProjectMember();
    m.setProjectId(rs.getLong("project_id"));
    m.setUserId(rs.getLong("user_id"));
    m.setRole(rs.getString("role"));
    m.setHourlyCost(rs.getBigDecimal("hourly_cost"));
    m.setAllocationHours(rs.getBigDecimal("allocation_hours"));
    return m;
  }

  public void upsert(ProjectMember m) throws SQLException {
    String sql =
        "INSERT INTO project_members (project_id, user_id, role, hourly_cost, allocation_hours) VALUES (?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE role=VALUES(role), hourly_cost=VALUES(hourly_cost), allocation_hours=VALUES(allocation_hours)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, m.getProjectId());
      ps.setLong(2, m.getUserId());
      ps.setString(3, m.getRole() == null ? "MEMBER" : m.getRole());
      ps.setBigDecimal(4, safeDecimal(m.getHourlyCost()));
      ps.setBigDecimal(5, safeDecimal(m.getAllocationHours()));
      ps.executeUpdate();
    }
  }

  private BigDecimal safeDecimal(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
