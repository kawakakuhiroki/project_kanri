<%--
[役割] projects: プロジェクト一覧画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] projects.js。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="_top.jsp" />

<div class="card">
  <div class="row" style="justify-content: space-between; align-items: center;">
    <div>
      <div class="h1">プロジェクト一覧</div>
      <div class="muted">ガントチャート/工数計画を管理します。</div>
    </div>
    <button id="newProjectBtn">新規プロジェクト</button>
  </div>

  <div id="projectForm" class="project-form hidden">
    <div class="row">
      <div class="field">
        <label>プロジェクト名</label>
        <input type="text" id="projectName" placeholder="例: 新商品開発" />
      </div>
      <div class="field">
        <label>コード</label>
        <input type="text" id="projectCode" placeholder="例: PJ-001" />
      </div>
      <div class="field">
        <label>開始日</label>
        <input type="date" id="projectStart" />
      </div>
      <div class="field">
        <label>終了日</label>
        <input type="date" id="projectEnd" />
      </div>
      <div class="field" style="min-width: 280px;">
        <label>概要</label>
        <input type="text" id="projectDesc" placeholder="目的や概要" />
      </div>
    </div>
    <div class="row" style="margin-top: 12px;">
      <button id="createProjectBtn">作成</button>
      <button class="secondary" id="cancelProjectBtn">キャンセル</button>
      <div id="projectError" class="inline-error"></div>
    </div>
  </div>

  <div class="table-scroll" style="margin-top: 18px;">
    <table class="projects-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>コード</th>
        <th>プロジェクト名</th>
        <th>開始</th>
        <th>終了</th>
        <th>ステータス</th>
        <th>進捗</th>
        <th>タスク数</th>
        <th></th>
      </tr>
      </thead>
      <tbody id="projectsBody"></tbody>
    </table>
  </div>
</div>

<script src="${pageContext.request.contextPath}/static/projects.js"></script>
<jsp:include page="_bottom.jsp" />
