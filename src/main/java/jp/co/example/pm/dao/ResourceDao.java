/*
 * [役割] ResourceDao: リソース情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] リソース情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jp.co.example.pm.model.Resource;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ResourceDao: リソース情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] リソース情報。
 * [影響] DBアクセス。
 */
public class ResourceDao {
  public List<Resource> listByProject(long projectId) throws SQLException {
    String sql = "SELECT * FROM resources WHERE project_id = ? ORDER BY id";
    List<Resource> out = new ArrayList<>();
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

  public long insert(Connection con, Resource r) throws SQLException {
    String sql =
        "INSERT INTO resources (project_id, name, role, daily_hours, cost_rate) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, r.getProjectId());
      ps.setString(2, r.getName());
      ps.setString(3, r.getRole());
      ps.setBigDecimal(4, safeDecimal(r.getDailyHours(), new BigDecimal("8.00")));
      ps.setBigDecimal(5, safeDecimal(r.getCostRate(), BigDecimal.ZERO));
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert resource");
  }

  public void update(Connection con, Resource r) throws SQLException {
    String sql =
        "UPDATE resources SET name=?, role=?, daily_hours=?, cost_rate=? WHERE id=? AND project_id=?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, r.getName());
      ps.setString(2, r.getRole());
      ps.setBigDecimal(3, safeDecimal(r.getDailyHours(), new BigDecimal("8.00")));
      ps.setBigDecimal(4, safeDecimal(r.getCostRate(), BigDecimal.ZERO));
      ps.setLong(5, r.getId());
      ps.setLong(6, r.getProjectId());
      ps.executeUpdate();
    }
  }

  public void deleteByProjectExcept(Connection con, long projectId, Collection<Long> keepIds)
      throws SQLException {
    if (keepIds == null || keepIds.isEmpty()) {
      try (PreparedStatement ps = con.prepareStatement("DELETE FROM resources WHERE project_id = ?")) {
        ps.setLong(1, projectId);
        ps.executeUpdate();
      }
      return;
    }
    StringBuilder sb = new StringBuilder("DELETE FROM resources WHERE project_id = ? AND id NOT IN (");
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

  private Resource map(ResultSet rs) throws SQLException {
    Resource r = new Resource();
    r.setId(rs.getLong("id"));
    r.setProjectId(rs.getLong("project_id"));
    r.setName(rs.getString("name"));
    r.setRole(rs.getString("role"));
    r.setDailyHours(rs.getBigDecimal("daily_hours"));
    r.setCostRate(rs.getBigDecimal("cost_rate"));
    return r;
  }

  private BigDecimal safeDecimal(BigDecimal v, BigDecimal defaultValue) {
    return v == null ? defaultValue : v;
  }
}
