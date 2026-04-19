/*
 * [役割] ApiUsersServlet: ユーザー管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [依存] UserDao/WebUtil。
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
import java.util.stream.Collectors;
import jp.co.example.pm.dao.UserDao;
import jp.co.example.pm.model.User;
import jp.co.example.pm.util.HashUtil;
import jp.co.example.pm.web.AccessControl;
import jp.co.example.pm.web.SessionKeys;
import jp.co.example.pm.web.WebUtil;
import jp.co.example.pm.web.dto.UserRequest;
import jp.co.example.pm.web.dto.UserSummary;

/**
 * [目的] ApiUsersServlet: ユーザー管理API。
 * [入力] HTTPリクエスト(JSON)。
 * [出力] JSONレスポンス。
 * [影響] DBアクセス。
 */
@WebServlet(name = "ApiUsersServlet", urlPatterns = "/api/users/*")
public class ApiUsersServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserDao userDao = new UserDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      List<User> users = userDao.list();
      if (AccessControl.isAdmin(req)) {
        WebUtil.writeJson(resp, HttpServletResponse.SC_OK, users);
        return;
      }
      List<UserSummary> summaries =
          users.stream()
              .filter(User::isActive)
              .map(ApiUsersServlet::toSummary)
              .collect(Collectors.toList());
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, summaries);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    requireAdmin(req);
    UserRequest input = WebUtil.readJson(req, UserRequest.class);
    if (input.getLoginId() == null || input.getLoginId().isBlank()) {
      throw new IllegalArgumentException("ログインIDは必須です。");
    }
    if (input.getName() == null || input.getName().isBlank()) {
      throw new IllegalArgumentException("氏名は必須です。");
    }
    if (input.getPassword() == null || input.getPassword().isBlank()) {
      throw new IllegalArgumentException("パスワードは必須です。");
    }
    User user = new User();
    user.setLoginId(input.getLoginId());
    user.setName(input.getName());
    user.setEmail(input.getEmail());
    user.setRole(input.getRole() == null ? "MEMBER" : input.getRole());
    user.setActive(input.getActive() == null || input.getActive());
    user.setPasswordHash(HashUtil.sha256(input.getPassword()));
    try {
      long id = userDao.insert(user);
      User created = userDao.find(id);
      WebUtil.writeJson(resp, HttpServletResponse.SC_CREATED, created);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    requireAdmin(req);
    String path = req.getPathInfo();
    if (path == null || "/".equals(path)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    long id = parseId(path);
    UserRequest input = WebUtil.readJson(req, UserRequest.class);
    try {
      User existing = userDao.find(id);
      if (existing == null) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      existing.setLoginId(input.getLoginId() == null ? existing.getLoginId() : input.getLoginId());
      existing.setName(input.getName() == null ? existing.getName() : input.getName());
      existing.setEmail(input.getEmail());
      existing.setRole(input.getRole() == null ? existing.getRole() : input.getRole());
      existing.setActive(input.getActive() == null || input.getActive());
      if (input.getPassword() != null && !input.getPassword().isBlank()) {
        existing.setPasswordHash(HashUtil.sha256(input.getPassword()));
      }
      userDao.update(existing);
      WebUtil.writeJson(resp, HttpServletResponse.SC_OK, existing);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    requireAdmin(req);
    String path = req.getPathInfo();
    if (path == null || "/".equals(path)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    long id = parseId(path);
    try {
      userDao.delete(id);
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private long parseId(String path) {
    String trimmed = path.startsWith("/") ? path.substring(1) : path;
    try {
      return Long.parseLong(trimmed);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("不正なIDです。");
    }
  }

  private void requireAdmin(HttpServletRequest req) {
    String role = (String) req.getSession().getAttribute(SessionKeys.ROLE);
    if (!"ADMIN".equals(role)) {
      throw new SecurityException("管理者権限が必要です。");
    }
  }

  private static UserSummary toSummary(User user) {
    UserSummary s = new UserSummary();
    s.setId(user.getId());
    s.setLoginId(user.getLoginId());
    s.setName(user.getName());
    s.setRole(user.getRole());
    s.setActive(user.isActive());
    return s;
  }
}
