/*
 * [役割] ApiFinanceServlet: プロジェクト予実管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] ProjectDao/ProjectFinanceDao。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.dao.ProjectFinanceDao;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.ProjectFinance;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiFinanceServlet: プロジェクト予実管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DB更新。
 */
@WebServlet(name = "ApiFinanceServlet", urlPatterns = "/api/finance")
public class ApiFinanceServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final ProjectDao projectDao = new ProjectDao();
  private final ProjectFinanceDao financeDao = new ProjectFinanceDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      Project project = projectDao.find(projectId);
      ProjectFinance finance = financeDao.find(projectId);
      if (finance == null) {
        finance = new ProjectFinance();
        finance.setProjectId(projectId);
      }
      Map<String, Object> out = new HashMap<>();
      out.put("project", project);
      out.put("finance", finance);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, out);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireManager(req);
    AccessControl.requireProjectMember(req, projectId);
    Map<?, ?> input = WebUtil.readJson(req, Map.class);
    try {
      Project project = projectDao.find(projectId);
      if (project == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      project.setBudgetRevenue(toDecimal(input.get("budgetRevenue")));
      project.setBudgetCost(toDecimal(input.get("budgetCost")));
      projectDao.update(project);

      ProjectFinance finance = new ProjectFinance();
      finance.setProjectId(projectId);
      finance.setRevenueActual(toDecimal(input.get("revenueActual")));
      finance.setCostActual(toDecimal(input.get("costActual")));
      finance.setLaborCostActual(toDecimal(input.get("laborCostActual")));
      financeDao.upsert(finance);

      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private long parseProjectId(HttpServletRequest req) {
    String v = req.getParameter("projectId");
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("projectId は必須です。");
    }
    try {
      return Long.parseLong(v);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("projectId が不正です。");
    }
  }

  private BigDecimal toDecimal(Object v) {
    if (v == null) {
      return BigDecimal.ZERO;
    }
    try {
      return new BigDecimal(String.valueOf(v));
    } catch (Exception e) {
      return BigDecimal.ZERO;
    }
  }
}
