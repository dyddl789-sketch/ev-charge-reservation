<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 최근 이용 내역</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/usage_history_list.css">
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
                    <h1>최근 이용 내역</h1>
                    <p>충전 완료된 이용 내역을 회원, 차량, 충전소 기준으로 확인할 수 있습니다.</p>
                </div>

                <form action="${pageContext.request.contextPath}/admin/stat/usage/history" method="get" class="date-box">
                    <label>기간</label>
                    <input type="date" name="startDate" value="2026-05-01">
                    <span>~</span>
                    <input type="date" name="endDate" value="2026-05-31">
                </form>
            </section>

            <!-- 검색 영역 -->
            <section class="usage-history-search-card">

                <form action="${pageContext.request.contextPath}/admin/stat/usage/history" method="get">

                    <div class="search-row">

                        <div class="search-group">
                            <label>검색 기준</label>
                            <select name="searchType">
                                <option value="all">전체</option>
                                <option value="memberName">회원명</option>
                                <option value="vehicleName">차량명</option>
                                <option value="stationName">충전소명</option>
                                <option value="chargerName">충전기명</option>
                            </select>
                        </div>

                        <div class="search-group">
                            <label>상태</label>
                            <select name="status">
                                <option value="">전체</option>
                                <option value="완료">완료</option>
                                <option value="충전중">충전중</option>
                                <option value="취소">취소</option>
                            </select>
                        </div>

                        <div class="search-group">
                            <label>충전 타입</label>
                            <select name="chargerType">
                                <option value="">전체</option>
                                <option value="완속">완속</option>
                                <option value="급속">급속</option>
                                <option value="초급속">초급속</option>
                            </select>
                        </div>

                        <div class="search-group date">
                            <label>시작일</label>
                            <input type="date" name="startDate" value="2026-05-01">
                        </div>

                        <span class="date-wave">~</span>

                        <div class="search-group date">
                            <label>종료일</label>
                            <input type="date" name="endDate" value="2026-05-31">
                        </div>

                    </div>

                    <div class="search-row second">

                        <div class="keyword-box">
                            <input type="text" name="keyword" placeholder="회원명, 차량명, 충전소명을 입력하세요.">
                            <button type="submit">검색</button>
                        </div>

                        <button type="button" class="reset-btn">초기화</button>

                    </div>

                </form>

            </section>

            <!-- 요약 카드 -->
            <section class="usage-history-summary">

                <article>
                    <span>총 이용 건수</span>
                    <strong>128건</strong>
                    <p>검색 조건 기준</p>
                </article>

                <article>
                    <span>완료 건수</span>
                    <strong>128건</strong>
                    <p>정상 충전 완료</p>
                </article>

                <article>
                    <span>평균 충전 시간</span>
                    <strong>27분</strong>
                    <p>건당 평균 이용 시간</p>
                </article>

                <article>
                    <span>총 충전량</span>
                    <strong>25,630kWh</strong>
                    <p>완료 내역 기준</p>
                </article>

            </section>

            <!-- 테이블 -->
            <section class="usage-history-table-card">

                <div class="table-top">
                    <p>총 <strong>128건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="usage-history-table">
                    <thead>
                        <tr>
                            <th>이용일시</th>
                            <th>회원명</th>
                            <th>차량</th>
                            <th>충전소명</th>
                            <th>충전기</th>
                            <th>충전 타입</th>
                            <th>충전 시간</th>
                            <th>충전량</th>
                            <th>상태</th>
                            <th>상세</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>2026-05-31 18:20</td>
                            <td>김민수</td>
                            <td>아이오닉 5</td>
                            <td class="station-name">부산 사상 EV 충전소</td>
                            <td>급속 1번</td>
                            <td>급속</td>
                            <td>24분</td>
                            <td>22.4kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 17:40</td>
                            <td>이소연</td>
                            <td>EV6</td>
                            <td class="station-name">서면 EV 스테이션</td>
                            <td>급속 2번</td>
                            <td>급속</td>
                            <td>28분</td>
                            <td>27.8kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 16:10</td>
                            <td>박정훈</td>
                            <td>테슬라 모델 3</td>
                            <td class="station-name">해운대 센텀 충전소</td>
                            <td>초급속 1번</td>
                            <td>초급속</td>
                            <td>18분</td>
                            <td>31.2kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 15:05</td>
                            <td>최지현</td>
                            <td>코나 EV</td>
                            <td class="station-name">수영강변 공영 충전소</td>
                            <td>급속 1번</td>
                            <td>급속</td>
                            <td>22분</td>
                            <td>20.6kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 14:20</td>
                            <td>정우진</td>
                            <td>아이오닉 5</td>
                            <td class="station-name">기장 오션뷰 충전소</td>
                            <td>급속 3번</td>
                            <td>급속</td>
                            <td>31분</td>
                            <td>30.5kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 13:15</td>
                            <td>한지수</td>
                            <td>EV6</td>
                            <td class="station-name">광안리 공영 충전소</td>
                            <td>급속 1번</td>
                            <td>급속</td>
                            <td>26분</td>
                            <td>24.6kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 12:05</td>
                            <td>오민혁</td>
                            <td>테슬라 모델 3</td>
                            <td class="station-name">남천 해변 충전소</td>
                            <td>초급속 2번</td>
                            <td>초급속</td>
                            <td>17분</td>
                            <td>33.8kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 11:10</td>
                            <td>이현아</td>
                            <td>코나 EV</td>
                            <td class="station-name">동래 롯데백화점 충전소</td>
                            <td>급속 2번</td>
                            <td>급속</td>
                            <td>23분</td>
                            <td>21.1kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 10:20</td>
                            <td>김태호</td>
                            <td>아이오닉 5</td>
                            <td class="station-name">북항 친수공원 충전소</td>
                            <td>급속 1번</td>
                            <td>급속</td>
                            <td>29분</td>
                            <td>27.3kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>

                        <tr>
                            <td>2026-05-31 09:30</td>
                            <td>유하린</td>
                            <td>EV6</td>
                            <td class="station-name">연산역 충전소</td>
                            <td>급속 2번</td>
                            <td>급속</td>
                            <td>25분</td>
                            <td>23.7kWh</td>
                            <td><span class="status-badge completed">완료</span></td>
                            <td><a href="#" class="detail-btn">상세보기</a></td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination">
                    <button type="button">‹</button>
                    <button type="button" class="active">1</button>
                    <button type="button">2</button>
                    <button type="button">3</button>
                    <button type="button">4</button>
                    <button type="button">5</button>
                    <span>...</span>
                    <button type="button">13</button>
                    <button type="button">›</button>
                </div>

            </section>

        </main>

    </div>

</div>

<script>
    const resetBtn = document.querySelector(".reset-btn");

    if (resetBtn) {
        resetBtn.addEventListener("click", function() {
            location.href = "${pageContext.request.contextPath}/admin/stat/usage/history";
        });
    }
</script>

</body>
</html>