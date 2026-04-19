/*
 * [役割] ProjectFinanceDao: プロジェクト予実のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 予実情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jp.co.example.pm.model.ProjectFinance;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ProjectFinanceDao: プロジェクト予実のDBアクセス。
 * [入力] Service/Web層。
 * [出力] 予実情報。
 * [影響] DBアクセス。
 */
public class ProjectFinanceDao {
  public ProjectFinance find(long projectId) throws SQLException {
    String sql = "SELECT * FROM project_finance WHERE project_id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, projectId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return map(rs);
        }
      }
    }
    return null;
  }

  public void upsert(ProjectFinance f) throws SQLException {
    String sql =
        "INSERT INTO project_finance (project_id, revenue_actual, cost_actual, labor_cost_actual) "
            + "VALUES (?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE revenue_actual=VALUES(revenue_actual), cost_actual=VALUES(cost_actual), labor_cost_actual=VALUES(labor_cost_actual)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, f.getProjectId());
      ps.setBigDecimal(2, safeDecimal(f.getRevenueActual()));
      ps.setBigDecimal(3, safeDecimal(f.getCostActual()));
      ps.setBigDecimal(4, safeDecimal(f.getLaborCostActual()));
      ps.executeUpdate();
    }
  }

  private ProjectFinance map(ResultSet rs) throws SQLException {
    ProjectFinance f = new ProjectFinance();
    f.setProjectId(rs.getLong("project_id"));
    f.setRevenueActual(rs.getBigDecimal("revenue_actual"));
    f.setCostActual(rs.getBigDecimal("cost_actual"));
    f.setLaborCostActual(rs.getBigDecimal("labor_cost_actual"));
    return f;
  }

  private BigDecimal safeDecimal(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
