<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<header class="top-header">

    <div class="logo-area">
        <a href="${pageContext.request.contextPath}/main" class="logo-link">
            <span class="logo-text">EV Charge</span>
        </a>
    </div>

    <nav class="top-nav">

        <a href="${pageContext.request.contextPath}/main" data-menu="main">
            홈
        </a>

        <a href="${pageContext.request.contextPath}/station/map" data-menu="station">
            충전소 검색
        </a>

        <a href="${pageContext.request.contextPath}/reservation/my" data-menu="reservation">
            내 예약
        </a>

        <a href="${pageContext.request.contextPath}/reservation/history" data-menu="history">
            충전 내역
        </a>

        <a href="${pageContext.request.contextPath}/vehicle/list" data-menu="vehicle">
            내 차량
        </a>

        <!-- 로그인한 회원에게만 회원정보수정 메뉴 노출 -->
        <sec:authorize access="isAuthenticated()">
            <a href="${pageContext.request.contextPath}/member/mypage/edit" data-menu="mypage">
                회원정보수정
            </a>
        </sec:authorize>

        <!-- 관리자에게만 관리자 대시보드 노출 -->
        <sec:authorize access="hasRole('ADMIN')">
            <a href="${pageContext.request.contextPath}/admin/dashboard" data-menu="admin">
                관리자 대시보드
            </a>
        </sec:authorize>

    </nav>

	<div class="user-area">
	
	    <sec:authorize access="isAuthenticated()">
	        <sec:authentication property="principal.profileImageUrl" var="headerProfileImageUrl" />
	        <sec:authentication property="principal.nickname" var="headerNickname" />
	
	        <div class="user-profile">
	            <img src="${pageContext.request.contextPath}${headerProfileImageUrl}"
	                 alt="프로필"
	                 class="profile-image">
	
	            <span class="user-name">${headerNickname}</span>
	        </div>
	
	        <form action="${pageContext.request.contextPath}/logout"
	              method="post"
	              class="logout-form">
	            <button type="submit" class="header-logout-btn">
	                로그아웃
	            </button>
	        </form>
	    </sec:authorize>
	
	    <sec:authorize access="isAnonymous()">
	        <a href="${pageContext.request.contextPath}/login"
	           class="header-login-btn">
	            로그인
	        </a>
	    </sec:authorize>
	
	</div>

</header>

<script>
    const currentPath = window.location.pathname;
    const menuLinks = document.querySelectorAll(".top-nav a, .user-area a");

    menuLinks.forEach(function(link) {
        const menu = link.dataset.menu;

        if (menu === "main" && (currentPath === "/" || currentPath.startsWith("/main"))) {
            link.classList.add("active");
        }

        if (menu === "station" && currentPath.startsWith("/station")) {
            link.classList.add("active");
        }

        if (menu === "reservation"
                && currentPath.startsWith("/reservation")
                && !currentPath.startsWith("/reservation/history")) {
            link.classList.add("active");
        }

        if (menu === "history" && currentPath.startsWith("/reservation/history")) {
            link.classList.add("active");
        }

        if (menu === "vehicle" && currentPath.startsWith("/vehicle")) {
            link.classList.add("active");
        }

        if (menu === "mypage" && currentPath.startsWith("/member/mypage")) {
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