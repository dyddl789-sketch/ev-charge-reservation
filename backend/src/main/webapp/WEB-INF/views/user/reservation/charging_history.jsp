<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전 내역</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/reservation.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/charging_history.css">
</head>
<body>

<div class="reservation-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="reservation-main">

        <section class="page-title">
            <div>
                <h1>충전 내역</h1>
                <p>충전이 완료된 이용 내역을 확인하고 영수증을 이메일로 받을 수 있습니다.</p>
            </div>

            <a href="${pageContext.request.contextPath}/reservation/my" class="back-btn">
                내 예약으로 이동
            </a>
        </section>

        <!--
            충전 내역 요약 계산

            현재는 실제 결제 테이블이 없으므로
            예약 시 계산된 estimatedCost 기준으로 합산한다.
        -->
        <c:set var="historyCount" value="0" />
        <c:set var="totalKwh" value="0" />
        <c:set var="totalMinutes" value="0" />
        <c:set var="totalCost" value="0" />

        <c:forEach var="history" items="${chargingHistoryList}">
            <c:set var="historyCount" value="${historyCount + 1}" />

            <c:if test="${history.requiredKwh != null}">
                <c:set var="totalKwh" value="${totalKwh + history.requiredKwh}" />
            </c:if>

            <c:if test="${history.estimatedMinutes != null}">
                <c:set var="totalMinutes" value="${totalMinutes + history.estimatedMinutes}" />
            </c:if>

            <c:if test="${history.estimatedCost != null}">
                <c:set var="totalCost" value="${totalCost + history.estimatedCost}" />
            </c:if>
        </c:forEach>

        <!-- 충전 내역 요약 -->
        <section class="charging-summary-grid">

            <article class="charging-summary-card">
                <span>총 충전 횟수</span>
                <strong>${historyCount}건</strong>
            </article>

            <article class="charging-summary-card">
                <span>총 충전량</span>
                <strong>
                    <fmt:formatNumber value="${totalKwh}" pattern="#,##0.00" />kWh
                </strong>
            </article>

            <article class="charging-summary-card">
                <span>총 충전 시간</span>
                <strong>${totalMinutes}분</strong>
            </article>

            <article class="charging-summary-card emphasis">
                <span>총 이용 금액</span>
                <strong>
                    <fmt:formatNumber value="${totalCost}" pattern="#,###" />원
                </strong>
            </article>

        </section>

        <!-- 충전 내역 필터 -->
        <section class="charging-filter-card">

            <div class="charging-filter-left">
                <a href="${pageContext.request.contextPath}/reservation/history?period=all"
				   class="charging-filter-tab ${empty selectedMonth and (empty selectedPeriod or selectedPeriod == 'all') ? 'active' : ''}">
				    전체
				</a>
				
            </div>

            <div class="charging-filter-right">

                <!--
                    월 선택 필터

                    month 값을 서버로 보내기 위해 form + name="month"가 필요하다.
                    월을 선택하면 /reservation/history?month=yyyy-MM 형태로 요청된다.
                -->
                <form action="${pageContext.request.contextPath}/reservation/history"
                      method="get"
                      class="charging-month-form">

                    <input type="month"
                           id="chargingMonthFilter"
                           name="month"
                           value="${selectedMonth}"
                           onchange="this.form.submit()">
                </form>

                <select id="chargingSort">
                    <option value="latest">최신순</option>
                    <option value="cost">금액 높은순</option>
                    <option value="kwh">충전량 높은순</option>
                </select>
            </div>

        </section>

        <!-- 충전 내역 목록 -->
        <section class="charging-history-list">

            <c:choose>
                <c:when test="${empty chargingHistoryList}">
                    <div class="empty-reservation-box">
                        <h2>충전 내역이 없습니다.</h2>
                        <p>충전이 완료된 예약이 있으면 이곳에서 확인할 수 있습니다.</p>

                        <a href="${pageContext.request.contextPath}/station/map"
                           class="complete-main-btn">
                            충전소 탐색하기
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <c:forEach var="history" items="${chargingHistoryList}">

                        <article class="charging-history-card"
                                 data-date="${history.reservationDate}"
                                 data-time="${history.startTime}"
                                 data-cost="${history.estimatedCost}"
                                 data-kwh="${history.requiredKwh}">

                            <div class="charging-history-header">

                                <div>
                                    <span class="charging-status-badge">
                                        충전 완료
                                    </span>

                                    <h2>${history.stationName}</h2>
                                    <p>${history.stationAddress}</p>
                                </div>

                                <div class="charging-date-box">
                                    <strong>${history.reservationDate}</strong>
                                    <span>
                                        ${history.startTimeText}
                                        ~
                                        ${history.endTimeText}
                                    </span>
                                </div>

                            </div>

                            <div class="charging-info-grid">

                                <div class="charging-info-item">
                                    <span>이용 차량</span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty history.vehicleNickname}">
                                                ${history.vehicleNickname} / ${history.modelName}
                                            </c:when>

                                            <c:otherwise>
                                                ${history.modelName}
                                            </c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>

                                <div class="charging-info-item">
                                    <span>충전기</span>
                                    <strong>
                                        ${history.chargerName} · ${history.connectorType}
                                    </strong>
                                </div>

                                <div class="charging-info-item">
                                    <span>배터리</span>
                                    <strong>
                                        ${history.currentSoc}% → ${history.targetSoc}%
                                    </strong>
                                </div>

                                <div class="charging-info-item">
                                    <span>충전량</span>
                                    <strong>
                                        <fmt:formatNumber value="${history.requiredKwh}"
                                                          pattern="#,##0.00" />kWh
                                    </strong>
                                </div>

                                <div class="charging-info-item">
                                    <span>충전 시간</span>
                                    <strong>${history.estimatedMinutes}분</strong>
                                </div>

                                <div class="charging-info-item">
                                    <span>이용 금액</span>
                                    <strong>
                                        <fmt:formatNumber value="${history.estimatedCost}"
                                                          pattern="#,###" />원
                                    </strong>
                                </div>

                            </div>

                            <div class="charging-card-bottom">

							    <span class="charging-receipt-note">
							        영수증은 회원 이메일로 발송할 수 있습니다.
							    </span>
							
							    <div class="charging-actions">
							
							        <form action="${pageContext.request.contextPath}/reservation/receipt/email"
							              method="post"
							              class="charging-receipt-form">
							
							            <input type="hidden"
							                   name="reservationId"
							                   value="${history.reservationId}">
							
							            <c:if test="${not empty _csrf}">
							                <input type="hidden"
							                       name="${_csrf.parameterName}"
							                       value="${_csrf.token}">
							            </c:if>
							
							            <button type="submit" class="charging-primary-btn">
							                영수증 발급
							            </button>
							
							        </form>
							
							    </div>
							
							</div>

                        </article>

                    </c:forEach>
                </c:otherwise>
            </c:choose>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

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

