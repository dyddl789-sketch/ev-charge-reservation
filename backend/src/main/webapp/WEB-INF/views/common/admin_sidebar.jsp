<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<aside class="admin-sidebar">

    <nav class="admin-menu">

        <a href="/admin/dashboard" class="dashboard-link">대시보드</a>

        <div class="menu-group">
            <p>회원 관리</p>
            <a href="/admin/member/list">회원 목록</a>
        </div>

        <div class="menu-group">
            <p>충전소 관리</p>
            <a href="/admin/station/list">충전소 목록</a>
            <a href="/admin/station/manage">충전소 등록/관리</a>
        </div>

        <div class="menu-group">
            <p>예약 관리</p>
            <a href="/admin/reservation/list">예약 현황</a>
        </div>

        <div class="menu-group">
            <p>통계/분석</p>
            <a href="/admin/stat/usage">이용 통계</a>
            <a href="/admin/stat/sales">매출 통계</a>
        </div>

    </nav>

</aside>