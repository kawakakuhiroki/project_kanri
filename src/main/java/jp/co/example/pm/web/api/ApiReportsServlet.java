/*
 * [役割] ApiReportsServlet: レポート情報API。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [依存] ProjectDao/TaskDao/ProjectFinanceDao。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.dao.ProjectFinanceDao;
import jp.co.example.pm.dao.TaskDao;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.ProjectFinance;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiReportsServlet: レポート情報API。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiReportsServlet", urlPatterns = "/api/reports")
public class ApiReportsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final TaskDao taskDao = new TaskDao();
  private final ProjectDao projectDao = new ProjectDao();
  private final ProjectFinanceDao financeDao = new ProjectFinanceDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      List<Task> tasks = taskDao.listByProject(projectId);
      Project project = projectDao.find(projectId);
      ProjectFinance finance = financeDao.find(projectId);
      if (finance == null) {
        finance = new ProjectFinance();
        finance.setProjectId(projectId);
      }
      BigDecimal planned = sum(tasks, true);
      BigDecimal actual = sum(tasks, false);
      int avgProgress = tasks.isEmpty() ? 0 :
          Math.round((float) tasks.stream().mapToInt(t -> t.getProgress() == null ? 0 : t.getProgress()).sum() / tasks.size());
      int overdue = (int) tasks.stream().filter(t -> isOverdue(t)).count();

      BigDecimal budgetRevenue = project == null ? BigDecimal.ZERO : safe(project.getBudgetRevenue());
      BigDecimal budgetCost = project == null ? BigDecimal.ZERO : safe(project.getBudgetCost());
      BigDecimal revenueActual = safe(finance.getRevenueActual());
      BigDecimal costActual = safe(finance.getCostActual());
      BigDecimal laborActual = safe(finance.getLaborCostActual());
      BigDecimal totalCost = costActual.add(laborActual);
      BigDecimal profit = revenueActual.subtract(totalCost);
      BigDecimal profitRate = revenueActual.compareTo(BigDecimal.ZERO) == 0
          ? BigDecimal.ZERO
          : profit.multiply(new BigDecimal("100")).divide(revenueActual, 2, java.math.RoundingMode.HALF_UP);

      Map<String, Object> out = new HashMap<>();
      out.put("plannedHours", planned);
      out.put("actualHours", actual);
      out.put("avgProgress", avgProgress);
      out.put("overdueCount", overdue);
      out.put("budgetRevenue", budgetRevenue);
      out.put("budgetCost", budgetCost);
      out.put("revenueActual", revenueActual);
      out.put("costActual", costActual);
      out.put("laborCostActual", laborActual);
      out.put("profit", profit);
      out.put("profitRate", profitRate);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, out);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private BigDecimal sum(List<Task> tasks, boolean planned) {
    BigDecimal total = BigDecimal.ZERO;
    for (Task t : tasks) {
      BigDecimal v = planned ? t.getPlannedHours() : t.getActualHours();
      if (v != null) {
        total = total.add(v);
      }
    }
    return total;
  }

  private boolean isOverdue(Task t) {
    if (t.getDueDate() == null) {
      return false;
    }
    if ("DONE".equalsIgnoreCase(t.getStatus())) {
      return false;
    }
    return t.getDueDate().isBefore(LocalDate.now());
  }

  private BigDecimal safe(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
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
}
