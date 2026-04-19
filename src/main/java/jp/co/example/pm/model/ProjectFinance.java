/*
 * [役割] ProjectFinance: プロジェクト予実情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;

/**
 * [目的] ProjectFinance: プロジェクト予実情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class ProjectFinance {
  private Long projectId;
  private BigDecimal revenueActual;
  private BigDecimal costActual;
  private BigDecimal laborCostActual;

  public Long getProjectId() {
    return projectId;
  }

  public void setProjectId(Long projectId) {
    this.projectId = projectId;
  }

  public BigDecimal getRevenueActual() {
    return revenueActual;
  }

  public void setRevenueActual(BigDecimal revenueActual) {
    this.revenueActual = revenueActual;
  }

  public BigDecimal getCostActual() {
    return costActual;
  }

  public void setCostActual(BigDecimal costActual) {
    this.costActual = costActual;
  }

  public BigDecimal getLaborCostActual() {
    return laborCostActual;
  }

  public void setLaborCostActual(BigDecimal laborCostActual) {
    this.laborCostActual = laborCostActual;
  }
}
