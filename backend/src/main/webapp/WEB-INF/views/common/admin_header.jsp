<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<header class="admin-top-header">

    <!-- 로고 클릭 시 메인 홈으로 이동 -->
    <a href="${pageContext.request.contextPath}/main" class="admin-logo">
        <span class="logo-text">EV Charge</span>
    </a>

    <!-- 관리자 상단 메뉴 -->
    <nav class="admin-top-nav">
        <a href="${pageContext.request.contextPath}/main">홈</a>
        <a href="${pageContext.request.contextPath}/admin/dashboard">관리자 대시보드</a>
        <a href="${pageContext.request.contextPath}/member/logout">로그아웃</a>
    </nav>

</header>