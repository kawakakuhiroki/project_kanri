/*
 * [役割] AccessControl: 認可チェックを行う。
 * [入力] HTTPリクエスト。
 * [出力] 認可の可否。
 * [依存] ProjectMemberDao。
 */
package jp.co.example.pm.web;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import jp.co.example.pm.dao.ProjectMemberDao;

/**
 * [目的] AccessControl: ロール/プロジェクト単位の認可チェックを行う。
 * [入力] HTTPリクエスト。
 * [出力] 例外/判定結果。
 * [影響] DBアクセス。
 */
public final class AccessControl {
  private static final ProjectMemberDao projectMemberDao = new ProjectMemberDao();

  private AccessControl() {}

  public static boolean isAdmin(HttpServletRequest req) {
    return "ADMIN".equals(req.getSession().getAttribute(SessionKeys.ROLE));
  }

  public static boolean isManager(HttpServletRequest req) {
    String role = (String) req.getSession().getAttribute(SessionKeys.ROLE);
    return "ADMIN".equals(role) || "MANAGER".equals(role);
  }

  public static long requireUserId(HttpServletRequest req) {
    Object userId = req.getSession().getAttribute(SessionKeys.USER_ID);
    if (userId == null) {
      throw new SecurityException("ログインが必要です。");
    }
    return (Long) userId;
  }

  public static void requireAdmin(HttpServletRequest req) {
    if (!isAdmin(req)) {
      throw new SecurityException("管理者権限が必要です。");
    }
  }

  public static void requireManager(HttpServletRequest req) {
    if (!isManager(req)) {
      throw new SecurityException("管理権限が必要です。");
    }
  }

  public static void requireProjectMember(HttpServletRequest req, long projectId) {
    if (isAdmin(req)) {
      return;
    }
    long userId = requireUserId(req);
    try {
      if (!projectMemberDao.isMember(projectId, userId)) {
        throw new SecurityException("プロジェクトへのアクセス権限がありません。");
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
