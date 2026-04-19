/*
 * [役割] ProjectMember: プロジェクト参加者情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;

/**
 * [目的] ProjectMember: プロジェクト参加者情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class ProjectMember {
  private Long projectId;
  private Long userId;
  private String role;
  private BigDecimal hourlyCost;
  private BigDecimal allocationHours;

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public BigDecimal getHourlyCost() {
    return hourlyCost;
  }

  public void setHourlyCost(BigDecimal hourlyCost) {
    this.hourlyCost = hourlyCost;
  }

  public BigDecimal getAllocationHours() {
    return allocationHours;
  }

  public void setAllocationHours(BigDecimal allocationHours) {
    this.allocationHours = allocationHours;
  }
}
