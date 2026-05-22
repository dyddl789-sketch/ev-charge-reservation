<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 탐색</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/station/station_list.css">
</head>
<body>

<div class="station-search-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="station-layout">

        <!-- 왼쪽 패널 -->
        <aside class="left-panel">

            <section class="left-card">
                <div class="left-card-header">
                    <h2>저장 위치</h2>
                    <a href="#">+ 위치 추가</a>
                </div>

                <div class="location-list">
                    <button type="button" class="location-item active">
                        <span class="location-icon home">⌂</span>
                        <div>
                            <strong>집</strong>
                            <p>부산 사상구 괘법동</p>
                        </div>
                    </button>

                    <button type="button" class="location-item">
                        <span class="location-icon office">▣</span>
                        <div>
                            <strong>회사</strong>
                            <p>부산 부산진구 중앙대로</p>
                        </div>
                    </button>

                    <button type="button" class="location-item">
                        <span class="location-icon recent">⌖</span>
                        <div>
                            <strong>최근 위치</strong>
                            <p>부산 동구 부산역 인근</p>
                        </div>
                    </button>
                </div>
            </section>

            <section class="left-card">
                <div class="left-card-header">
                    <h2>즐겨찾기</h2>
                    <a href="/favorite/list">전체 보기 ›</a>
                </div>

                <div class="favorite-list">
                    <a href="/station/detail" class="favorite-item">
                        <span class="favorite-icon orange">⚡</span>
                        <div>
                            <strong>부산 사상 EV 충전소</strong>
                            <p>1.2km</p>
                        </div>
                    </a>

                    <a href="/station/detail" class="favorite-item">
                        <span class="favorite-icon green">⚡</span>
                        <div>
                            <strong>서면 EV 스테이션</strong>
                            <p>2.3km</p>
                        </div>
                    </a>

                    <a href="/station/detail" class="favorite-item">
                        <span class="favorite-icon blue">⚡</span>
                        <div>
                            <strong>부산역 환승센터</strong>
                            <p>2.8km</p>
                        </div>
                    </a>
                </div>
            </section>

        </aside>

        <!-- 중앙 지도 영역 -->
        <section class="map-section">

            <!-- 검색 / 필터 -->
            <section class="search-toolbar">

                <div class="search-input-box">
                    <input type="text" placeholder="지역, 충전소명 검색">
                    <button type="button">🔍</button>
                </div>

                <div class="filter-group">
                    <button type="button" class="filter-btn active">충전기 타입 전체</button>
                    <button type="button" class="filter-btn">급속</button>
                    <button type="button" class="filter-btn">완속</button>
                    <button type="button" class="filter-btn">초급속</button>
                    <button type="button" class="filter-btn">커넥터 전체</button>
                    <button type="button" class="detail-filter-btn">상세 필터</button>
                </div>

            </section>

            <!-- 지도 -->
            <section class="map-area">
                <div class="map-bg"></div>

                <div class="current-location"></div>

                <button type="button" class="station-marker marker-1 green">2</button>
                <button type="button" class="station-marker marker-2 orange">⚡</button>
                <button type="button" class="station-marker marker-3 green">2</button>
                <button type="button" class="station-marker marker-4 green">0</button>
                <button type="button" class="station-marker marker-5 green">5</button>
                <button type="button" class="station-marker marker-6 orange">⚡</button>
                <button type="button" class="station-marker marker-7 green">2</button>
                <button type="button" class="station-marker marker-8 green">0</button>

                <div class="map-control current">⌖</div>

                <div class="map-control zoom">
                    <button type="button">+</button>
                    <button type="button">−</button>
                </div>

                <button type="button" class="list-view-btn">☰ 목록 보기</button>
            </section>

        </section>

        <!-- 오른쪽 상세 패널 -->
        <aside class="detail-panel">

            <div class="panel-header">
    			<div>
        			<h1>부산 사상 EV 충전소</h1>
        			<button type="button" class="station-favorite-star" id="favoriteStar" data-station-id="1">
            			☆
        			</button>
    			</div>
    				<button type="button" class="panel-close-btn">×</button>
			</div>

            <div class="station-address">
                <p>⌖ 부산 사상구 괘법동 518-1</p>
                <p>1.2km · 24시간 운영 · <span>운영 중</span></p>
            </div>

            <div class="tag-row">
                <span>DC콤보</span>
                <span>급속 2대</span>
                <span>완속 1대</span>
                <span>주차 가능</span>
            </div>

            <div class="action-row">
                <a href="#" class="route-btn">길찾기</a>
                <a href="/reservation/form" class="reserve-btn">예약하기</a>
            </div>

            <div class="tab-row">
                <button type="button" class="active">충전기 정보</button>
                <button type="button">이용 안내</button>
            </div>

            <section class="charger-list">

                <article class="charger-item">
                    <div class="charger-left">
                        <span class="charger-icon green">1</span>
                        <div>
                            <strong>급속 1번</strong>
                            <p>200kW · DC콤보</p>
                        </div>
                    </div>

                    <div class="charger-right">
                        <strong class="available">사용 가능</strong>
                        <p>250원/kWh</p>
                    </div>
                </article>

                <article class="charger-item">
                    <div class="charger-left">
                        <span class="charger-icon blue">2</span>
                        <div>
                            <strong>급속 2번</strong>
                            <p>200kW · DC콤보</p>
                        </div>
                    </div>

                    <div class="charger-right">
                        <strong class="available">사용 가능</strong>
                        <p>250원/kWh</p>
                    </div>
                </article>

                <article class="charger-item">
                    <div class="charger-left">
                        <span class="charger-icon blue">3</span>
                        <div>
                            <strong>완속 1번</strong>
                            <p>7kW · AC완속</p>
                        </div>
                    </div>

                    <div class="charger-right">
                        <strong class="available">사용 가능</strong>
                        <p>200원/kWh</p>
                    </div>
                </article>

            </section>

			<button type="button" class="favorite-add-btn" id="favoriteBtn" data-station-id="1">
    			♡ 즐겨찾기 추가
			</button>
        </aside>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

