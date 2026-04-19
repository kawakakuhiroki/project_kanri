/*
 * [役割] ApiBaselineServlet: ベースライン保存を行う。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [依存] DAO/WebUtil。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import jp.co.example.pm.dao.ProjectDao;
import jp.co.example.pm.dao.TaskDao;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] ApiBaselineServlet: ベースライン保存を行う。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス。
 * [影響] DB更新。
 */
@WebServlet(name = "ApiBaselineServlet", urlPatterns = "/api/plan/baseline")
public class ApiBaselineServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final TaskDao taskDao = new TaskDao();
  private final ProjectDao projectDao = new ProjectDao();

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String v = req.getParameter("projectId");
    if (v == null || v.isBlank()) {
      throw new IllegalArgumentException("projectId は必須です。");
    }
    long projectId;
    try {
      projectId = Long.parseLong(v);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("projectId が不正です。");
    }

    try (Connection con = DbUtil.getConnection()) {
      con.setAutoCommit(false);
      try {
        taskDao.setBaselineForProject(con, projectId);
        projectDao.updateBaselineSetAt(con, projectId);
        con.commit();
      } catch (Exception e) {
        con.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }
}
