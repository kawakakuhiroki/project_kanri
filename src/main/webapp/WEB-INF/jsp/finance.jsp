<%--
[役割] finance: 予実管理画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] finance.js。
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
  <div class="h1">予実管理</div>
  <div class="muted">売上/原価/工数の予実を管理します。</div>

  <div id="financeError" class="inline-error"></div>

  <div class="finance-grid">
    <div class="field">
      <label>売上予算</label>
      <input type="number" id="budgetRevenue" min="0" step="1" />
    </div>
    <div class="field">
      <label>原価予算</label>
      <input type="number" id="budgetCost" min="0" step="1" />
    </div>
    <div class="field">
      <label>売上実績</label>
      <input type="number" id="revenueActual" min="0" step="1" />
    </div>
    <div class="field">
      <label>原価実績</label>
      <input type="number" id="costActual" min="0" step="1" />
    </div>
    <div class="field">
      <label>工数原価実績</label>
      <input type="number" id="laborCostActual" min="0" step="1" />
    </div>
  </div>

  <div class="finance-summary" id="financeSummary"></div>

  <div class="row" style="margin-top: 12px;">
    <button id="saveFinanceBtn">保存</button>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/finance.js"></script>
<jsp:include page="_bottom.jsp" />