<script>
    const filterButtons = document.querySelectorAll(".filter-btn");

    filterButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            filterButtons.forEach(function(btn) {
                btn.classList.remove("active");
            });

            button.classList.add("active");
        });
    });

    const locationItems = document.querySelectorAll(".location-item");

    locationItems.forEach(function(item) {
        item.addEventListener("click", function() {
            locationItems.forEach(function(btn) {
                btn.classList.remove("active");
            });

            item.classList.add("active");
        });
    });

    // 즐겨찾기 추가 / 해제 임시 처리
    const favoriteBtn = document.getElementById("favoriteBtn");
    const favoriteStar = document.getElementById("favoriteStar");

    let isFavorite = false;

    function updateFavoriteUI() {
        if (isFavorite) {
            favoriteBtn.classList.add("active");
            favoriteBtn.textContent = "♥ 즐겨찾기 해제";
            favoriteStar.classList.add("active");
            favoriteStar.textContent = "★";
        } else {
            favoriteBtn.classList.remove("active");
            favoriteBtn.textContent = "♡ 즐겨찾기 추가";
            favoriteStar.classList.remove("active");
            favoriteStar.textContent = "☆";
        }
    }

    favoriteBtn.addEventListener("click", function() {
        isFavorite = !isFavorite;
        updateFavoriteUI();

        // 나중에 DB 연결 시 여기에서 Ajax로 favorite_station insert/delete 처리
        // POST /favorite/toggle
        console.log("stationId =>", favoriteBtn.dataset.stationId);
        console.log("isFavorite =>", isFavorite);
    });

    favoriteStar.addEventListener("click", function() {
        isFavorite = !isFavorite;
        updateFavoriteUI();

        console.log("stationId =>", favoriteStar.dataset.stationId);
        console.log("isFavorite =>", isFavorite);
    });
</script>

</body>
</html>