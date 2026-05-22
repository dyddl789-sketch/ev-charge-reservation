<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 목록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/station.css">
</head>
<body>

<div class="admin-page">
    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>
    <div class="admin-layout">
        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <!-- 본문 -->
        <main class="admin-content">

            <section class="admin-title-row">
                <div>
                    <h1>충전소 목록</h1>
                    <p>등록된 충전소 정보를 확인하고 운영 상태를 관리합니다.</p>
                </div>

                <a href="/admin/station/manage" class="add-station-btn">+ 충전소 등록</a>
            </section>

            <!-- 요약 카드 -->
            <section class="station-summary">

                <article class="station-summary-card">
                    <div>
                        <span>전체 충전소</span>
                        <strong>156개</strong>
                    </div>
                </article>

                <article class="station-summary-card">
                    <div>
                        <span>운영중</span>
                        <strong>148개</strong>
                    </div>
                </article>

                <article class="station-summary-card">
                    <div>
                        <span>점검중</span>
                        <strong>8개</strong>
                    </div>
                </article>

            </section>

            <!-- 검색 영역 -->
            <section class="station-search-card">

                <div class="search-row">
                    <div class="search-group">
                        <label for="region">지역</label>
                        <select id="region">
                            <option value="">전체</option>
                            <option value="BUSAN">부산</option>
                            <option value="SEOUL">서울</option>
                            <option value="DAEGU">대구</option>
                            <option value="INCHEON">인천</option>
                        </select>
                    </div>

                    <div class="search-group">
                        <label for="status">운영 상태</label>
                        <select id="status">
                            <option value="">전체</option>
                            <option value="ACTIVE">운영중</option>
                            <option value="CHECK">점검중</option>
                            <option value="CLOSED">운영중지</option>
                        </select>
                    </div>

                    <div class="search-group">
                        <label for="chargerType">충전 타입</label>
                        <select id="chargerType">
                            <option value="">전체</option>
                            <option value="FAST">급속</option>
                            <option value="SLOW">완속</option>
                            <option value="BOTH">급속/완속</option>
                        </select>
                    </div>

                    <div class="search-actions">
                        <button type="button" class="reset-btn">초기화</button>
                    </div>
                </div>

                <div class="search-row second">
                    <div class="search-group">
                        <label for="searchType">검색 기준</label>
                        <select id="searchType">
                            <option value="stationName">충전소명</option>
                            <option value="address">주소</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <input type="text" placeholder="충전소명 또는 주소를 입력하세요.">
                        <button type="button">검색</button>
                    </div>
                </div>

            </section>

            <!-- 충전소 목록 테이블 -->
            <section class="station-table-card">

                <div class="table-top">
                    <p>총 <strong>156건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="station-table">
                    <thead>
                        <tr>
                            <th><input type="checkbox"></th>
                            <th>번호</th>
                            <th>충전소명</th>
                            <th>주소</th>
                            <th>충전기 수</th>
                            <th>사용 가능</th>
                            <th>충전 타입</th>
                            <th>운영 상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td><input type="checkbox"></td>
                            <td>156</td>
                            <td class="station-name">부산 사상 EV 충전소</td>
                            <td>부산 사상구 괘법동 518-1</td>
                            <td>12대</td>
                            <td>8대</td>
                            <td>
                                <span class="type-badge fast">급속</span>
                                <span class="type-badge slow">완속</span>
                            </td>
                            <td><span class="status-badge active">운영중</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="edit-btn">수정</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>155</td>
                            <td class="station-name">서면 EV 스테이션</td>
                            <td>부산 부산진구 중앙대로 672</td>
                            <td>10대</td>
                            <td>6대</td>
                            <td>
                                <span class="type-badge fast">급속</span>
                            </td>
                            <td><span class="status-badge active">운영중</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="edit-btn">수정</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>154</td>
                            <td class="station-name">부산역 환승센터</td>
                            <td>부산 동구 중앙대로 206</td>
                            <td>8대</td>
                            <td>3대</td>
                            <td>
                                <span class="type-badge fast">급속</span>
                                <span class="type-badge slow">완속</span>
                            </td>
                            <td><span class="status-badge check">점검중</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="edit-btn">수정</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>153</td>
                            <td class="station-name">해운대 센텀 충전소</td>
                            <td>부산 해운대구 센텀중앙로 55</td>
                            <td>10대</td>
                            <td>6대</td>
                            <td>
                                <span class="type-badge fast">급속</span>
                            </td>
                            <td><span class="status-badge active">운영중</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="edit-btn">수정</button>
                            </td>
                        </tr>

                        <tr>
                            <td><input type="checkbox"></td>
                            <td>152</td>
                            <td class="station-name">동래구 공영 충전소</td>
                            <td>부산 동래구 명륜동 530-1</td>
                            <td>6대</td>
                            <td>3대</td>
                            <td>
                                <span class="type-badge slow">완속</span>
                            </td>
                            <td><span class="status-badge active">운영중</span></td>
                            <td>
                                <button type="button" class="detail-btn">상세보기</button>
                                <button type="button" class="edit-btn">수정</button>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination">
                    <button type="button">«</button>
                    <button type="button">‹</button>
                    <button type="button" class="active">1</button>
                    <button type="button">2</button>
                    <button type="button">3</button>
                    <button type="button">4</button>
                    <button type="button">5</button>
                    <button type="button">›</button>
                    <button type="button">»</button>
                </div>

            </section>

        </main>

    </div>

</div>

</body>
</html>