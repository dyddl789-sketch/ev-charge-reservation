<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 내 예약</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/reservation.css">
</head>
<body>

<div class="reservation-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="reservation-main">

        <section class="page-title">
            <div>
                <h1>내 예약</h1>
                <p>예약한 충전 내역과 충전 진행 상태를 확인할 수 있습니다.</p>
            </div>

            <a href="${pageContext.request.contextPath}/station/map" class="back-btn">
                충전소 탐색
            </a>
        </section>

        <!--
            예약 요약 계산

            reservationList를 반복하면서 상태별 개수를 계산한다.
            실제 충전 비용은 charging_session과 연결된 후 계산하는 것이 정확하므로
            현재는 예약 시 계산된 estimatedCost 기준으로 합산한다.
        -->
        <c:set var="totalCount" value="0" />
        <c:set var="reservedCount" value="0" />
        <c:set var="verifiedCount" value="0" />
        <c:set var="usingCount" value="0" />
        <c:set var="completedCount" value="0" />
        <c:set var="canceledCount" value="0" />
        <c:set var="noShowCount" value="0" />
        <c:set var="totalEstimatedCost" value="0" />

        <c:forEach var="reservation" items="${reservationList}">
            <c:set var="totalCount" value="${totalCount + 1}" />

            <c:if test="${reservation.status == '예약완료'}">
                <c:set var="reservedCount" value="${reservedCount + 1}" />
            </c:if>

            <c:if test="${reservation.status == '인증완료'}">
                <c:set var="verifiedCount" value="${verifiedCount + 1}" />
            </c:if>

            <c:if test="${reservation.status == '충전중'}">
                <c:set var="usingCount" value="${usingCount + 1}" />
            </c:if>

            <c:if test="${reservation.status == '완료'}">
                <c:set var="completedCount" value="${completedCount + 1}" />
            </c:if>

            <c:if test="${reservation.status == '취소'}">
                <c:set var="canceledCount" value="${canceledCount + 1}" />
            </c:if>

            <c:if test="${reservation.status == '노쇼'}">
                <c:set var="noShowCount" value="${noShowCount + 1}" />
            </c:if>

            <c:if test="${reservation.estimatedCost != null}">
                <c:set var="totalEstimatedCost"
                       value="${totalEstimatedCost + reservation.estimatedCost}" />
            </c:if>
        </c:forEach>

        <!-- 예약 요약 -->
        <section class="my-summary-grid">

            <article class="my-summary-card">
                <span>전체 예약</span>
                <strong>${totalCount}건</strong>
            </article>

            <article class="my-summary-card">
                <span>예약 완료</span>
                <strong>${reservedCount}건</strong>
            </article>

            <article class="my-summary-card">
                <span>충전 완료</span>
                <strong>${completedCount}건</strong>
            </article>

            <article class="my-summary-card">
                <span>총 예상 비용</span>
                <strong>
                    <fmt:formatNumber value="${totalEstimatedCost}" pattern="#,###" />원
                </strong>
            </article>

        </section>

        <!-- 예약 필터 -->
        <section class="reservation-filter-card">
            <div class="filter-left">
                <button type="button" class="filter-tab active" data-status="전체">
                    전체
                </button>
                <button type="button" class="filter-tab" data-status="예약완료">
                    예약완료
                </button>
                <button type="button" class="filter-tab" data-status="인증완료">
                    인증완료
                </button>
                <button type="button" class="filter-tab" data-status="충전중">
                    충전중
                </button>
                <button type="button" class="filter-tab" data-status="완료">
                    완료
                </button>
                <button type="button" class="filter-tab" data-status="취소">
                    취소
                </button>
                <button type="button" class="filter-tab" data-status="노쇼">
                    노쇼
                </button>
            </div>

            <div class="filter-right">
                <input type="month" id="monthFilter">
            </div>
        </section>

        <!-- 예약 목록 -->
        <section class="my-reservation-list">

            <c:choose>
                <c:when test="${empty reservationList}">
                    <div class="empty-reservation-box">
                        <h2>예약 내역이 없습니다.</h2>
                        <p>충전소를 선택한 후 예약을 진행해보세요.</p>

                        <a href="${pageContext.request.contextPath}/station/map"
                           class="complete-main-btn">
                            충전소 탐색하기
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <c:forEach var="reservation" items="${reservationList}">

                        <!--
                            예약 상태에 따라 배지 CSS 클래스 결정
                        -->
                        <c:set var="statusClass" value="reserved" />

                        <c:choose>
                            <c:when test="${reservation.status == '예약완료'}">
                                <c:set var="statusClass" value="reserved" />
                            </c:when>

                            <c:when test="${reservation.status == '인증완료'}">
                                <c:set var="statusClass" value="verified" />
                            </c:when>

                            <c:when test="${reservation.status == '충전중'}">
                                <c:set var="statusClass" value="using" />
                            </c:when>

                            <c:when test="${reservation.status == '완료'}">
                                <c:set var="statusClass" value="completed" />
                            </c:when>

                            <c:when test="${reservation.status == '취소'}">
                                <c:set var="statusClass" value="canceled" />
                            </c:when>

                            <c:when test="${reservation.status == '노쇼'}">
                                <c:set var="statusClass" value="noshow" />
                            </c:when>
                        </c:choose>

                        <!--
                            상태별 카드 클래스 결정
                        -->
                        <c:set var="cardClass" value="my-reservation-card" />

                        <c:if test="${reservation.status == '충전중'}">
                            <c:set var="cardClass" value="my-reservation-card active-session" />
                        </c:if>

                        <c:if test="${reservation.status == '취소'}">
                            <c:set var="cardClass" value="my-reservation-card canceled-card" />
                        </c:if>

                        <article class="${cardClass}" data-status="${reservation.status}">

                            <div class="reservation-card-header">
                                <div>
                                    <span class="status-badge ${statusClass}">
                                        ${reservation.status}
                                    </span>

                                    <h2>${reservation.stationName}</h2>
                                    <p>${reservation.stationAddress}</p>
                                </div>

                                <div class="reservation-date-box">
                                    <strong>${reservation.reservationDate}</strong>
                                    <span>
                                        ${reservation.startTimeText}
                                        ~
                                        ${reservation.endTimeText}
                                    </span>
                                </div>
                            </div>

                            <div class="reservation-info-grid">

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
                                    <span>배터리</span>
                                    <strong>
                                        ${reservation.currentSoc}% → ${reservation.targetSoc}%
                                    </strong>
                                </div>

                                <div>
                                    <span>예상 충전량</span>
                                    <strong>
                                        <fmt:formatNumber value="${reservation.requiredKwh}"
                                                          pattern="#,##0.00" />kWh
                                    </strong>
                                </div>

                                <div>
                                    <span>예상 시간</span>
                                    <strong>${reservation.estimatedMinutes}분</strong>
                                </div>

                                <div>
                                    <span>예상 비용</span>
                                    <strong>
                                        <fmt:formatNumber value="${reservation.estimatedCost}"
                                                          pattern="#,###" />원
                                    </strong>
                                </div>

                            </div>

                            <!--
                                충전중 상태일 때만 진행 영역 표시

                                현재는 charging_session 연동 전이므로
                                실제 진행률은 아직 표시하지 않는다.
                            -->
                            <c:if test="${reservation.status == '충전중'}">
                                <div class="charging-progress">
                                    <div class="progress-top">
                                        <span>충전 진행률</span>
                                        <strong>진행중</strong>
                                    </div>

                                    <div class="progress-bar">
                                        <span style="width: 50%;"></span>
                                    </div>
                                </div>
                            </c:if>

                            <!--
                                예약 카드 하단 버튼 영역

                                예약 내역 보기:
                                - 모든 예약 상태에서 볼 수 있다.

                                도착 인증:
                                - 예약완료 상태이고 예약 시작 10분 전부터 가능하다.
                                - POST /reservation/auth-code/issue로 Redis 인증코드를 발급한다.

                                예약 취소:
                                - 예약완료 상태일 때만 가능하다.
                                - POST /reservation/cancel로 요청한다.
                            -->
                            <div class="reservation-card-actions">

                                <a href="${pageContext.request.contextPath}/reservation/complete?reservationId=${reservation.reservationId}"
                                   class="outline-action">
                                    예약 내역 보기
                                </a>

                                <c:choose>
                                    <c:when test="${reservation.status == '예약완료'}">

                                        <div class="reservation-action-group">

                                            <c:choose>
                                                <c:when test="${reservation.verifyAvailable}">

                                                    <!--
                                                        도착 인증

                                                        예약 시작 10분 전부터 활성화된다.
                                                        버튼을 누르면 Redis에 충전기별 인증코드를 발급한다.
                                                    -->
                                                    <form action="${pageContext.request.contextPath}/reservation/auth-code/issue"
                                                          method="post"
                                                          class="inline-action-form"
                                                          onsubmit="return confirm('현장 인증코드를 발급하시겠습니까?');">

                                                        <input type="hidden"
                                                               name="reservationId"
                                                               value="${reservation.reservationId}">

                                                        <c:if test="${not empty _csrf}">
                                                            <input type="hidden"
                                                                   name="${_csrf.parameterName}"
                                                                   value="${_csrf.token}">
                                                        </c:if>

                                                        <button type="submit" class="arrival-action">
                                                            도착 인증
                                                        </button>
                                                    </form>

                                                </c:when>

                                                <c:otherwise>
                                                    <button type="button" class="wait-action" disabled>
                                                        인증 대기
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>

                                            <!-- 예약 취소 -->
                                            <form action="${pageContext.request.contextPath}/reservation/cancel"
                                                  method="post"
                                                  class="inline-action-form"
                                                  onsubmit="return confirm('예약을 취소하시겠습니까?');">

                                                <input type="hidden"
                                                       name="reservationId"
                                                       value="${reservation.reservationId}">

                                                <c:if test="${not empty _csrf}">
                                                    <input type="hidden"
                                                           name="${_csrf.parameterName}"
                                                           value="${_csrf.token}">
                                                </c:if>

                                                <button type="submit" class="cancel-action">
                                                    예약 취소
                                                </button>
                                            </form>

                                        </div>

                                    </c:when>

                                    <c:when test="${reservation.status == '인증완료'}">
                                        <span class="disabled-action">
                                            인증 완료된 예약입니다
                                        </span>
                                    </c:when>

                                    <c:when test="${reservation.status == '충전중'}">
                                        <span class="disabled-action">
                                            충전중인 예약입니다
                                        </span>
                                    </c:when>

                                    <c:when test="${reservation.status == '완료'}">
                                        <span class="disabled-action">
                                            충전 완료
                                        </span>
                                    </c:when>

                                    <c:when test="${reservation.status == '취소'}">
                                        <span class="disabled-action">
                                            취소된 예약입니다
                                        </span>
                                    </c:when>

                                    <c:when test="${reservation.status == '노쇼'}">
                                        <span class="disabled-action">
                                            노쇼 처리된 예약입니다
                                        </span>
                                    </c:when>
                                </c:choose>

                            </div>

                        </article>

                    </c:forEach>
                </c:otherwise>
            </c:choose>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

