<%--
[役割] calendar: カレンダー画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] calendar.js。
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
  <div class="row" style="justify-content: space-between; align-items: center;">
    <div>
      <div class="h1">カレンダー</div>
      <div class="muted">タスク期日と成果物をカレンダーで確認します。</div>
    </div>
    <div class="row">
      <button class="secondary" id="prevMonthBtn">前月</button>
      <div id="calendarTitle" class="badge"></div>
      <button class="secondary" id="nextMonthBtn">次月</button>
    </div>
  </div>

  <div id="calendarError" class="inline-error"></div>
  <div class="calendar-grid" id="calendarGrid"></div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project_context.js"></script>
<script src="${pageContext.request.contextPath}/static/calendar.js"></script>
<jsp:include page="_bottom.jsp" />
