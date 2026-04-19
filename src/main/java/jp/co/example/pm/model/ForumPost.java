/*
 * [役割] ForumPost: フォーラムの投稿情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [依存] なし。
 */
package jp.co.example.pm.model;

import java.time.LocalDateTime;

/**
 * [目的] ForumPost: フォーラムの投稿情報を保持する。
 * [入力] DAO/Serviceで生成。
 * [出力] Web/APIへ返却。
 * [影響] 副作用なし。
 */
public class ForumPost {
  private Long id;
  private Long threadId;
  private String body;
  private Long createdBy;
  private LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getThreadId() {
    return threadId;
  }

  public void setThreadId(Long threadId) {
    this.threadId = threadId;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
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
