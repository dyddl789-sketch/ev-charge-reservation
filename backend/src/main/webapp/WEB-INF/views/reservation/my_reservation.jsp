<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

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
                <p>예약한 충전 내역과 실제 충전 진행 상태를 확인할 수 있습니다.</p>
            </div>

            <a href="/station/list" class="back-btn">충전소 탐색</a>
        </section>

        <!-- 예약 요약 -->
        <section class="my-summary-grid">

            <article class="my-summary-card">
                <span>전체 예약</span>
                <strong>5건</strong>
            </article>

            <article class="my-summary-card">
                <span>예약 완료</span>
                <strong>2건</strong>
            </article>

            <article class="my-summary-card">
                <span>충전 완료</span>
                <strong>2건</strong>
            </article>

            <article class="my-summary-card">
                <span>이번 달 실제 비용</span>
                <strong>42,500원</strong>
            </article>

        </section>

        <!-- 예약 필터 -->
        <section class="reservation-filter-card">
            <div class="filter-left">
                <button type="button" class="filter-tab active">전체</button>
                <button type="button" class="filter-tab">예약완료</button>
                <button type="button" class="filter-tab">사용중</button>
                <button type="button" class="filter-tab">완료</button>
                <button type="button" class="filter-tab">취소</button>
            </div>

            <div class="filter-right">
                <input type="month">
            </div>
        </section>

        <!-- 예약 목록 -->
        <section class="my-reservation-list">

            <!-- 예약완료 -->
            <article class="my-reservation-card">

                <div class="reservation-card-header">
                    <div>
                        <span class="status-badge reserved">예약완료</span>
                        <h2>부산 사상 EV 충전소</h2>
                        <p>부산 사상구 괘법동 518-1</p>
                    </div>

                    <div class="reservation-date-box">
                        <strong>오늘</strong>
                        <span>18:00 ~ 19:00</span>
                    </div>
                </div>

                <div class="reservation-info-grid">

                    <div>
                        <span>예약 차량</span>
                        <strong>아이오닉 5</strong>
                    </div>

                    <div>
                        <span>충전기</span>
                        <strong>급속 1번</strong>
                    </div>

                    <div>
                        <span>배터리</span>
                        <strong>30% → 80%</strong>
                    </div>

                    <div>
                        <span>예상 충전량</span>
                        <strong>38.70kWh</strong>
                    </div>

                    <div>
                        <span>예상 시간</span>
                        <strong>24분</strong>
                    </div>

                    <div>
                        <span>예상 비용</span>
                        <strong>9,675원</strong>
                    </div>

                </div>

                <div class="reservation-card-actions">
                    <a href="/station/detail" class="outline-action">충전소 상세</a>
                    <button type="button" class="outline-action">예약 변경</button>
                    <button type="button" class="cancel-action">예약 취소</button>
                </div>

            </article>

            <!-- 사용중 -->
            <article class="my-reservation-card active-session">

                <div class="reservation-card-header">
                    <div>
                        <span class="status-badge using">사용중</span>
                        <h2>서면 EV 스테이션</h2>
                        <p>부산 부산진구 중앙대로 672</p>
                    </div>

                    <div class="reservation-date-box">
                        <strong>진행중</strong>
                        <span>16:00 ~ 충전중</span>
                    </div>
                </div>

                <div class="reservation-info-grid">

                    <div>
                        <span>예약 차량</span>
                        <strong>EV6</strong>
                    </div>

                    <div>
                        <span>충전기</span>
                        <strong>급속 2번</strong>
                    </div>

                    <div>
                        <span>시작 SOC</span>
                        <strong>35%</strong>
                    </div>

                    <div>
                        <span>목표 SOC</span>
                        <strong>80%</strong>
                    </div>

                    <div>
                        <span>예상 시간</span>
                        <strong>28분</strong>
                    </div>

                    <div>
                        <span>상태</span>
                        <strong class="green-text">충전중</strong>
                    </div>

                </div>

                <div class="charging-progress">
                    <div class="progress-top">
                        <span>충전 진행률</span>
                        <strong>62%</strong>
                    </div>
                    <div class="progress-bar">
                        <span style="width: 62%;"></span>
                    </div>
                </div>

                <div class="reservation-card-actions">
                    <button type="button" class="outline-action">충전 상태 보기</button>
                    <button type="button" class="cancel-action">충전 중단</button>
                </div>

            </article>

            <!-- 완료 -->
            <article class="my-reservation-card">

                <div class="reservation-card-header">
                    <div>
                        <span class="status-badge completed">완료</span>
                        <h2>부산역 환승센터</h2>
                        <p>부산 동구 중앙대로 206</p>
                    </div>

                    <div class="reservation-date-box">
                        <strong>2026.05.18</strong>
                        <span>14:00 ~ 14:37</span>
                    </div>
                </div>

                <div class="reservation-info-grid">

                    <div>
                        <span>예약 차량</span>
                        <strong>아이오닉 5</strong>
                    </div>

                    <div>
                        <span>충전기</span>
                        <strong>완속 1번</strong>
                    </div>

                    <div>
                        <span>실제 SOC</span>
                        <strong>42% → 78%</strong>
                    </div>

                    <div>
                        <span>실제 충전량</span>
                        <strong>27.86kWh</strong>
                    </div>

                    <div>
                        <span>실제 시간</span>
                        <strong>37분</strong>
                    </div>

                    <div>
                        <span>실제 비용</span>
                        <strong>6,965원</strong>
                    </div>

                </div>

                <div class="compare-box">
                    <p>예상 비용 7,250원 / 실제 비용 6,965원</p>
                    <p>예상 시간 40분 / 실제 시간 37분</p>
                </div>

                <div class="reservation-card-actions">
                    <a href="/station/detail" class="outline-action">충전소 상세</a>
                </div>

            </article>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

<script>
    const filterTabs = document.querySelectorAll(".filter-tab");

    filterTabs.forEach(function(tab) {
        tab.addEventListener("click", function() {
            filterTabs.forEach(function(item) {
                item.classList.remove("active");
            });

            tab.classList.add("active");
        });
    });
</script>

</body>
</html>