<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="top-header">
    <div class="logo-area">
        <a href="${pageContext.request.contextPath}/main" class="logo-link">
            <span class="logo-text">EV Charge</span>
        </a>
    </div>

    <nav class="top-nav">
        <a href="${pageContext.request.contextPath}/main" data-menu="main">홈</a>

        <!-- 충전소 검색은 지도 기반 탐색 화면으로 이동 -->
        <a href="${pageContext.request.contextPath}/station/map" data-menu="station">충전소 검색</a>

        <a href="${pageContext.request.contextPath}/reservation/my" data-menu="reservation">내 예약</a>
        <a href="${pageContext.request.contextPath}/vehicle/list" data-menu="vehicle">내 차량</a>

        <c:choose>
            <c:when test="${empty sessionScope.loginMemberId}">
                <a href="${pageContext.request.contextPath}/login" data-menu="login">로그인</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/logout" data-menu="logout">로그아웃</a>
            </c:otherwise>
        </c:choose>

        <c:if test="${sessionScope.loginUserType eq 'ADMIN'}">
            <a href="${pageContext.request.contextPath}/admin/dashboard" data-menu="admin">
                관리자 대시보드
            </a>
        </c:if>
    </nav>

    <div class="user-area">
        <span class="user-icon">👤</span>

        <c:choose>
            <c:when test="${not empty sessionScope.loginMemberName}">
                <span class="user-name">${sessionScope.loginMemberName} 님</span>
            </c:when>
            <c:otherwise>
                <span class="user-name">비회원</span>
            </c:otherwise>
        </c:choose>
    </div>
</header>

<script>
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll(".top-nav a");

    menuLinks.forEach(function(link) {
        const menu = link.dataset.menu;

        if (menu === "main" && (currentPath === "/" || currentPath.startsWith("/main"))) {
            link.classList.add("active");
        }

        if (menu === "station" && currentPath.startsWith("/station")) {
            link.classList.add("active");
        }

        if (menu === "reservation" && currentPath.startsWith("/reservation")) {
            link.classList.add("active");
        }

        if (menu === "vehicle" && currentPath.startsWith("/vehicle")) {
            link.classList.add("active");
        }

        if (menu === "login" && currentPath === "/login") {
            link.classList.add("active");
        }

        if (menu === "admin" && currentPath.startsWith("/admin")) {
            link.classList.add("active");
        }
    });
</script>