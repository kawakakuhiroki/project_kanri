<%--
[役割] wiki: Wiki画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] wiki.js。
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
  <div class="h1">Wiki</div>
  <div class="muted">プロジェクトのナレッジや資料をまとめます。</div>

  <div id="wikiError" class="inline-error"></div>

  <div class="wiki-layout">
    <div class="wiki-list">
      <div class="row" style="justify-content: space-between; align-items: center;">
        <div class="h2">ページ一覧</div>
        <button class="secondary" id="newWikiBtn">新規ページ</button>
      </div>
      <div id="wikiList" class="wiki-items"></div>
    </div>
    <div class="wiki-editor">
      <div class="field">
        <label>タイトル</label>
        <input type="text" id="wikiTitle" />
      </div>
      <div class="field">
        <label>本文</label>
        <textarea id="wikiContent" rows="12"></textarea>
      </div>
      <div class="row" style="justify-content: space-between; align-items: center;">
        <div id="wikiMeta" class="muted small"></div>
        <button id="saveWikiBtn">保存</button>
      </div>
    </div>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project_context.js"></script>
<script src="${pageContext.request.contextPath}/static/wiki.js"></script>
<jsp:include page="_bottom.jsp" />