<!-- 예약 인증 / 예약 취소 성공, 실패 메시지 -->
<c:if test="${not empty msg}">
    <script>
        alert("${msg}");
    </script>
</c:if>

<c:if test="${not empty errorMsg}">
    <script>
        alert("${errorMsg}");
    </script>
</c:if>

<!-- Redis 인증코드 발급 후 표시되는 모달 -->
<c:if test="${not empty issuedAuthCode}">
    <div class="auth-modal-backdrop" id="authModal">
        <div class="auth-modal">

            <h2>현장 인증</h2>

            <p class="auth-modal-desc">
                Redis에 인증코드가 발급되었습니다.<br>
                5분 안에 아래 코드를 입력해서 예약 인증을 완료해주세요.
            </p>

            <div class="issued-code-box">
                <span>발급된 인증코드</span>
                <strong>${issuedAuthCode}</strong>
            </div>

            <form action="${pageContext.request.contextPath}/reservation/verify"
                  method="post"
                  class="auth-modal-form">

                <input type="hidden"
                       name="reservationId"
                       value="${issuedReservationId}">

                <input type="text"
                       name="authCode"
                       class="auth-modal-input"
                       placeholder="인증코드 6자리 입력"
                       maxlength="6"
                       required>

                <c:if test="${not empty _csrf}">
                    <input type="hidden"
                           name="${_csrf.parameterName}"
                           value="${_csrf.token}">
                </c:if>

                <div class="auth-modal-actions">
                    <button type="button"
                            class="auth-modal-close"
                            onclick="document.getElementById('authModal').style.display='none';">
                        닫기
                    </button>

                    <button type="submit" class="auth-modal-submit">
                        인증하기
                    </button>
                </div>

            </form>

        </div>
    </div>
</c:if>

<script>
    const filterTabs = document.querySelectorAll(".filter-tab");
    const reservationCards = document.querySelectorAll(".my-reservation-card");

    /*
     * 상태 필터
     *
     * 현재 화면에 렌더링된 카드들을
     * 프론트에서 숨김/표시 처리한다.
     */
    filterTabs.forEach(function(tab) {
        tab.addEventListener("click", function() {
            const selectedStatus = tab.dataset.status;

            filterTabs.forEach(function(item) {
                item.classList.remove("active");
            });

            tab.classList.add("active");

            reservationCards.forEach(function(card) {
                const cardStatus = card.dataset.status;

                if (selectedStatus === "전체" || selectedStatus === cardStatus) {
                    card.style.display = "";
                } else {
                    card.style.display = "none";
                }
            });
        });
    });
</script>

</body>
</html>