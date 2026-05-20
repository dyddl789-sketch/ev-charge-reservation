<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<header class="top-header">
    <div class="logo-area">
        <a href="/main" class="logo-link">
            <span class="logo-icon">⚡</span>
            <span class="logo-text">EV Charge</span>
        </a>
    </div>

    <nav class="top-nav">
        <a href="/main" data-menu="main">홈</a>
        <a href="/station/list" data-menu="station">충전소 탐색</a>
        <a href="/reservation/my" data-menu="reservation">내 예약</a>
        <a href="/vehicle/list" data-menu="vehicle">내 차량</a>
    </nav>

    <div class="user-area">
        <span class="alarm-icon">🔔</span>
        <span class="user-icon">👤</span>
        <span class="user-name">홍길동 님</span>
        <span class="arrow">⌄</span>
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
    });
</script>