<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<header class="top-header">
    <div class="logo-area">
        <a href="/main" class="logo-link">
            <span class="logo-text">EV Charge</span>
        </a>
    </div>

    <nav class="top-nav">
	    <a href="${pageContext.request.contextPath}/main" data-menu="main">홈</a>
	    <a href="${pageContext.request.contextPath}/station/list" data-menu="station">충전소 검색</a>
	    <a href="${pageContext.request.contextPath}/reservation/my" data-menu="reservation">내 예약</a>
	    <a href="${pageContext.request.contextPath}/vehicle/list" data-menu="vehicle">내 차량</a>
	
		<sec:authorize access="isAnonymous()">
		    <a href="${pageContext.request.contextPath}/login" data-menu="login">
		        로그인
		    </a>
		</sec:authorize>
		
		<sec:authorize access="isAuthenticated()">
		    <a href="${pageContext.request.contextPath}/logout" data-menu="logout">
		        로그아웃
		    </a>
		</sec:authorize>
	    
		<sec:authorize access="hasRole('ADMIN')">
		    <a href="${pageContext.request.contextPath}/admin/dashboard" data-menu="admin">
		        관리자 대시보드
		    </a>
		</sec:authorize>
	</nav>

    <div class="user-area">
        <span class="user-icon">👤</span>

		<sec:authorize access="isAuthenticated()">
		    <span class="user-name">
		        <sec:authentication property="principal.memberName" />
		        님
		    </span>
		    <a href="${pageContext.request.contextPath}/logout"
           class="logout-btn">
            로그아웃
       	    </a>
		</sec:authorize>
		
		<sec:authorize access="isAnonymous()">
		    <span class="user-name">비회원</span>
		</sec:authorize>
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
    });
</script>