/*
 * [役割] Task: タスク/WBS情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * [目的] Task: タスク/WBS情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class Task {
  private Long id;
  private Long projectId;
  private Long parentId;
  private Integer sortOrder;
  private String name;
  private String description;
  private String status;
  private Integer priority;
  private Long assignedUserId;
  private String taskType;
  private LocalDate startDate;
  private LocalDate endDate;
  private LocalDate dueDate;
  private Integer progress;
  private BigDecimal plannedHours;
  private BigDecimal actualHours;
  private LocalDate baselineStartDate;
  private LocalDate baselineEndDate;
  private BigDecimal baselinePlannedHours;
  private boolean milestone;

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

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public Integer getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(Integer sortOrder) {
    this.sortOrder = sortOrder;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Long getAssignedUserId() {
    return assignedUserId;
  }

  public void setAssignedUserId(Long assignedUserId) {
    this.assignedUserId = assignedUserId;
  }

  public String getTaskType() {
    return taskType;
  }

  public void setTaskType(String taskType) {
    this.taskType = taskType;
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

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public BigDecimal getPlannedHours() {
    return plannedHours;
  }

  public void setPlannedHours(BigDecimal plannedHours) {
    this.plannedHours = plannedHours;
  }

  public BigDecimal getActualHours() {
    return actualHours;
  }

  public void setActualHours(BigDecimal actualHours) {
    this.actualHours = actualHours;
  }

  public LocalDate getBaselineStartDate() {
    return baselineStartDate;
  }

  public void setBaselineStartDate(LocalDate baselineStartDate) {
    this.baselineStartDate = baselineStartDate;
  }

  public LocalDate getBaselineEndDate() {
    return baselineEndDate;
  }

  public void setBaselineEndDate(LocalDate baselineEndDate) {
    this.baselineEndDate = baselineEndDate;
  }

  public BigDecimal getBaselinePlannedHours() {
    return baselinePlannedHours;
  }

  public void setBaselinePlannedHours(BigDecimal baselinePlannedHours) {
    this.baselinePlannedHours = baselinePlannedHours;
  }

  public boolean isMilestone() {
    return milestone;
  }

  public void setMilestone(boolean milestone) {
    this.milestone = milestone;
  }
}
