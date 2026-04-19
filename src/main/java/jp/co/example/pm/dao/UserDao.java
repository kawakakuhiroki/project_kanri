/*
 * [役割] UserDao: ユーザー情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] ユーザー情報。
 * [依存] JDBC/DbUtil。
 */
package jp.co.example.pm.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jp.co.example.pm.model.User;
import jp.co.example.pm.util.DbUtil;

/**
 * [目的] UserDao: ユーザー情報のDBアクセス。
 * [入力] Service/Web層。
 * [出力] ユーザー情報。
 * [影響] DBアクセス。
 */
public class UserDao {
  public List<User> list() throws SQLException {
    String sql = "SELECT * FROM users ORDER BY id";
    List<User> out = new ArrayList<>();
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        out.add(map(rs));
      }
    }
    return out;
  }

  public User find(long id) throws SQLException {
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return map(rs);
        }
      }
    }
    return null;
  }

  public User findByLoginId(String loginId) throws SQLException {
    String sql = "SELECT * FROM users WHERE login_id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, loginId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return map(rs);
        }
      }
    }
    return null;
  }

  public long insert(User u) throws SQLException {
    String sql =
        "INSERT INTO users (login_id, name, email, password_hash, role, active) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, u.getLoginId());
      ps.setString(2, u.getName());
      ps.setString(3, u.getEmail());
      ps.setString(4, u.getPasswordHash());
      ps.setString(5, u.getRole() == null ? "MEMBER" : u.getRole());
      ps.setBoolean(6, u.isActive());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    throw new SQLException("Failed to insert user");
  }

  public void update(User u) throws SQLException {
    String sql =
        "UPDATE users SET login_id=?, name=?, email=?, password_hash=?, role=?, active=? WHERE id=?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, u.getLoginId());
      ps.setString(2, u.getName());
      ps.setString(3, u.getEmail());
      ps.setString(4, u.getPasswordHash());
      ps.setString(5, u.getRole());
      ps.setBoolean(6, u.isActive());
      ps.setLong(7, u.getId());
      ps.executeUpdate();
    }
  }

  public void delete(long id) throws SQLException {
    String sql = "DELETE FROM users WHERE id = ?";
    try (Connection con = DbUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  private User map(ResultSet rs) throws SQLException {
    User u = new User();
    u.setId(rs.getLong("id"));
    u.setLoginId(rs.getString("login_id"));
    u.setName(rs.getString("name"));
    u.setEmail(rs.getString("email"));
    u.setPasswordHash(rs.getString("password_hash"));
    u.setRole(rs.getString("role"));
    u.setActive(rs.getBoolean("active"));
    return u;
  }
}
