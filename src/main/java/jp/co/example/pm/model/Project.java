/*
 * [役割] Project: プロジェクト情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [目的] Project: プロジェクト情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class Project {
  private Long id;
  private String name;
  private String code;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
  private String status;
  private BigDecimal budgetRevenue;
  private BigDecimal budgetCost;
  private Integer progress;
  private Integer taskCount;
  private String workdays;
  private BigDecimal dailyHours;
  private LocalDateTime baselineSetAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public BigDecimal getBudgetRevenue() {
    return budgetRevenue;
  }

  public void setBudgetRevenue(BigDecimal budgetRevenue) {
    this.budgetRevenue = budgetRevenue;
  }

  public BigDecimal getBudgetCost() {
    return budgetCost;
  }

  public void setBudgetCost(BigDecimal budgetCost) {
    this.budgetCost = budgetCost;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public Integer getTaskCount() {
    return taskCount;
  }

  public void setTaskCount(Integer taskCount) {
    this.taskCount = taskCount;
  }

  public String getWorkdays() {
    return workdays;
  }

  public void setWorkdays(String workdays) {
    this.workdays = workdays;
  }

  public BigDecimal getDailyHours() {
    return dailyHours;
  }

  public void setDailyHours(BigDecimal dailyHours) {
    this.dailyHours = dailyHours;
  }

  public LocalDateTime getBaselineSetAt() {
    return baselineSetAt;
  }

  public void setBaselineSetAt(LocalDateTime baselineSetAt) {
    this.baselineSetAt = baselineSetAt;
  }
}
