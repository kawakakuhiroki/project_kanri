<%--
[役割] forum: フォーラム画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] forum.js。
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
  <div class="h1">フォーラム / チャット</div>
  <div class="muted">プロジェクト内で議論や相談を行います。</div>

  <div id="forumError" class="inline-error"></div>

  <div class="forum-layout">
    <div class="forum-threads">
      <div class="row" style="justify-content: space-between; align-items: center;">
        <div class="h2">スレッド</div>
        <button class="secondary" id="newThreadBtn">新規スレッド</button>
      </div>
      <div id="threadForm" class="hidden" style="margin-top: 8px;">
        <input type="text" id="threadTitle" placeholder="議題タイトル" />
        <button id="createThreadBtn">作成</button>
      </div>
      <div id="threadList" class="thread-list"></div>
    </div>
    <div class="forum-posts">
      <div class="h2" id="threadTitleLabel">投稿</div>
      <div id="postList" class="post-list"></div>
      <div class="row" style="margin-top: 8px;">
        <input type="text" id="postBody" placeholder="投稿内容" style="flex: 1;" />
        <button id="postBtn">投稿</button>
      </div>
    </div>
  </div>

  <div class="chat-panel">
    <div class="h2">チャット</div>
    <div id="chatList" class="chat-list"></div>
    <div class="row" style="margin-top: 8px;">
      <input type="text" id="chatInput" placeholder="メッセージ" style="flex: 1;" />
      <button id="chatSendBtn">送信</button>
    </div>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/forum.js"></script>
<jsp:include page="_bottom.jsp" />
