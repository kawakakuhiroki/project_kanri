/*
 * [役割] UserSummary: 公開用ユーザーサマリ。
 * [入力] User。
 * [出力] JSON。
 * [依存] なし。
 */
package jp.co.example.pm.web.dto;

/**
 * [目的] UserSummary: 公開用ユーザーサマリ。
 * [入力] User。
 * [出力] JSON。
 * [影響] 副作用なし。
 */
public class UserSummary {
  private Long id;
  private String loginId;
  private String name;
  private String role;
  private boolean active;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLoginId() {
    return loginId;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
