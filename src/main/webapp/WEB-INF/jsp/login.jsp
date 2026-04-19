<%--
[役割] login: ログイン画面。
[入力] error。
[出力] HTML。
[依存] app.css。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="_top.jsp" />

<div class="card" style="max-width: 520px; margin: 40px auto;">
  <div class="h1">ログイン</div>
  <div class="muted" style="margin-bottom: 12px;">利用を開始するには、配布されたアカウント情報でログインしてください。</div>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <div class="field">
      <label>ログインID</label>
      <input type="text" name="loginId" required />
    </div>
    <div class="field">
      <label>パスワード</label>
      <input type="password" name="password" required />
    </div>
    <div class="row" style="margin-top: 12px;">
      <button type="submit">ログイン</button>
    </div>
    <c:if test="${not empty error}">
      <div class="inline-error" style="margin-top: 12px;">${error}</div>
    </c:if>
  </form>
</div>

<jsp:include page="_bottom.jsp" />
