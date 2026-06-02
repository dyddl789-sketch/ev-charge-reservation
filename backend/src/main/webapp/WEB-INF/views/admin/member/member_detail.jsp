<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원 상세보기</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/admin/member_detail.css">
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
                    <h1>회원 상세보기</h1>
                    <p>회원의 기본 정보와 이용 현황을 확인합니다.</p>
                </div>

                <a href="${pageContext.request.contextPath}/admin/member/list"
                   class="detail-btn">
                    목록으로
                </a>
            </section>

            <!-- 회원 기본 정보 -->
            <section class="member-detail-card">

                <div class="member-detail-header">
                    <div class="member-avatar">
                       ${memberDetail.initial}
                    </div>

                    <div>
                        <h2>${memberDetail.memberName}</h2>
                        <p>${memberDetail.userId}</p>
                    </div>

                    <span class="status-badge ${memberDetail.statusClass}">
                        ${memberDetail.statusText}
                    </span>
                </div>

                <div class="member-detail-grid">

                    <div>
                        <span>회원 번호</span>
                        <strong>${memberDetail.memberId}</strong>
                    </div>

                    <div>
                        <span>회원 구분</span>
                        <strong>${memberDetail.userTypeText}</strong>
                    </div>

                    <div>
                        <span>로그인 타입</span>
                        <strong>${memberDetail.loginTypeText}</strong>
                    </div>

                    <div>
                        <span>닉네임</span>
                        <strong>${empty memberDetail.nickname ? '-' : memberDetail.nickname}</strong>
                    </div>

                    <div>
                        <span>이메일</span>
                        <strong>${memberDetail.email}</strong>
                    </div>

                    <div>
                        <span>연락처</span>
                        <strong>${empty memberDetail.phone ? '-' : memberDetail.phone}</strong>
                    </div>

                    <div>
                        <span>가입일</span>
                        <strong>${memberDetail.createdAtText}</strong>
                    </div>

                    <div>
                        <span>마지막 로그인</span>
                        <strong>${memberDetail.lastLoginAtText}</strong>
                    </div>

                </div>

            </section>

			<!-- 이용 현황 -->
			<section class="member-summary detail-summary">
			
			    <article class="member-summary-card">
			        <div>
			            <span>등록 차량 수</span>
			            <strong>
			                <fmt:formatNumber value="${memberDetail.vehicleCount}" pattern="#,###"/>대
			            </strong>
			        </div>
			    </article>
			
			    <article class="member-summary-card">
			        <div>
			            <span>예약 건수</span>
			            <strong>
			                <fmt:formatNumber value="${memberDetail.reservationCount}" pattern="#,###"/>건
			            </strong>
			        </div>
			    </article>
			
			    <article class="member-summary-card">
			        <div>
			            <span>충전 완료 건수</span>
			            <strong>
			                <fmt:formatNumber value="${memberDetail.completedSessionCount}" pattern="#,###"/>건
			            </strong>
			        </div>
			    </article>
			
			    <article class="member-summary-card">
			        <div>
			            <span>총 결제 금액</span>
			            <strong>
			                <fmt:formatNumber value="${memberDetail.totalPaymentAmount}" pattern="#,###"/>원
			            </strong>
			        </div>
			    </article>
			
			</section>
			
			<!-- 회원 활동 내역 -->
			<section class="member-history-card">
			
			    <div class="history-tab">
			
			        <button class="tab-btn active"
			                data-tab="vehicle">
			            등록 차량
			        </button>
			
			        <button class="tab-btn"
			                data-tab="reservation">
			            예약 내역
			        </button>
			
			        <button class="tab-btn"
			                data-tab="charging">
			            충전 내역
			        </button>
			
			        <button class="tab-btn"
			                data-tab="payment">
			            결제 내역
			        </button>
			
			    </div>
			
			    <!-- 등록 차량 -->
			    <div class="history-panel active"
			         id="vehiclePanel">
			
			        <table class="history-table">
			
			            <thead>
			            <tr>
			                <th>번호</th>
			                <th>차량 별칭</th>
			                <th>차량 모델</th>
			                <th>차량 번호</th>
			                <th>기본 차량</th>
			                <th>등록일</th>
			            </tr>
			            </thead>
			
			            <tbody>
			
			            <c:choose>
			
			                <c:when test="${empty vehicleList}">
			                    <tr>
			                        <td colspan="6">
			                            등록된 차량이 없습니다.
			                        </td>
			                    </tr>
			                </c:when>
			
			                <c:otherwise>
			
			                    <c:forEach var="vehicle"
			                               items="${vehicleList}">
			
			                        <tr>
			
			                            <td>${vehicle.vehicleId}</td>
			
			                            <td>
			                                ${vehicle.vehicleNickname}
			                            </td>
			
			                            <td>
			                                ${vehicle.manufacturer}
			                                ${vehicle.modelName}
			                            </td>
			
			                            <td>
			                                ${vehicle.plateNumber}
			                            </td>
			
			                            <td>
			
			                                <c:choose>
			
			                                    <c:when test="${vehicle.isDefault}">
			                                        <span class="default-badge">
			                                            대표
			                                        </span>
			                                    </c:when>
			
			                                    <c:otherwise>
			                                        -
			                                    </c:otherwise>
			
			                                </c:choose>
			
			                            </td>
			
			                            <td>
			                                ${vehicle.createdAtText}
			                            </td>
			
			                        </tr>
			
			                    </c:forEach>
			
			                </c:otherwise>
			
			            </c:choose>
			
			            </tbody>
			
			        </table>
			
			    </div>
			
			    <!-- 예약 내역 -->
			    <div class="history-panel"
			         id="reservationPanel">
			
			        <table class="history-table">
			
			            <thead>
			            <tr>
			                <th>예약번호</th>
			                <th>충전소</th>
			                <th>충전기</th>
			                <th>차량</th>
			                <th>예약시간</th>
			                <th>상태</th>
			            </tr>
			            </thead>
			
			            <tbody>
			
			            <c:choose>
			
			                <c:when test="${empty reservationList}">
			                    <tr>
			                        <td colspan="6">
			                            예약 내역이 없습니다.
			                        </td>
			                    </tr>
			                </c:when>
			
			                <c:otherwise>
			
			                    <c:forEach var="reservation"
			                               items="${reservationList}">
			
			                        <tr>
			
			                            <td>${reservation.reservationId}</td>
			
			                            <td>${reservation.stationName}</td>
			
			                            <td>${reservation.chargerName}</td>
			
			                            <td>
			                                ${reservation.vehicleNickname}
			                                (${reservation.modelName})
			                            </td>
			
			                            <td>
			                                ${reservation.startTimeText}
			                            </td>
			
			                            <td>${reservation.status}</td>
			
			                        </tr>
			
			                    </c:forEach>
			
			                </c:otherwise>
			
			            </c:choose>
			
			            </tbody>
			
			        </table>
			
			    </div>
			
			    <!-- 충전 내역 -->
			    <div class="history-panel"
			         id="chargingPanel">
			
			        <table class="history-table">
			
			            <thead>
			            <tr>
			                <th>세션번호</th>
			                <th>충전소</th>
			                <th>차량</th>
			                <th>충전량(kWh)</th>
			                <th>충전금액</th>
			                <th>상태</th>
			            </tr>
			            </thead>
			
			            <tbody>
			
			            <c:choose>
			
			                <c:when test="${empty chargingList}">
			                    <tr>
			                        <td colspan="6">
			                            충전 내역이 없습니다.
			                        </td>
			                    </tr>
			                </c:when>
			
			                <c:otherwise>
			
			                    <c:forEach var="charging"
			                               items="${chargingList}">
			
			                        <tr>
			
			                            <td>${charging.sessionId}</td>
			
			                            <td>${charging.stationName}</td>
			
			                            <td>
			                                ${charging.vehicleNickname}
			                                (${charging.modelName})
			                            </td>
			
			                            <td>${charging.actualKwh}</td>
			
			                            <td>
			                                <fmt:formatNumber
			                                        value="${charging.actualCost}"
			                                        pattern="#,###"/>원
			                            </td>
			
			                            <td>${charging.status}</td>
			
			                        </tr>
			
			                    </c:forEach>
			
			                </c:otherwise>
			
			            </c:choose>
			
			            </tbody>
			
			        </table>
			
			    </div>
			
			    <!-- 결제 내역 -->
			    <div class="history-panel"
			         id="paymentPanel">
			
			        <table class="history-table">
			
			            <thead>
			            <tr>
			                <th>세션번호</th>
			                <th>충전소</th>
			                <th>충전량(kWh)</th>
			                <th>결제금액</th>
			                <th>결제일</th>
			            </tr>
			            </thead>
			
			            <tbody>
			
			            <c:choose>
			
			                <c:when test="${empty paymentList}">
			                    <tr>
			                        <td colspan="5">
			                            결제 내역이 없습니다.
			                        </td>
			                    </tr>
			                </c:when>
			
			                <c:otherwise>
			
			                    <c:forEach var="payment"
			                               items="${paymentList}">
			
			                        <tr>
			
			                            <td>${payment.sessionId}</td>
			
			                            <td>${payment.stationName}</td>
			
			                            <td>${payment.actualKwh}</td>
			
			                            <td>
			                                <fmt:formatNumber
			                                        value="${payment.actualCost}"
			                                        pattern="#,###"/>원
			                            </td>
			
			                            <td>${payment.paidAtText}</td>
			
			                        </tr>
			
			                    </c:forEach>
			
			                </c:otherwise>
			
			            </c:choose>
			
			            </tbody>
			
			        </table>
			
			    </div>
			
			</section>

        </main>

    </div>

</div>

<script>
    const withdrawForms = document.querySelectorAll(".withdraw-form");

    withdrawForms.forEach(function(form) {
        form.addEventListener("submit", function(event) {
            const result = confirm("해당 회원을 탈퇴 처리하시겠습니까?");

            if (!result) {
                event.preventDefault();
            }
        });
    });

    const restoreForms = document.querySelectorAll(".restore-form");

    restoreForms.forEach(function(form) {
        form.addEventListener("submit", function(event) {
            const result = confirm("해당 회원을 복구하시겠습니까?");

            if (!result) {
                event.preventDefault();
            }
        });
    });
    const tabs = document.querySelectorAll(".tab-btn");
    const panels = document.querySelectorAll(".history-panel");

    tabs.forEach(tab => {

        tab.addEventListener("click", function() {

            tabs.forEach(btn =>
                btn.classList.remove("active")
            );

            panels.forEach(panel =>
                panel.classList.remove("active")
            );

            tab.classList.add("active");

            const target = tab.dataset.tab;

            document
                .getElementById(target + "Panel")
                .classList.add("active");
        });
    });

</script>

</body>
</html>