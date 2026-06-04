<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 관리자 대시보드</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

            <!-- 제목 영역 -->
            <section class="admin-title-row">
                <div>
                    <h1>관리자 대시보드</h1>
                    <p>EV Charge 서비스 운영 현황을 한눈에 확인하고 관리할 수 있습니다.</p>
                </div>

                <form action="${pageContext.request.contextPath}/admin/dashboard" method="get" class="date-box">
                    <input type="date" name="date" value="${selectedDate}" onchange="this.form.submit()">
                </form>
            </section>

            <!-- 상단 요약 카드 -->
            <section class="summary-grid">

                <article class="summary-card">
                    <span>전체 회원 수</span>
                    <strong>
                        <fmt:formatNumber value="${dashboard.totalMemberCount}" pattern="#,###"/>명
                    </strong>
                    <p>선택일 신규 +<fmt:formatNumber value="${dashboard.memberIncreaseCount}" pattern="#,###"/></p>
                </article>

                <article class="summary-card">
                    <span>전체 충전소 수</span>
                    <strong>
                        <fmt:formatNumber value="${dashboard.totalStationCount}" pattern="#,###"/>개
                    </strong>
                    <p>선택일 신규 +<fmt:formatNumber value="${dashboard.stationIncreaseCount}" pattern="#,###"/></p>
                </article>

                <article class="summary-card">
                    <span>전체 충전기 수</span>
                    <strong>
                        <fmt:formatNumber value="${dashboard.totalChargerCount}" pattern="#,###"/>대
                    </strong>
                    <p>선택일 신규 +<fmt:formatNumber value="${dashboard.chargerIncreaseCount}" pattern="#,###"/></p>
                </article>

                <article class="summary-card">
                    <span>선택일 예약 수</span>
                    <strong>
                        <fmt:formatNumber value="${dashboard.todayReservationCount}" pattern="#,###"/>건
                    </strong>
                    <p>전일 대비 ${dashboard.reservationIncreaseCount}</p>
                </article>

            </section>

            <!-- 중간 영역 -->
            <section class="dashboard-grid">

                <!-- 충전기 상태 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>충전기 상태 현황</h2>
                    </div>

                    <div class="charger-status-grid">

                        <div>
                            <span class="status-icon green">✓</span>
                            <p>사용 가능</p>
                            <strong class="green-text">
                                <fmt:formatNumber value="${dashboard.availableChargerCount}" pattern="#,###"/>대
                            </strong>
                            <em>${dashboard.availableChargerRate}%</em>
                        </div>

                        <div>
                            <span class="status-icon blue">■</span>
                            <p>사용중</p>
                            <strong class="blue-text">
                                <fmt:formatNumber value="${dashboard.chargingChargerCount}" pattern="#,###"/>대
                            </strong>
                            <em>${dashboard.chargingChargerRate}%</em>
                        </div>

                        <div>
                            <span class="status-icon orange">⌚</span>
                            <p>예약됨</p>
                            <strong class="orange-text">
                                <fmt:formatNumber value="${dashboard.reservedChargerCount}" pattern="#,###"/>대
                            </strong>
                            <em>${dashboard.reservedChargerRate}%</em>
                        </div>

                        <div>
                            <span class="status-icon red">!</span>
                            <p>점검중/고장</p>
                            <strong class="red-text">
                                <fmt:formatNumber value="${dashboard.troubleChargerCount}" pattern="#,###"/>대
                            </strong>
                            <em>${dashboard.troubleChargerRate}%</em>
                        </div>

                    </div>
                </article>

                <!-- 선택일 예약 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>선택일 예약 현황</h2>
                        <a href="${pageContext.request.contextPath}/admin/reservation/list">전체 보기</a>
                    </div>

                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>예약자</th>
                                <th>차량</th>
                                <th>충전소</th>
                                <th>충전기</th>
                                <th>예약 시간</th>
                                <th>상태</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:choose>
                                <c:when test="${empty dashboard.todayReservationList}">
                                    <tr>
                                        <td colspan="6">선택한 날짜의 예약 내역이 없습니다.</td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="reservation" items="${dashboard.todayReservationList}">
                                        <tr>
                                            <td>${reservation.memberName}</td>
                                            <td>${reservation.modelName}</td>
                                            <td>${reservation.stationName}</td>
                                            <td>${reservation.chargerName}</td>
                                            <td>${reservation.reservationTimeText}</td>
                                            <td>
                                                <span class="table-badge ${reservation.statusClass}">
                                                    ${reservation.statusText}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </article>

            </section>

            <!-- 하단 영역 -->
            <section class="bottom-grid">

                <!-- 충전소 운영 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>충전소 운영 현황</h2>
                        <a href="${pageContext.request.contextPath}/admin/station/list">전체 보기</a>
                    </div>

                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>충전소명</th>
                                <th>주소</th>
                                <th>충전기 수</th>
                                <th>사용 가능</th>
                                <th>운영 상태</th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:choose>
                                <c:when test="${empty dashboard.stationStatusList}">
                                    <tr>
                                        <td colspan="5">등록된 충전소가 없습니다.</td>
                                    </tr>
                                </c:when>

                                <c:otherwise>
                                    <c:forEach var="station" items="${dashboard.stationStatusList}">
                                        <tr>
                                            <td>${station.stationName}</td>
                                            <td>${station.address}</td>
                                            <td>
                                                <fmt:formatNumber value="${station.totalChargerCount}" pattern="#,###"/>
                                            </td>
                                            <td>
                                                <fmt:formatNumber value="${station.availableChargerCount}" pattern="#,###"/>대
                                            </td>
                                            <td>
                                                <span class="dot ${station.dotClass}"></span>
                                                ${station.operationText}
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </article>

                <!-- 최근 알림 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>최근 알림</h2>
                    </div>

                    <div class="notice-list">
                        <c:forEach var="notice" items="${dashboard.noticeList}">
                            <div>
                                <strong>${notice.message}</strong>
                                <span>${notice.timeText}</span>
                            </div>
                        </c:forEach>
                    </div>
                </article>

            </section>

        </main>

    </div>

</div>

</body>
</html>