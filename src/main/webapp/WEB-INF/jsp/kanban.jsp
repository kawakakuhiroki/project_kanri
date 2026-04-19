<%--
[役割] kanban: かんばん画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] kanban.js。
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
      <div class="h1">かんばん</div>
      <div class="muted">ステータス別にタスクを管理します。</div>
    </div>
    <div class="row">
      <button id="addKanbanTaskBtn">タスク追加</button>
    </div>
  </div>

  <div id="kanbanError" class="inline-error"></div>

  <div id="kanbanForm" class="kanban-form hidden">
    <div class="field">
      <label>タスク名</label>
      <input type="text" id="kanbanTaskName" />
    </div>
    <div class="field">
      <label>期日</label>
      <input type="date" id="kanbanTaskDue" />
    </div>
    <div class="field">
      <label>優先度</label>
      <select id="kanbanTaskPriority">
        <option value="1">最高</option>
        <option value="2">高</option>
        <option value="3" selected>中</option>
        <option value="4">低</option>
        <option value="5">最低</option>
      </select>
    </div>
    <div class="field">
      <label>担当</label>
      <select id="kanbanTaskAssignee"></select>
    </div>
    <div class="row" style="align-items: flex-end;">
      <button id="createKanbanTaskBtn">作成</button>
      <button class="secondary" id="cancelKanbanTaskBtn">キャンセル</button>
    </div>
  </div>

  <div class="kanban-board" id="kanbanBoard"></div>

  <div class="kanban-detail" id="kanbanDetail">
    <div class="h2">タスク詳細</div>
    <div id="kanbanDetailEmpty" class="muted">カードを選択してください。</div>
    <div id="kanbanDetailForm" class="hidden">
      <div class="row">
        <div class="field" style="min-width: 240px;">
          <label>タスク名</label>
          <input type="text" id="detailName" />
        </div>
        <div class="field">
          <label>ステータス</label>
          <select id="detailStatus"></select>
        </div>
        <div class="field">
          <label>優先度</label>
          <select id="detailPriority"></select>
        </div>
        <div class="field">
          <label>担当</label>
          <select id="detailAssignee"></select>
        </div>
      </div>
      <div class="row">
        <div class="field">
          <label>開始</label>
          <input type="date" id="detailStart" />
        </div>
        <div class="field">
          <label>終了</label>
          <input type="date" id="detailEnd" />
        </div>
        <div class="field">
          <label>期日</label>
          <input type="date" id="detailDue" />
        </div>
        <div class="field">
          <label>進捗(%)</label>
          <input type="number" id="detailProgress" min="0" max="100" />
        </div>
      </div>
      <div class="row">
        <div class="field" style="min-width: 260px;">
          <label>説明</label>
          <input type="text" id="detailDescription" />
        </div>
        <div class="field">
          <label>予定工数</label>
          <input type="number" id="detailPlanned" min="0" step="0.1" />
        </div>
        <div class="field">
          <label>実績工数</label>
          <input type="number" id="detailActual" min="0" step="0.1" />
        </div>
      </div>
      <div class="row" style="margin-top: 8px;">
        <button id="saveKanbanDetailBtn">更新</button>
      </div>
    </div>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/kanban.js"></script>
<jsp:include page="_bottom.jsp" />
