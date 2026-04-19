<%--
[役割] workflow: 承認ワークフロー画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] workflow.js。
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
  <div class="h1">承認ワークフロー</div>
  <div class="muted">タスクの承認申請と進捗を管理します。</div>

  <div id="workflowError" class="inline-error"></div>

  <div class="workflow-form">
    <div class="field">
      <label>対象タスク</label>
      <select id="workflowTask"></select>
    </div>
    <div class="field">
      <label>承認者</label>
      <select id="workflowApprover"></select>
    </div>
    <div class="field" style="flex: 1; min-width: 240px;">
      <label>メッセージ</label>
      <input type="text" id="workflowMessage" placeholder="承認依頼の内容" />
    </div>
    <div class="row" style="align-items: flex-end;">
      <button id="createWorkflowBtn">申請</button>
    </div>
  </div>

  <div class="table-scroll" style="margin-top: 12px;">
    <table class="workflow-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>タスク</th>
          <th>申請者</th>
          <th>承認者</th>
          <th>ステータス</th>
          <th>メッセージ</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody id="workflowBody"></tbody>
    </table>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project_context.js"></script>
<script src="${pageContext.request.contextPath}/static/workflow.js"></script>
<jsp:include page="_bottom.jsp" />
