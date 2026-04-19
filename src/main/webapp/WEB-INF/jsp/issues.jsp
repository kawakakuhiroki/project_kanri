<%--
[役割] issues: 不具合管理画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] issues.js。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String projectId = request.getParameter("projectId");
  if (projectId == null) {
    projectId = "";
  }
%>
<jsp:include page="_top.jsp" />

<div class="card">
  <div class="h1">不具合管理</div>
  <div class="muted">バグ/不具合の登録と進捗管理を行います。</div>

  <div id="issuesError" class="inline-error"></div>

  <div class="issue-form">
    <div class="field">
      <label>タイトル</label>
      <input type="text" id="issueTitle" />
    </div>
    <div class="field">
      <label>詳細</label>
      <input type="text" id="issueDesc" />
    </div>
    <div class="field">
      <label>優先度</label>
      <select id="issuePriority">
        <option value="1">最高</option>
        <option value="2">高</option>
        <option value="3" selected>中</option>
        <option value="4">低</option>
        <option value="5">最低</option>
      </select>
    </div>
    <div class="field">
      <label>重要度</label>
      <select id="issueSeverity">
        <option value="CRITICAL">致命的</option>
        <option value="HIGH">高</option>
        <option value="MEDIUM" selected>中</option>
        <option value="LOW">低</option>
      </select>
    </div>
    <div class="row" style="margin-top: 12px;">
      <button id="createIssueBtn">登録</button>
    </div>
  </div>

  <div class="table-scroll" style="margin-top: 12px;">
    <table class="issues-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>タイトル</th>
          <th>ステータス</th>
          <th>優先度</th>
          <th>重要度</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody id="issuesBody"></tbody>
    </table>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project_context.js"></script>
<script src="${pageContext.request.contextPath}/static/issues.js"></script>
<jsp:include page="_bottom.jsp" />
