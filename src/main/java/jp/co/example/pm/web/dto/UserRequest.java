/*
 * [役割] UserRequest: ユーザーAPIの入力DTO。
 * [入力] JSON。
 * [出力] Serviceで使用。
 * [依存] なし。
 */
package jp.co.example.pm.web.dto;

/**
 * [目的] UserRequest: ユーザーAPIの入力DTO。
 * [入力] JSON。
 * [出力] Serviceで使用。
 * [影響] 副作用なし。
 */
public class UserRequest {
  private Long id;
  private String loginId;
  private String name;
  private String email;
  private String role;
  private String password;
  private Boolean active;

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }
}
