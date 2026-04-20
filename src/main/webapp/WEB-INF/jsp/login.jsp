<%--
[役割] login: ログイン画面。
[入力] error。
[出力] HTML。
[依存] app.css。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="_top.jsp" />

<div class="login-shell">
  <div class="card login-card">
    <div class="login-panel">
      <div class="login-title">プロジェクト管理システム</div>
      <form method="post" action="${pageContext.request.contextPath}/login" class="login-form">
        <div class="field login-field">
          <label>ログインID</label>
          <input type="text" name="loginId" placeholder="例：99000001" required />
        </div>
        <div class="field login-field">
          <label>パスワード</label>
          <input type="password" name="password" required />
        </div>
        <div class="row login-actions">
          <button type="submit">ログイン</button>
        </div>
        <c:if test="${not empty error}">
          <div class="inline-error login-error">${error}</div>
        </c:if>
      </form>
    </div>
  </div>
</div>

<jsp:include page="_bottom.jsp" />
