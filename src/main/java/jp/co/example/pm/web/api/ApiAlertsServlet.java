/*
 * [役割] ApiAlertsServlet: アラート情報API。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [依存] TaskDao/ProjectDao。
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.dao.TaskDao;
import jp.co.example.pm.model.Project;
import jp.co.example.pm.model.Task;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiAlertsServlet: アラート情報API。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiAlertsServlet", urlPatterns = "/api/alerts")
public class ApiAlertsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final TaskDao taskDao = new TaskDao();
  private final ProjectDao projectDao = new ProjectDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    try {
      List<Task> tasks = taskDao.listByProject(projectId);
      Project project = projectDao.find(projectId);
      List<Map<String, Object>> alerts = new ArrayList<>();
      LocalDate today = LocalDate.now();
      for (Task t : tasks) {
        if (t.getDueDate() != null && !"DONE".equalsIgnoreCase(t.getStatus())
            && t.getDueDate().isBefore(today)) {
          Map<String, Object> a = new HashMap<>();
          a.put("type", "OVERDUE");
          a.put("message", "期日超過: " + t.getName());
          a.put("taskId", t.getId());
          alerts.add(a);
        }
      }
      if (project != null) {
        BigDecimal budget = safe(project.getBudgetCost());
        BigDecimal actual = tasks.stream()
            .map(t -> t.getActualHours() == null ? BigDecimal.ZERO : t.getActualHours())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (budget.compareTo(BigDecimal.ZERO) > 0 && actual.compareTo(budget) > 0) {
          Map<String, Object> a = new HashMap<>();
          a.put("type", "BUDGET_OVER");
          a.put("message", "工数予算超過: " + actual + "h > " + budget + "h");
          alerts.add(a);
        }
      }
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, alerts);
    } catch (Exception e) {
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

  private BigDecimal safe(BigDecimal v) {
    return v == null ? BigDecimal.ZERO : v;
  }
}
