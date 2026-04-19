/*
 * [役割] ApiDocumentsServlet: ドキュメントAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス/ファイル。
 * [依存] DocumentDao。
 */
package jp.co.example.pm.web.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import jp.co.example.pm.dao.DocumentDao;
import jp.co.example.pm.model.DocumentFile;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;

/**
 * [目的] ApiDocumentsServlet: ドキュメントAPI。
 * [入力] HTTPリクエスト。
 * [出力] JSONレスポンス/ファイル。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiDocumentsServlet", urlPatterns = "/api/docs")
@MultipartConfig
public class ApiDocumentsServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final DocumentDao documentDao = new DocumentDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String idParam = req.getParameter("id");
    try {
      if (idParam != null && !idParam.isBlank()) {
        long id = Long.parseLong(idParam);
        DocumentFile doc = documentDao.find(id);
        if (doc == null) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND);
          return;
        }
        AccessControl.requireProjectMember(req, doc.getProjectId());
        resp.setContentType(doc.getContentType());
        resp.setHeader(
            "Content-Disposition", "attachment; filename=\"" + doc.getFilename() + "\"");
        resp.getOutputStream().write(doc.getData());
        return;
      }
      long projectId = parseProjectId(req);
      AccessControl.requireProjectMember(req, projectId);
      List<DocumentFile> list = documentDao.listByProject(projectId);
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
    String taskIdParam = req.getParameter("taskId");
    Long taskId = null;
    if (taskIdParam != null && !taskIdParam.isBlank()) {
      taskId = Long.parseLong(taskIdParam);
    }
    Part file = req.getPart("file");
    if (file == null) {
      throw new IllegalArgumentException("ファイルがありません。");
    }
    byte[] data;
    try (InputStream in = file.getInputStream()) {
      data = in.readAllBytes();
    }
    DocumentFile doc = new DocumentFile();
    doc.setProjectId(projectId);
    doc.setTaskId(taskId);
    doc.setFilename(file.getSubmittedFileName());
    doc.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
    doc.setData(data);
    doc.setUploadedBy((Long) req.getSession().getAttribute(SessionKeys.USER_ID));
    try {
      long id = documentDao.insert(doc);
      doc.setId(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, doc);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String idParam = req.getParameter("id");
    if (idParam == null || idParam.isBlank()) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    try {
      long id = Long.parseLong(idParam);
      DocumentFile doc = documentDao.find(id);
      if (doc == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      AccessControl.requireProjectMember(req, doc.getProjectId());
      documentDao.delete(id);
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
}
