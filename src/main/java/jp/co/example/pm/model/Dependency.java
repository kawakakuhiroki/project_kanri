/*
 * [役割] Dependency: タスク依存関係を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

/**
 * [目的] Dependency: タスク依存関係を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class Dependency {
  private Long id;
  private Long projectId;
  private Long predecessorId;
  private Long successorId;
  private String type;
  private Integer lagDays;

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

  public Long getPredecessorId() {
    return predecessorId;
  }

  public void setPredecessorId(Long predecessorId) {
    this.predecessorId = predecessorId;
  }

  public Long getSuccessorId() {
    return successorId;
  }

  public void setSuccessorId(Long successorId) {
    this.successorId = successorId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getLagDays() {
    return lagDays;
  }

  public void setLagDays(Integer lagDays) {
    this.lagDays = lagDays;
  }
}
