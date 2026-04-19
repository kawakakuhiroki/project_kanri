<%--
[役割] _header: 画面テンプレート。
[入力] request/session の属性。
[出力] HTMLとしてクライアントへ送信。
[依存] 共通JSP/JS/CSS。
--%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  String ctx = request.getContextPath();
  String pid = request.getParameter("projectId");
  String pidParam = "";
  String projectScopedFallback = ctx + "/projects";
  boolean hasProjectContext = false;
  if (pid != null && !pid.isBlank()) {
    pidParam = "?projectId=" + pid;
    hasProjectContext = true;
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
    <a class="tab ${activeTab=='kanban'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/kanban" + pidParam : projectScopedFallback %>">かんばん</a>
    <a class="tab ${activeTab=='calendar'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/calendar" + pidParam : projectScopedFallback %>">カレンダー</a>
    <a class="tab ${activeTab=='reports'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/reports" + pidParam : projectScopedFallback %>">レポート</a>
    <a class="tab ${activeTab=='finance'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/finance" + pidParam : projectScopedFallback %>">予実管理</a>
    <a class="tab ${activeTab=='forum'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/forum" + pidParam : projectScopedFallback %>">フォーラム</a>
    <a class="tab ${activeTab=='docs'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/docs" + pidParam : projectScopedFallback %>">ドキュメント</a>
    <a class="tab ${activeTab=='issues'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/issues" + pidParam : projectScopedFallback %>">不具合</a>
    <a class="tab ${activeTab=='workflow'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/workflow" + pidParam : projectScopedFallback %>">承認</a>
    <a class="tab ${activeTab=='wiki'?'active':''} <%= hasProjectContext ? "" : "tab-disabled" %>" href="<%= hasProjectContext ? ctx + "/wiki" + pidParam : projectScopedFallback %>">Wiki</a>
    <c:if test="${sessionScope.role == 'ADMIN'}">
      <a class="tab ${activeTab=='admin'?'active':''}" href="${pageContext.request.contextPath}/admin/users">管理</a>
    </c:if>
    <a class="tab" href="${pageContext.request.contextPath}/logout">ログアウト</a>
  </div>
</div>
