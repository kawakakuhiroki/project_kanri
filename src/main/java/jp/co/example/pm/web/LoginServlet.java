/*
 * [役割] LoginServlet: ログイン画面と認証処理を行う。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward / redirect。
 * [依存] UserDao/HashUtil。
 */
package jp.co.example.pm.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jp.co.example.pm.dao.UserDao;
import jp.co.example.pm.model.User;
import jp.co.example.pm.util.HashUtil;

/**
 * [目的] LoginServlet: ログイン画面と認証処理を行う。
 * [入力] HTTPリクエスト。
 * [出力] JSP forward / redirect。
 * [影響] セッション更新。
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final UserDao userDao = new UserDao();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("pageTitle", "ログイン");
    req.setAttribute("showGlobalHeader", false);
    WebUtil.forward(req, resp, "login.jsp");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String loginId = req.getParameter("loginId");
    String password = req.getParameter("password");
    if (loginId == null || loginId.isBlank()) {
      req.setAttribute("error", "ログインIDを入力してください。");
      doGet(req, resp);
      return;
    }
    try {
      User user = userDao.findByLoginId(loginId);
      if (user == null || !user.isActive()) {
        req.setAttribute("error", "ユーザーが存在しません。");
        doGet(req, resp);
        return;
      }
      String hashed = HashUtil.sha256(password == null ? "" : password);
      if (!hashed.equals(user.getPasswordHash())) {
        req.setAttribute("error", "パスワードが正しくありません。");
        doGet(req, resp);
        return;
      }
      req.getSession().setAttribute(SessionKeys.USER_ID, user.getId());
      req.getSession().setAttribute(SessionKeys.LOGIN_ID, user.getLoginId());
      req.getSession().setAttribute(SessionKeys.USER_NAME, user.getName());
      req.getSession().setAttribute(SessionKeys.ROLE, user.getRole());
      resp.sendRedirect(req.getContextPath() + "/projects");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
