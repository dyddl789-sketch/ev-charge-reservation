<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 이용 통계</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/usage.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

            <section class="admin-title-row">
                <div>
                    <h1>이용 통계</h1>
                    <p>충전 서비스 이용 건수와 충전소별 이용 현황을 확인할 수 있습니다.</p>
                </div>

                <form action="/admin/stat/usage" method="get" class="date-box">
                    <label>기간</label>
                    <input type="date" name="startDate" value="2026-05-01">
                    <span>~</span>
                    <input type="date" name="endDate" value="2026-05-31">
                </form>
            </section>

            <!-- 상단 요약 카드 -->
            <section class="usage-summary">

                <article class="usage-summary-card">
                    <span>전체 이용 건수</span>
                    <strong>8,962건</strong>
                    <p>전월 대비 +12.1%</p>
                </article>

                <article class="usage-summary-card">
                    <span>오늘 이용 건수</span>
                    <strong>342건</strong>
                    <p>전일 대비 +27건</p>
                </article>

                <article class="usage-summary-card">
                    <span>평균 충전 시간</span>
                    <strong>27분</strong>
                    <p>건당 평균 이용 시간</p>
                </article>

                <article class="usage-summary-card">
                    <span>총 충전량</span>
                    <strong>18,420kWh</strong>
                    <p>완료된 충전 기준</p>
                </article>

            </section>

            <!-- 차트 영역 -->
            <section class="usage-chart-grid">

                <article class="usage-card">
                    <div class="usage-card-header">
                        <h2>일별 이용 현황</h2>
                        <span>단위: 건</span>
                    </div>

                    <div class="chart-box">
                        <svg viewBox="0 0 640 220" class="usage-svg-chart">

                            <line x1="40" y1="30" x2="610" y2="30" class="grid-line" />
                            <line x1="40" y1="75" x2="610" y2="75" class="grid-line" />
                            <line x1="40" y1="120" x2="610" y2="120" class="grid-line" />
                            <line x1="40" y1="165" x2="610" y2="165" class="grid-line" />

                            <rect x="75"  y="96"  width="34" height="69" class="bar" />
                            <rect x="155" y="72"  width="34" height="93" class="bar" />
                            <rect x="235" y="110" width="34" height="55" class="bar" />
                            <rect x="315" y="86"  width="34" height="79" class="bar" />
                            <rect x="395" y="64"  width="34" height="101" class="bar" />
                            <rect x="475" y="42"  width="34" height="123" class="bar" />
                            <rect x="555" y="78"  width="34" height="87" class="bar" />

                            <text x="92"  y="198" class="x-label">5/25</text>
                            <text x="172" y="198" class="x-label">5/26</text>
                            <text x="252" y="198" class="x-label">5/27</text>
                            <text x="332" y="198" class="x-label">5/28</text>
                            <text x="412" y="198" class="x-label">5/29</text>
                            <text x="492" y="198" class="x-label">5/30</text>
                            <text x="572" y="198" class="x-label">5/31</text>

                        </svg>
                    </div>
                </article>

                <article class="usage-card">
                    <div class="usage-card-header">
                        <h2>시간대별 이용 현황</h2>
                        <span>단위: 건</span>
                    </div>

                    <div class="chart-box">
                        <svg viewBox="0 0 700 220" class="usage-svg-chart">

                            <line x1="45" y1="30" x2="670" y2="30" class="grid-line" />
                            <line x1="45" y1="75" x2="670" y2="75" class="grid-line" />
                            <line x1="45" y1="120" x2="670" y2="120" class="grid-line" />
                            <line x1="45" y1="165" x2="670" y2="165" class="grid-line" />

                            <polyline
                                points="70,152 140,138 210,114 280,92 350,74 420,60 490,84 560,118 630,142"
                                class="usage-line"
                                fill="none"
                            />

                            <circle cx="70"  cy="152" r="5" class="line-dot" />
                            <circle cx="140" cy="138" r="5" class="line-dot" />
                            <circle cx="210" cy="114" r="5" class="line-dot" />
                            <circle cx="280" cy="92"  r="5" class="line-dot" />
                            <circle cx="350" cy="74"  r="5" class="line-dot" />
                            <circle cx="420" cy="60"  r="5" class="line-dot" />
                            <circle cx="490" cy="84"  r="5" class="line-dot" />
                            <circle cx="560" cy="118" r="5" class="line-dot" />
                            <circle cx="630" cy="142" r="5" class="line-dot" />

                            <text x="70"  y="198" class="x-label">06시</text>
                            <text x="140" y="198" class="x-label">08시</text>
                            <text x="210" y="198" class="x-label">10시</text>
                            <text x="280" y="198" class="x-label">12시</text>
                            <text x="350" y="198" class="x-label">14시</text>
                            <text x="420" y="198" class="x-label">16시</text>
                            <text x="490" y="198" class="x-label">18시</text>
                            <text x="560" y="198" class="x-label">20시</text>
                            <text x="630" y="198" class="x-label">22시</text>

                        </svg>
                    </div>
                </article>

            </section>

            <!-- 하단 테이블 영역 -->
            <section class="usage-table-grid">

                <article class="usage-card">
                    <div class="usage-card-header">
                        <h2>충전 타입별 이용 현황</h2>
                    </div>

                    <table class="usage-table">
                        <thead>
                            <tr>
                                <th>충전 타입</th>
                                <th>이용 건수</th>
                                <th>비율</th>
                                <th>평균 시간</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>급속</td>
                                <td>4,658건</td>
                                <td class="point-text">52%</td>
                                <td>24분</td>
                            </tr>
                            <tr>
                                <td>완속</td>
                                <td>2,777건</td>
                                <td class="point-text">31%</td>
                                <td>62분</td>
                            </tr>
                            <tr>
                                <td>초급속</td>
                                <td>1,527건</td>
                                <td class="point-text">17%</td>
                                <td>18분</td>
                            </tr>
                        </tbody>
                    </table>
                </article>

                <article class="usage-card">
                    <div class="usage-card-header">
                        <h2>충전소별 이용 순위</h2>
                        <a href="${pageContext.request.contextPath}/admin/stat/usage/station">전체 보기</a>
                    </div>

                    <table class="usage-table">
                        <thead>
                            <tr>
                                <th>순위</th>
                                <th>충전소명</th>
                                <th>이용 건수</th>
                                <th>평균 시간</th>
                                <th>가동률</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><span class="rank-badge top">1</span></td>
                                <td>부산 사상 EV 충전소</td>
                                <td>1,284건</td>
                                <td>26분</td>
                                <td class="point-text">82%</td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge top">2</span></td>
                                <td>서면 EV 스테이션</td>
                                <td>1,106건</td>
                                <td>28분</td>
                                <td class="point-text">78%</td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge top">3</span></td>
                                <td>부산역 환승센터</td>
                                <td>986건</td>
                                <td>30분</td>
                                <td class="point-text">74%</td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge">4</span></td>
                                <td>해운대 센텀 충전소</td>
                                <td>852건</td>
                                <td>27분</td>
                                <td>69%</td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge">5</span></td>
                                <td>수영강변 공영 충전소</td>
                                <td>774건</td>
                                <td>29분</td>
                                <td>66%</td>
                            </tr>
                        </tbody>
                    </table>
                </article>

            </section>

            <section class="usage-card recent-usage-card">
                <div class="usage-card-header">
                    <h2>최근 이용 내역</h2>
                    <a href="${pageContext.request.contextPath}/admin/stat/usage/history">전체 보기</a>
                </div>

                <table class="recent-usage-table">
                    <thead>
                        <tr>
                            <th>이용일시</th>
                            <th>회원명</th>
                            <th>차량</th>
                            <th>충전소명</th>
                            <th>충전기</th>
                            <th>충전 타입</th>
                            <th>충전 시간</th>
                            <th>상태</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>2026-05-31 18:20</td>
                            <td>김민수</td>
                            <td>아이오닉 5</td>
                            <td>부산 사상 EV 충전소</td>
                            <td>급속 1번</td>
                            <td>급속</td>
                            <td>24분</td>
                            <td><span class="status-badge">완료</span></td>
                        </tr>
                        <tr>
                            <td>2026-05-31 17:40</td>
                            <td>이소연</td>
                            <td>EV6</td>
                            <td>서면 EV 스테이션</td>
                            <td>급속 2번</td>
                            <td>급속</td>
                            <td>28분</td>
                            <td><span class="status-badge">완료</span></td>
                        </tr>
                        <tr>
                            <td>2026-05-31 16:10</td>
                            <td>박정훈</td>
                            <td>테슬라 모델 3</td>
                            <td>해운대 센텀 충전소</td>
                            <td>초급속 1번</td>
                            <td>초급속</td>
                            <td>18분</td>
                            <td><span class="status-badge">완료</span></td>
                        </tr>
                    </tbody>
                </table>
            </section>

        </main>

    </div>

</div>

</body>
</html>