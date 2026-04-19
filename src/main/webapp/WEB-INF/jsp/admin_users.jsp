<%--
[役割] admin_users: ユーザー管理画面。
[入力] APIレスポンス。
[出力] HTML。
[依存] admin_users.js。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<jsp:include page="_top.jsp" />

<div class="card">
  <div class="h1">ユーザー管理</div>
  <div class="muted">システム利用者・権限・有効状態を管理します。</div>

  <div id="adminUsersError" class="inline-error"></div>

  <div class="admin-user-form">
    <div class="field">
      <label>ログインID</label>
      <input type="text" id="newUserLoginId" />
    </div>
    <div class="field">
      <label>氏名</label>
      <input type="text" id="newUserName" />
    </div>
    <div class="field">
      <label>メール</label>
      <input type="email" id="newUserEmail" />
    </div>
    <div class="field">
      <label>権限</label>
      <select id="newUserRole">
        <option value="ADMIN">ADMIN</option>
        <option value="MANAGER">MANAGER</option>
        <option value="MEMBER" selected>MEMBER</option>
        <option value="VIEWER">VIEWER</option>
      </select>
    </div>
    <div class="field">
      <label>初期パスワード</label>
      <input type="password" id="newUserPassword" />
    </div>
    <div class="field">
      <label>有効</label>
      <select id="newUserActive">
        <option value="true" selected>有効</option>
        <option value="false">無効</option>
      </select>
    </div>
    <div class="row" style="align-items: flex-end;">
      <button id="createUserBtn">追加</button>
    </div>
  </div>

  <div class="table-scroll" style="margin-top: 12px;">
    <table class="admin-users-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>ログインID</th>
          <th>氏名</th>
          <th>メール</th>
          <th>権限</th>
          <th>有効</th>
          <th>パスワード更新</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody id="adminUsersBody"></tbody>
    </table>
  </div>
</div>

<script src="${pageContext.request.contextPath}/static/admin_users.js"></script>
<jsp:include page="_bottom.jsp" />
