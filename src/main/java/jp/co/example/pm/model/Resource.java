/*
 * [役割] Resource: リソース情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;

/**
 * [目的] Resource: リソース情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class Resource {
  private Long id;
  private Long projectId;
  private String name;
  private String role;
  private BigDecimal dailyHours;
  private BigDecimal costRate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
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

  public BigDecimal getDailyHours() {
    return dailyHours;
  }

  public void setDailyHours(BigDecimal dailyHours) {
    this.dailyHours = dailyHours;
  }

  public BigDecimal getCostRate() {
    return costRate;
  }

  public void setCostRate(BigDecimal costRate) {
    this.costRate = costRate;
  }
}
