<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소별 이용 순위</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/usage_list.css">
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
                    <h1>충전소별 이용 순위</h1>
                    <p>기간별 충전소 이용 건수, 충전량, 평균 충전 시간, 가동률을 확인할 수 있습니다.</p>
                </div>

                <form action="/admin/stat/usage/station" method="get" class="date-box">
                    <label>기간</label>
                    <input type="date" name="startDate" value="2026-05-01">
                    <span>~</span>
                    <input type="date" name="endDate" value="2026-05-31">
                </form>
            </section>

            <!-- 검색 영역 -->
            <section class="usage-search-card">

                <div class="search-row">

                    <div class="search-group">
                        <label>지역</label>
                        <select name="region">
                            <option value="">전체</option>
                            <option value="busan">부산</option>
                            <option value="seoul">서울</option>
                            <option value="daegu">대구</option>
                        </select>
                    </div>

                    <div class="search-group">
                        <label>충전 타입</label>
                        <select name="chargerType">
                            <option value="">전체</option>
                            <option value="급속">급속</option>
                            <option value="완속">완속</option>
                            <option value="초급속">초급속</option>
                        </select>
                    </div>

                    <div class="search-group">
                        <label>정렬 기준</label>
                        <select name="sort">
                            <option value="usage">이용 건수 높은순</option>
                            <option value="kwh">충전량 높은순</option>
                            <option value="rate">가동률 높은순</option>
                            <option value="time">평균 시간 높은순</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <label>검색어</label>
                        <div>
                            <input type="text" name="keyword" placeholder="충전소명을 입력하세요.">
                            <button type="submit">검색</button>
                        </div>
                    </div>

                    <button type="button" class="reset-btn">초기화</button>

                </div>

            </section>

            <!-- 요약 카드 -->
            <section class="usage-summary-row">

                <article>
                    <span>총 충전소 수</span>
                    <strong>24곳</strong>
                </article>

                <article>
                    <span>총 이용 건수</span>
                    <strong>8,962건</strong>
                </article>

                <article>
                    <span>총 충전량</span>
                    <strong>18,420kWh</strong>
                </article>

                <article>
                    <span>평균 가동률</span>
                    <strong>58%</strong>
                </article>

            </section>

            <!-- 테이블 -->
            <section class="usage-table-card">

                <div class="table-top">
                    <p>총 <strong>24건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="usage-list-table">
                    <thead>
                        <tr>
                            <th>순위</th>
                            <th>충전소명</th>
                            <th>지역</th>
                            <th>이용 건수</th>
                            <th>총 충전량</th>
                            <th>평균 충전 시간</th>
                            <th>가동률</th>
                            <th>상세</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td><span class="rank-badge top">1</span></td>
                            <td class="station-name">부산 사상 EV 충전소</td>
                            <td>부산광역시 사상구</td>
                            <td>1,284건</td>
                            <td>2,615kWh</td>
                            <td>26분</td>
                            <td class="point-text">82%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge top">2</span></td>
                            <td class="station-name">서면 EV 스테이션</td>
                            <td>부산광역시 부산진구</td>
                            <td>1,106건</td>
                            <td>2,201kWh</td>
                            <td>28분</td>
                            <td class="point-text">78%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge top">3</span></td>
                            <td class="station-name">부산역 환승센터</td>
                            <td>부산광역시 동구</td>
                            <td>986건</td>
                            <td>1,892kWh</td>
                            <td>30분</td>
                            <td class="point-text">74%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">4</span></td>
                            <td class="station-name">해운대 센텀 충전소</td>
                            <td>부산광역시 해운대구</td>
                            <td>852건</td>
                            <td>1,712kWh</td>
                            <td>27분</td>
                            <td>69%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">5</span></td>
                            <td class="station-name">수영강변 공영 충전소</td>
                            <td>부산광역시 수영구</td>
                            <td>774건</td>
                            <td>1,458kWh</td>
                            <td>29분</td>
                            <td>66%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">6</span></td>
                            <td class="station-name">동래 온천장 충전소</td>
                            <td>부산광역시 동래구</td>
                            <td>642건</td>
                            <td>1,231kWh</td>
                            <td>32분</td>
                            <td>61%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">7</span></td>
                            <td class="station-name">기장 휴게소 충전소</td>
                            <td>부산광역시 기장군</td>
                            <td>538건</td>
                            <td>1,098kWh</td>
                            <td>31분</td>
                            <td>59%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">8</span></td>
                            <td class="station-name">남포동 공영 주차장</td>
                            <td>부산광역시 중구</td>
                            <td>486건</td>
                            <td>942kWh</td>
                            <td>30분</td>
                            <td>54%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">9</span></td>
                            <td class="station-name">김해공항 주차장</td>
                            <td>부산광역시 강서구</td>
                            <td>452건</td>
                            <td>865kWh</td>
                            <td>29분</td>
                            <td>52%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">10</span></td>
                            <td class="station-name">광안리 해변 주차장</td>
                            <td>부산광역시 수영구</td>
                            <td>418건</td>
                            <td>798kWh</td>
                            <td>28분</td>
                            <td>49%</td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination">
                    <button type="button">‹</button>
                    <button type="button" class="active">1</button>
                    <button type="button">2</button>
                    <button type="button">3</button>
                    <button type="button">›</button>
                </div>

            </section>

        </main>

    </div>

</div>

</body>
</html>