/*
 * [役割] ApiWikiServlet: Wiki管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] WikiDao。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jp.co.example.pm.dao.WikiDao;
import jp.co.example.pm.model.WikiPage;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiWikiServlet: Wiki管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiWikiServlet", urlPatterns = "/api/wiki/*")
public class ApiWikiServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final WikiDao wikiDao = new WikiDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String idParam = req.getParameter("id");
    try {
      if (idParam != null && !idParam.isBlank()) {
        WikiPage page = wikiDao.find(Long.parseLong(idParam));
        if (page == null) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        AccessControl.requireProjectMember(req, page.getProjectId());
        WebUtil.writeJson(resp, HttpServletResponse.SC_OK, page);
        return;
      }
      long projectId = parseProjectId(req);
      AccessControl.requireProjectMember(req, projectId);
      List<WikiPage> list = wikiDao.listByProject(projectId);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, list);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long projectId = parseProjectId(req);
    AccessControl.requireProjectMember(req, projectId);
    WikiPage input = WebUtil.readJson(req, WikiPage.class);
    input.setProjectId(projectId);
    input.setUpdatedBy((Long) req.getSession().getAttribute(SessionKeys.USER_ID));
    if (input.getTitle() == null || input.getTitle().isBlank()) {
      throw new IllegalArgumentException("タイトルは必須です。");
    }
    if (input.getContent() == null) {
      input.setContent("");
    }
    if (input.getVersion() == null) {
      input.setVersion(1);
    } else {
      input.setVersion(input.getVersion() + 1);
    }
    try {
      long id = wikiDao.upsert(input);
      input.setId(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, input);
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
}
