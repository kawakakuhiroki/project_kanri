<%--
[役割] docs: ドキュメント画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] docs.js。
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
  <div class="h1">ドキュメント管理</div>
  <div class="muted">プロジェクトに関連する資料を共有します。</div>

  <div id="docsError" class="inline-error"></div>

  <div class="row" style="margin-top: 12px;">
    <input type="file" id="docFile" />
    <input type="text" id="docTaskId" placeholder="紐付けタスクID(任意)" />
    <button id="uploadDocBtn">アップロード</button>
  </div>

  <div class="table-scroll" style="margin-top: 12px;">
    <table class="docs-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>ファイル名</th>
          <th>種別</th>
          <th>タスクID</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody id="docsBody"></tbody>
    </table>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project_context.js"></script>
<script src="${pageContext.request.contextPath}/static/docs.js"></script>
<jsp:include page="_bottom.jsp" />
