<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약 완료</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/reservation.css">
</head>
<body>

<div class="reservation-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="reservation-main">

        <section class="complete-wrap">

            <div class="complete-card">

                <div class="complete-icon">✓</div>

                <h1>예약이 완료되었습니다</h1>

                <p class="complete-desc">
                    예약 인증 코드가 발급되었습니다. 예약 시간에 충전소에서 인증 코드를 입력하면 충전이 가능합니다.
                </p>

                <div class="complete-station-box">
                    <span class="complete-station-icon">⚡</span>

                    <div>
                        <strong>${reservation.stationName}</strong>
                        <p>${reservation.stationAddress}</p>
                    </div>
                </div>

                <div class="auth-code-box">
                    <span>예약 인증 코드</span>
                    <strong>${reservation.authCode}</strong>
                    <p>충전소 도착 후 인증 코드 입력 시 충전이 가능합니다.</p>
                </div>

                <div class="complete-summary">

                    <div>
                        <span>예약 상태</span>
                        <strong class="orange-text">${reservation.status}</strong>
                    </div>

                    <div>
                        <span>예약 차량</span>
                        <strong>
                            <c:choose>
                                <c:when test="${not empty reservation.vehicleNickname}">
                                    ${reservation.vehicleNickname} / ${reservation.modelName}
                                </c:when>
                                <c:otherwise>
                                    ${reservation.modelName}
                                </c:otherwise>
                            </c:choose>
                        </strong>
                    </div>

                    <div>
                        <span>충전기</span>
                        <strong>
                            ${reservation.chargerName} · ${reservation.connectorType}
                        </strong>
                    </div>

                    <div>
                        <span>예약 날짜</span>
                        <strong>${reservation.reservationDate}</strong>
                    </div>

                    <div>
                        <span>예약 시작</span>
                        <strong>${reservation.startTimeText}</strong>
                    </div>

                    <div>
                        <span>예약 종료 예정</span>
                        <strong>${reservation.endTimeText}</strong>
                    </div>

                    <div>
                        <span>배터리 잔량</span>
                        <strong>${reservation.currentSoc}% → ${reservation.targetSoc}%</strong>
                    </div>

                    <div>
                        <span>예상 필요 충전량</span>
                        <strong>
                            <fmt:formatNumber value="${reservation.requiredKwh}" pattern="#,##0.00" />kWh
                        </strong>
                    </div>

                    <div>
                        <span>예상 충전 시간</span>
                        <strong>${reservation.estimatedMinutes}분</strong>
                    </div>

                    <div class="complete-cost-row">
                        <span>예상 충전 비용</span>
                        <strong>
                            <fmt:formatNumber value="${reservation.estimatedCost}" pattern="#,###" />원
                        </strong>
                    </div>

                </div>

                <div class="complete-notice">
                    <p>※ 예약 확정 후 발급된 인증 코드는 충전 시작 시 사용됩니다.</p>
                    <p>※ 예약 시간에 충전소에서 인증 코드를 입력하면 충전이 가능합니다.</p>
                    <p>※ 예약 시간 내 인증하지 않으면 노쇼로 처리될 수 있습니다.</p>
                </div>

                <div class="complete-buttons">
                    <a href="${pageContext.request.contextPath}/reservation/my"
                       class="complete-main-btn">
                        내 예약 보기
                    </a>

                    <a href="${pageContext.request.contextPath}/station/map"
                       class="complete-outline-btn">
                        충전소 탐색
                    </a>

                    <a href="${pageContext.request.contextPath}/main"
                       class="complete-outline-btn">
                        메인으로
                    </a>
                </div>

            </div>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

</body>
</html>