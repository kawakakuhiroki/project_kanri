/*
 * [役割] DependencyDao: 依存関係のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 依存関係情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.Dependency;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] DependencyDao: 依存関係のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 依存関係情報。
 * [影響] DBアクセス。
 */
public class DependencyDao {
  public List<Dependency> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM task_dependencies WHERE project_id = ?";
    List<Dependency> out = new ArrayList<>();
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
    String sql = "DELETE FROM task_dependencies WHERE project_id = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      ps.executeUpdate();
    }
  }

  public void insert(Connection con, Dependency d) throws SQLException {
    String sql =
        "INSERT INTO task_dependencies (project_id, predecessor_id, successor_id, dep_type, lag_days) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, d.getProjectId());
      ps.setLong(2, d.getPredecessorId());
      ps.setLong(3, d.getSuccessorId());
      ps.setString(4, d.getType() == null ? "FS" : d.getType());
      ps.setInt(5, d.getLagDays() == null ? 0 : d.getLagDays());
      ps.executeUpdate();
    }
  }

  private Dependency map(ResultSet rs) throws SQLException {
    Dependency d = new Dependency();
    d.setId(rs.getLong("id"));
    d.setProjectId(rs.getLong("project_id"));
    d.setPredecessorId(rs.getLong("predecessor_id"));
    d.setSuccessorId(rs.getLong("successor_id"));
    d.setType(rs.getString("dep_type"));
    d.setLagDays(rs.getInt("lag_days"));
    return d;
  }
}
