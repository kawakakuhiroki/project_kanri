/*
 * [役割] TaskAssignment: タスクとリソースの割当を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.math.BigDecimal;

/**
 * [目的] TaskAssignment: タスクとリソースの割当を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class TaskAssignment {
  private Long id;
  private Long taskId;
  private Long resourceId;
  private BigDecimal allocationHours;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long taskId) {
    this.taskId = taskId;
  }

  public Long getResourceId() {
    return resourceId;
  }

  public void setResourceId(Long resourceId) {
    this.resourceId = resourceId;
  }

  public BigDecimal getAllocationHours() {
    return allocationHours;
  }

  public void setAllocationHours(BigDecimal allocationHours) {
    this.allocationHours = allocationHours;
  }
}
