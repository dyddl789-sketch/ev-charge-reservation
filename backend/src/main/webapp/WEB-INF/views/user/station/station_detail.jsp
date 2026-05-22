<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 상세</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/station/station_detail.css"></head>
<body>

<div class="detail-page">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

    

    <main class="detail-main">

        <!-- 뒤로가기 -->
        <div class="back-row">
            <a href="/station/list">‹ 충전소 목록으로</a>
        </div>

        <!-- 상단 상세 정보 -->
        <section class="station-summary">

            <div class="summary-left">
                <span class="badge">추천 충전소</span>
                <h1>부산 사상 EV 충전소</h1>
                <p class="address">부산 사상구 괘법동 518-1</p>

                <div class="status-row">
                    <span class="status available">운영중</span>
                    <span class="status fast">급속 충전</span>
                    <span class="status slow">완속 충전</span>
                </div>
            </div>

            <div class="summary-right">
                <div class="distance-box">
                    <span>현재 위치 기준</span>
                    <strong>1.2km</strong>
                </div>

                <a href="/reservation/form" class="reserve-main-btn">예약하기</a>
            </div>

        </section>

        <!-- 요약 카드 -->
        <section class="info-cards">

            <div class="info-card">
                <span>전체 충전기</span>
                <strong>8대</strong>
            </div>

            <div class="info-card">
                <span>사용 가능</span>
                <strong class="green">4대</strong>
            </div>

            <div class="info-card">
                <span>최저 요금</span>
                <strong>250원/kWh</strong>
            </div>

            <div class="info-card">
                <span>운영 시간</span>
                <strong>24시간</strong>
            </div>

        </section>

        <!-- 본문 레이아웃 -->
        <section class="detail-layout">

            <!-- 왼쪽 영역 -->
            <section class="left-area">

                <!-- 지도 -->
                <div class="section-box">
                    <div class="section-header">
                        <h2>위치 정보</h2>
                        <button type="button">길찾기</button>
                    </div>

                    <div class="map-box">
                        <div class="map-grid"></div>
                        <div class="station-main-marker">⚡</div>
                        <span class="map-label label-1">사상구</span>
                        <span class="map-label label-2">부산역</span>
                    </div>

                    <div class="address-info">
                        <p><strong>주소</strong> 부산 사상구 괘법동 518-1</p>
                        <p><strong>좌표</strong> 위도 35.152300 / 경도 128.991200</p>
                    </div>
                </div>

                <!-- 충전기 목록 -->
                <div class="section-box">
                    <div class="section-header">
                        <h2>충전기 현황</h2>
                        <span>실시간 기준</span>
                    </div>

                    <table class="charger-table">
                        <thead>
                            <tr>
                                <th>충전기</th>
                                <th>타입</th>
                                <th>속도</th>
                                <th>상태</th>
                                <th>요금</th>
                                <th>예약</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1번</td>
                                <td>DC콤보</td>
                                <td>급속</td>
                                <td><span class="table-status available">사용 가능</span></td>
                                <td>250원/kWh</td>
                                <td><a href="/reservation/form" class="table-btn">예약</a></td>
                            </tr>
                            <tr>
                                <td>2번</td>
                                <td>DC콤보</td>
                                <td>급속</td>
                                <td><span class="table-status using">사용중</span></td>
                                <td>250원/kWh</td>
                                <td><span class="disabled-text">불가</span></td>
                            </tr>
                            <tr>
                                <td>3번</td>
                                <td>AC완속</td>
                                <td>완속</td>
                                <td><span class="table-status available">사용 가능</span></td>
                                <td>220원/kWh</td>
                                <td><a href="/reservation/form" class="table-btn">예약</a></td>
                            </tr>
                            <tr>
                                <td>4번</td>
                                <td>DC콤보</td>
                                <td>급속</td>
                                <td><span class="table-status check">점검중</span></td>
                                <td>250원/kWh</td>
                                <td><span class="disabled-text">불가</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

            </section>

            <!-- 오른쪽 영역 -->
            <aside class="right-area">

                <!-- 운영 정보 -->
                <div class="side-box">
                    <h2>운영 정보</h2>

                    <div class="info-list">
                        <div>
                            <span>운영 상태</span>
                            <strong>운영중</strong>
                        </div>
                        <div>
                            <span>운영 시간</span>
                            <strong>24시간</strong>
                        </div>
                        <div>
                            <span>주차 요금</span>
                            <strong>무료</strong>
                        </div>
                        <div>
                            <span>관리 기관</span>
                            <strong>EV Charge</strong>
                        </div>
                        <div>
                            <span>문의 전화</span>
                            <strong>051-123-4567</strong>
                        </div>
                    </div>
                </div>

                

                <!-- 빠른 예약 -->
                <div class="side-box">
                    <h2>빠른 예약</h2>

                    <div class="quick-reserve">
                        <label>예약 날짜</label>
                        <input type="date">

                        <label>예약 시간</label>
                        <select>
                            <option>18:00 ~ 19:00</option>
                            <option>19:00 ~ 20:00</option>
                            <option>20:00 ~ 21:00</option>
                        </select>

                        <label>충전기 선택</label>
                        <select>
                            <option>1번 DC콤보</option>
                            <option>3번 AC완속</option>
                        </select>

                        <a href="/reservation/form" class="quick-btn">예약 화면으로 이동</a>
                    </div>
                </div>

            </aside>

        </section>

    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</div>

</body>
</html>