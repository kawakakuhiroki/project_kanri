<%--
[役割] _header: 画面テンプレート。
[入力] request/session の属性。
[出力] HTMLとしてクライアントへ送信。
[依存] 共通JSP/JS/CSS。
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  String pid = request.getParameter("projectId");
  String pidParam = "";
  if (pid != null && !pid.isBlank()) {
    pidParam = "?projectId=" + pid;
  }
%>
<div class="nav">
  <div>
    <div class="nav-title">プロジェクト管理システム</div>
    <div class="small user-badge">
      ログインID：
      <c:choose>
        <c:when test="${not empty sessionScope.login_id}">${sessionScope.login_id}</c:when>
        <c:otherwise>guest</c:otherwise>
      </c:choose>
      　氏名：
      <c:choose>
        <c:when test="${not empty sessionScope.user_name}">${sessionScope.user_name}</c:when>
        <c:otherwise>ゲスト</c:otherwise>
      </c:choose>
    </div>
  </div>
  <div class="tabs">
    <a class="tab ${activeTab=='projects'?'active':''}" href="${pageContext.request.contextPath}/projects">プロジェクト一覧</a>
    <a class="tab ${activeTab=='kanban'?'active':''}" href="${pageContext.request.contextPath}/kanban<%= pidParam %>">かんばん</a>
    <a class="tab ${activeTab=='calendar'?'active':''}" href="${pageContext.request.contextPath}/calendar<%= pidParam %>">カレンダー</a>
    <a class="tab ${activeTab=='reports'?'active':''}" href="${pageContext.request.contextPath}/reports<%= pidParam %>">レポート</a>
    <a class="tab ${activeTab=='finance'?'active':''}" href="${pageContext.request.contextPath}/finance<%= pidParam %>">予実管理</a>
    <a class="tab ${activeTab=='forum'?'active':''}" href="${pageContext.request.contextPath}/forum<%= pidParam %>">フォーラム</a>
    <a class="tab ${activeTab=='docs'?'active':''}" href="${pageContext.request.contextPath}/docs<%= pidParam %>">ドキュメント</a>
    <a class="tab ${activeTab=='issues'?'active':''}" href="${pageContext.request.contextPath}/issues<%= pidParam %>">不具合</a>
    <a class="tab ${activeTab=='workflow'?'active':''}" href="${pageContext.request.contextPath}/workflow<%= pidParam %>">承認</a>
    <a class="tab ${activeTab=='wiki'?'active':''}" href="${pageContext.request.contextPath}/wiki<%= pidParam %>">Wiki</a>
    <c:if test="${sessionScope.role == 'ADMIN'}">
      <a class="tab ${activeTab=='admin'?'active':''}" href="${pageContext.request.contextPath}/admin/users">管理</a>
    </c:if>
    <a class="tab" href="${pageContext.request.contextPath}/logout">ログアウト</a>
  </div>
</div>
