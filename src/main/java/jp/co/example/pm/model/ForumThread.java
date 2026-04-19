/*
 * [役割] ForumThread: フォーラムのスレッド情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.time.LocalDateTime;

/**
 * [目的] ForumThread: フォーラムのスレッド情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class ForumThread {
  private Long id;
  private Long projectId;
  private String title;
  private Long createdBy;
  private LocalDateTime createdAt;

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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Long createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
