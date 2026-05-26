<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main/main.css">
</head>
<body>

<div class="main-page">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
    
    <main class="dashboard">

        <!-- 왼쪽 패널 -->
        <aside class="left-panel">

            <section class="welcome-card">
                <c:choose>
				    <c:when test="${not empty sessionScope.loginMemberName}">
				        <h2>${sessionScope.loginMemberName} 님, 안녕하세요!</h2>
				    </c:when>
				    <c:otherwise>
				        <h2>비회원 님, 안녕하세요!</h2>
				    </c:otherwise>
				</c:choose>
                <p>내 주변 최적의 충전소와 예약 정보를 확인해 보세요.</p>

                <div class="welcome-buttons">
                    <a href="/station/list" class="primary-btn">충전소 탐색하기</a>
                    <a href="/reservation/my" class="outline-btn">예약 관리하기</a>
                </div>
            </section>

            <section class="side-card vehicle-card">
                <div class="side-card-header">
                    <h2>내 차량</h2>
                    <a href="/vehicle/list">차량 관리 ›</a>
                </div>

                <div class="vehicle-info">
                    <div class="vehicle-img"></div>

                    <div class="vehicle-detail">
                        <strong>아이오닉 5</strong>
                        <p>77.4 kWh · DC콤보</p>
                        <span class="vehicle-tag">기본 차량</span>
                    </div>
                </div>

                <div class="battery-row">
                    <div class="battery-bar">
                        <span style="width: 68%;"></span>
                    </div>
                    <strong>배터리 68%</strong>
                </div>
            </section>

            <section class="side-card notice-card">
                <h2>이용 안내</h2>

                <ul>
                    <li>현재 위치 기준으로 가까운 충전소를 추천합니다.</li>
                    <li>차량 정보 등록 시 충전 타입이 자동 반영됩니다.</li>
                    <li>예약 전 예상 충전 비용을 확인할 수 있습니다.</li>
                </ul>
            </section>

        </aside>

        <!-- 중앙 컨텐츠 -->
        <section class="center-content">

            <!-- 요약 카드 -->
            <section class="summary-grid">

                <article class="summary-card">
                    <div class="summary-icon green">📍</div>
                    <div>
                        <span>내 주변 충전소</span>
                        <strong>24 곳</strong>
                        <p>반경 5km 기준</p>
                    </div>
                </article>

                <article class="summary-card">
                    <div class="summary-icon blue">🔌</div>
                    <div>
                        <span>사용 가능한 충전기</span>
                        <strong>32 대</strong>
                        <p>실시간 기준</p>
                    </div>
                </article>

                <article class="summary-card">
                    <div class="summary-icon purple">📅</div>
                    <div>
                        <span>다음 예약</span>
                        <strong>오늘 18:00</strong>
                        <p>부산 사상 EV 충전소</p>
                    </div>
                </article>

                <article class="summary-card">
                    <div class="summary-icon orange">₩</div>
                    <div>
                        <span>이번 달 충전 비용</span>
                        <strong>42,500 원</strong>
                        <p>전월 대비 -12%</p>
                    </div>
                </article>

            </section>

            <!-- 추천 충전소 -->
            <section class="content-card recommend-section">

                <div class="section-header">
                    <div>
                        <h2>추천 충전소</h2>
                        <p>현재 위치와 차량 정보를 기준으로 추천</p>
                    </div>
                    <a href="/station/list">전체 보기 ›</a>
                </div>

                <div class="station-grid">

                    <article class="station-card">
                        <div class="station-img img-1">
                            <span class="station-badge">추천</span>
                        </div>

                        <div class="station-body">
                            <div class="station-title-row">
                                <h3>부산 사상 EV 충전소</h3>
                                <span class="recommend-tag">추천</span>
                            </div>

                            <p class="station-address">부산 사상구 괘법동 518-1</p>

                            <div class="station-meta">
                                <span>1.2km</span>
                                <span>급속 2대</span>
                                <span>250원/kWh</span>
                            </div>

                            <div class="station-tags">
                                <span>DC콤보</span>
                                <span>사용 가능</span>
                                <span>가까움</span>
                            </div>

                            <div class="station-buttons">
                                <a href="/station/detail" class="detail-btn">상세보기</a>
                                <a href="/reservation/form" class="reserve-btn">예약하기</a>
                            </div>
                        </div>
                    </article>

                    <article class="station-card">
                        <div class="station-img img-2"></div>

                        <div class="station-body">
                            <div class="station-title-row">
                                <h3>서면 EV 스테이션</h3>
                                <span class="recommend-tag">추천</span>
                            </div>

                            <p class="station-address">부산 부산진구 중앙대로 672</p>

                            <div class="station-meta">
                                <span>2.3km</span>
                                <span>완속 2대</span>
                                <span>270원/kWh</span>
                            </div>

                            <div class="station-tags">
                                <span>DC콤보</span>
                                <span>사용 가능</span>
                                <span>빠른 충전</span>
                            </div>

                            <div class="station-buttons">
                                <a href="/station/detail" class="detail-btn">상세보기</a>
                                <a href="/reservation/form" class="reserve-btn">예약하기</a>
                            </div>
                        </div>
                    </article>

                    <article class="station-card">
                        <div class="station-img img-3"></div>

                        <div class="station-body">
                            <div class="station-title-row">
                                <h3>부산역 환승센터</h3>
                                <span class="recommend-tag">추천</span>
                            </div>

                            <p class="station-address">부산 동구 중앙대로 206</p>

                            <div class="station-meta">
                                <span>2.8km</span>
                                <span>완속 2대</span>
                                <span>290원/kWh</span>
                            </div>

                            <div class="station-tags">
                                <span>DC콤보</span>
                                <span>사용 가능</span>
                                <span>넓은 주차</span>
                            </div>

                            <div class="station-buttons">
                                <a href="/station/detail" class="detail-btn">상세보기</a>
                                <a href="/reservation/form" class="reserve-btn">예약하기</a>
                            </div>
                        </div>
                    </article>

                </div>

            </section>

        </section>

        <!-- 오른쪽 패널 -->
        <aside class="right-panel">

            <section class="right-card map-preview-card">
                <div class="right-card-header">
                    <h2>내 주변 지도</h2>
                </div>

                <div class="mini-map">
                    <div class="map-grid"></div>

                    <div class="map-user-marker"></div>
                    <div class="map-pin pin-1">6</div>
                    <div class="map-pin pin-2">4</div>
                    <div class="map-pin pin-3">5</div>
                    <div class="map-pin pin-4">7</div>
                    <div class="map-pin orange pin-5">⚡</div>
                </div>

                <a href="/station/list" class="map-link">지도에서 전체 보기 ›</a>
            </section>

            <section class="right-card reservation-card">
                <div class="right-card-header">
                    <h2>최근 예약 내역</h2>
                    <a href="/reservation/my">전체 보기 ›</a>
                </div>

                <div class="reservation-item">
                    <div class="reservation-icon">📅</div>

                    <div class="reservation-info">
                        <strong>부산 사상 EV 충전소</strong>
                        <p>오늘 18:00 ~ 19:00</p>
                    </div>

                    <span class="reservation-status">예약 확정</span>
                </div>
            </section>

            <section class="right-card favorite-card">
                <div class="right-card-header">
                    <h2>즐겨찾기</h2>
                    <a href="/favorite/list">전체 보기 ›</a>
                </div>

                <div class="favorite-list">
                    <div>
                        <span>⚡</span>
                        <p>
                            <strong>부산 사상 EV 충전소</strong>
                            <em>1.2km</em>
                        </p>
                    </div>

                    <div>
                        <span>⚡</span>
                        <p>
                            <strong>서면 EV 스테이션</strong>
                            <em>2.3km</em>
                        </p>
                    </div>

                    <div>
                        <span>⚡</span>
                        <p>
                            <strong>부산역 환승센터</strong>
                            <em>2.8km</em>
                        </p>
                    </div>
                </div>
            </section>

        </aside>

    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</div>

<script>
    const params = new URLSearchParams(window.location.search);
    const authMsg = params.get("authMsg");

    if (authMsg === "adminOnly") {
        alert("관리자만 접근할 수 있습니다.");
    }
</script>

</body>
</html>