<%--
[役割] project: プロジェクト詳細/計画編集画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] project.js。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
  String projectId = request.getParameter("projectId");
  if (projectId == null) {
    projectId = "";
  }
%>
<jsp:include page="_top.jsp" />

<div class="card">
  <div class="row" style="justify-content: space-between; align-items: flex-start;">
    <div>
      <div class="h1">計画編集</div>
      <div class="muted">WBS/ガント/工数を一体で編集します。</div>
    </div>
    <div class="row">
      <button id="savePlanBtn">保存</button>
      <button class="secondary" id="backToListBtn">一覧へ戻る</button>
    </div>
  </div>

  <div class="row" style="margin-top: 18px; align-items: flex-end;">
    <div class="field" style="min-width: 260px;">
      <label>プロジェクト名</label>
      <input type="text" id="planProjectName" />
    </div>
    <div class="field">
      <label>コード</label>
      <input type="text" id="planProjectCode" />
    </div>
    <div class="field">
      <label>開始日</label>
      <input type="date" id="planProjectStart" />
    </div>
    <div class="field">
      <label>終了日</label>
      <input type="date" id="planProjectEnd" />
    </div>
    <div class="field" style="min-width: 220px;">
      <label>ステータス</label>
      <select id="planProjectStatus">
        <option value="ACTIVE">進行中</option>
        <option value="ON_HOLD">保留</option>
        <option value="DONE">完了</option>
      </select>
    </div>
  </div>

  <div class="plan-toolbar">
    <div class="row">
      <button id="addTaskBtn">タスク追加</button>
      <button class="secondary" id="indentTaskBtn">インデント</button>
      <button class="secondary" id="outdentTaskBtn">アウトデント</button>
      <button class="secondary" id="deleteTaskBtn">削除</button>
      <button class="secondary" id="setBaselineBtn">ベースライン保存</button>
    </div>
    <div class="row">
      <div class="toggle">
        <input type="checkbox" id="autoSchedule" checked />
        <label for="autoSchedule">自動スケジュール</label>
      </div>
      <div class="zoom">
        <button class="secondary" data-zoom="-1">縮小</button>
        <span id="zoomLabel">週</span>
        <button class="secondary" data-zoom="1">拡大</button>
      </div>
    </div>
  </div>

  <div class="plan-summary" id="planSummary"></div>

  <div class="plan-settings">
    <div class="field-group">
      <label>稼働日</label>
      <div class="workdays" id="workdaysControl">
        <label><input type="checkbox" value="1" />月</label>
        <label><input type="checkbox" value="2" />火</label>
        <label><input type="checkbox" value="3" />水</label>
        <label><input type="checkbox" value="4" />木</label>
        <label><input type="checkbox" value="5" />金</label>
        <label><input type="checkbox" value="6" />土</label>
        <label><input type="checkbox" value="0" />日</label>
      </div>
    </div>
    <div class="field">
      <label>1日あたり稼働(h)</label>
      <input type="number" id="dailyHours" min="1" max="24" step="0.5" />
    </div>
    <div class="baseline-meta">
      ベースライン: <span id="baselineInfo">未設定</span>
    </div>
  </div>

  <div id="planError" class="inline-error"></div>

  <div class="plan-layout">
    <div class="wbs-panel">
      <table class="wbs-table">
        <thead>
          <tr>
            <th>WBS</th>
            <th>タスク名</th>
            <th>ステータス</th>
            <th>優先度</th>
            <th>開始</th>
            <th>終了</th>
            <th>期日</th>
            <th>期間(日)</th>
            <th>進捗(%)</th>
            <th>予定工数(h)</th>
            <th>実績工数(h)</th>
            <th>担当(ユーザー)</th>
            <th>担当(リソース)</th>
            <th>先行</th>
            <th>MS</th>
          </tr>
        </thead>
        <tbody id="wbsBody"></tbody>
      </table>
    </div>
    <div class="gantt-panel">
      <div class="gantt-header" id="ganttHeader"></div>
      <div class="gantt-body" id="ganttBody">
        <svg class="gantt-lines" id="ganttLines"></svg>
        <div class="gantt-today" id="ganttToday"></div>
      </div>
    </div>
  </div>

  <div class="plan-extra">
    <div class="resource-panel">
      <div class="h2">リソース管理</div>
      <div class="muted">担当者/役割/単価などを登録します。</div>
      <div class="table-scroll">
        <table class="resources-table">
          <thead>
            <tr>
              <th>名前</th>
              <th>役割</th>
              <th>稼働(h/日)</th>
              <th>単価(円/h)</th>
              <th></th>
            </tr>
          </thead>
          <tbody id="resourcesBody"></tbody>
        </table>
      </div>
      <div class="row" style="margin-top: 12px;">
        <button id="addResourceBtn">リソース追加</button>
        <button class="secondary" id="saveResourcesBtn">リソース保存</button>
        <div id="resourceError" class="inline-error"></div>
      </div>
    </div>
    <div class="assignment-panel">
      <div class="h2">リソース割当</div>
      <div class="muted">選択中タスク: <span id="assignmentTaskLabel">未選択</span></div>
      <div id="assignmentList" class="assignment-list"></div>
    </div>
  </div>
</div>

<script>
  window.PROJECT_ID = "<%= projectId %>";
</script>
<script src="${pageContext.request.contextPath}/static/project.js"></script>
<jsp:include page="_bottom.jsp" />
