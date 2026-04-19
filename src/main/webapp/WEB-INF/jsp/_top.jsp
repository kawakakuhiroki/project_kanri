<%--
[役割] _top: 共通レイアウトの先頭。
[入力] request属性。
[出力] HTMLヘッダー/共通UI。
[依存] 共通CSS。
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${pageTitle} | プロジェクト管理システム</title>
  <meta name="context-path" content="${pageContext.request.contextPath}" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/static/app.css" />
</head>
<body>
  <div class="container">
    <c:choose>
      <c:when test="${requestScope.showGlobalHeader == false}">
        <div class="auth-header">
          <div class="nav-title">プロジェクト管理システム</div>
          <div class="muted">プロジェクトの計画・進行・共有を一元管理します。</div>
        </div>
      </c:when>
      <c:otherwise>
        <jsp:include page="_header.jsp" />
      </c:otherwise>
    </c:choose>