<script>
    /*
     * 충전 내역 정렬
     */
    const chargingSort = document.getElementById("chargingSort");
    const chargingHistoryList = document.querySelector(".charging-history-list");

    if (chargingSort && chargingHistoryList) {
        chargingSort.addEventListener("change", function() {
            const cards = Array.from(document.querySelectorAll(".charging-history-card"));
            const sortType = chargingSort.value;

            cards.sort(function(a, b) {

                /*
                 * 금액 높은순
                 */
                if (sortType === "cost") {
                    const costA = Number(a.dataset.cost || 0);
                    const costB = Number(b.dataset.cost || 0);

                    return costB - costA;
                }

                /*
                 * 충전량 높은순
                 */
                if (sortType === "kwh") {
                    const kwhA = Number(a.dataset.kwh || 0);
                    const kwhB = Number(b.dataset.kwh || 0);

                    return kwhB - kwhA;
                }

                /*
                 * 최신순
                 */
                const dateA = String(a.dataset.time || a.dataset.date || "");
                const dateB = String(b.dataset.time || b.dataset.date || "");

                return dateB.localeCompare(dateA);
            });

            /*
             * 정렬된 순서대로 다시 화면에 붙인다.
             */
            cards.forEach(function(card) {
                chargingHistoryList.appendChild(card);
            });
        });
    }
</script>
</body>
</html>