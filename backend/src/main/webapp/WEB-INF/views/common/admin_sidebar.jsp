<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<aside class="admin-sidebar">

    <nav class="admin-menu">

        <!-- 대시보드 -->
        <a href="${pageContext.request.contextPath}/admin/dashboard"
           class="dashboard-link">
            대시보드
        </a>

        <!-- 회원 관리 -->
        <div class="menu-group">
            <p>회원 관리</p>

            <a href="${pageContext.request.contextPath}/admin/member/list">
                회원 목록
            </a>
        </div>

        <!-- 충전소 관리 -->
        <div class="menu-group">
            <p>충전소 관리</p>

            <a href="${pageContext.request.contextPath}/admin/station/list">
                충전소 목록
            </a>

            <a href="${pageContext.request.contextPath}/admin/station/manage">
                충전소 등록/관리
            </a>
        </div>

        <!-- 예약 관리 -->
        <div class="menu-group">
            <p>예약 관리</p>

            <a href="${pageContext.request.contextPath}/admin/reservation/list">
                예약 현황
            </a>
        </div>

        <!-- 통계/분석 -->
        <div class="menu-group">
            <p>통계/분석</p>

            <a href="${pageContext.request.contextPath}/admin/usage">
                이용 통계
            </a>

            <a href="${pageContext.request.contextPath}/admin/sales">
                매출 통계
            </a>
        </div>

    </nav>

</aside>