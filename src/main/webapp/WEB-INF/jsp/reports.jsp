<%--
[役割] reports: レポート画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] reports.js。
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
  <div class="h1">レポート</div>
  <div class="muted">進捗と予実のサマリを確認します。</div>

  <div id="reportsError" class="inline-error"></div>

  <div class="report-cards" id="reportCards"></div>
  <div class="alert-list" id="alertList"></div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/reports.js"></script>
<jsp:include page="_bottom.jsp" />
