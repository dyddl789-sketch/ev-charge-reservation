<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 매출 통계</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sales.css">
</head>
<body>

<div class="admin-page">
    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>
    <div class="admin-layout">
        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <!-- 본문 -->
        <main class="admin-content">

            <section class="sales-title-row">
                <div>
                    <h1>매출 통계</h1>
                    <p>충전 서비스 매출 현황을 기간별, 충전소별로 확인할 수 있습니다.</p>
                </div>

                <form action="/admin/stat/sales" method="get" class="date-box">
                    <label for="startDate">기간</label>
                    <input type="date" id="startDate" name="startDate" value="2026-05-01">
                    <span>~</span>
                    <input type="date" id="endDate" name="endDate" value="2026-05-31">
                </form>
            </section>

            <!-- 상단 요약 카드 -->
            <section class="sales-summary">

                <article class="summary-card">
                    <span>총 매출</span>
                    <strong>18,450,000원</strong>
                    <p>전 기간 17,020,000원 대비 <em>+8.4%</em></p>
                </article>

                <article class="summary-card">
                    <span>결제 건수</span>
                    <strong>1,248건</strong>
                    <p>전 기간 1,152건 대비 <em>+8.3%</em></p>
                </article>

                <article class="summary-card">
                    <span>평균 결제금액</span>
                    <strong>14,790원</strong>
                    <p>전 기간 14,759원 대비 <em>+0.2%</em></p>
                </article>

                <article class="summary-card">
                    <span>전월 대비 증감</span>
                    <strong>+8.4%</strong>
                    <p>전월 대비 <em>+1,430,000원</em></p>
                </article>

            </section>

            <!-- 차트 영역 -->
            <section class="chart-grid">

    <!-- 일별 매출 현황 -->
    <article class="chart-card">
        <div class="card-header">
            <h2>일별 매출 현황</h2>
            <span>단위: 원</span>
        </div>

        <div class="chart-box">
            <svg viewBox="0 0 640 220" class="sales-svg-chart">

                <!-- grid lines -->
                <line x1="40" y1="30" x2="610" y2="30" class="grid-line" />
                <line x1="40" y1="75" x2="610" y2="75" class="grid-line" />
                <line x1="40" y1="120" x2="610" y2="120" class="grid-line" />
                <line x1="40" y1="165" x2="610" y2="165" class="grid-line" />

                <!-- bars -->
                <rect x="75"  y="88"  width="34" height="77" class="bar" />
                <rect x="155" y="62"  width="34" height="103" class="bar" />
                <rect x="235" y="105" width="34" height="60" class="bar" />
                <rect x="315" y="82"  width="34" height="83" class="bar" />
                <rect x="395" y="58"  width="34" height="107" class="bar" />
                <rect x="475" y="34"  width="34" height="131" class="bar" />
                <rect x="555" y="78"  width="34" height="87" class="bar" />

                <!-- x labels -->
                <text x="92"  y="198" class="x-label">6/12</text>
                <text x="172" y="198" class="x-label">6/13</text>
                <text x="252" y="198" class="x-label">6/14</text>
                <text x="332" y="198" class="x-label">6/15</text>
                <text x="412" y="198" class="x-label">6/16</text>
                <text x="492" y="198" class="x-label">6/17</text>
                <text x="572" y="198" class="x-label">6/18</text>
            </svg>
        </div>
    </article>

    <!-- 월별 매출 현황 -->
    <article class="chart-card">
        <div class="card-header">
            <h2>월별 매출 현황</h2>
            <span>단위: 원</span>
        </div>

	   	<div class="chart-box">
	            <svg viewBox="0 0 700 220" class="sales-svg-chart">
	
	                <!-- grid lines -->
	                <line x1="45" y1="30" x2="670" y2="30" class="grid-line" />
	                <line x1="45" y1="75" x2="670" y2="75" class="grid-line" />
	                <line x1="45" y1="120" x2="670" y2="120" class="grid-line" />
	                <line x1="45" y1="165" x2="670" y2="165" class="grid-line" />
	
	                <!-- line -->
	                <polyline
	                    points="80,142 165,126 250,112 335,96 420,82 505,68 590,48 655,70"
	                    class="sales-line"
	                    fill="none"
	                />
	
	                <!-- points -->
	                <circle cx="80"  cy="142" r="5" class="line-dot" />
	                <circle cx="165" cy="126" r="5" class="line-dot" />
	                <circle cx="250" cy="112" r="5" class="line-dot" />
	                <circle cx="335" cy="96"  r="5" class="line-dot" />
	                <circle cx="420" cy="82"  r="5" class="line-dot" />
	                <circle cx="505" cy="68"  r="5" class="line-dot" />
	                <circle cx="590" cy="48"  r="5" class="line-dot" />
	                <circle cx="655" cy="70"  r="5" class="line-dot" />
	
	                <!-- x labels -->
	                <text x="80"  y="198" class="x-label">2024-11</text>
	                <text x="165" y="198" class="x-label">2024-12</text>
	                <text x="250" y="198" class="x-label">2025-01</text>
	                <text x="335" y="198" class="x-label">2025-02</text>
	                <text x="420" y="198" class="x-label">2025-03</text>
	                <text x="505" y="198" class="x-label">2025-04</text>
	                <text x="590" y="198" class="x-label">2025-05</text>
	                <text x="655" y="198" class="x-label">2025-06</text>
	            </svg>
	        </div>
	    </article>
	
	</section>

            <!-- 하단 테이블 영역 -->
            <section class="sales-table-grid">

                <article class="table-card">
                    <div class="table-card-header">
					    <h3>충전소별 매출 순위</h3>
					    <a href="${pageContext.request.contextPath}/admin/stat/sales/station" class="section-more-link">전체 보기</a>
					</div>

                    <table class="rank-table">
                        <thead>
                            <tr>
                                <th>순위</th>
                                <th>충전소명</th>
                                <th>매출액</th>
                                <th>건수</th>
                                <th>전월 대비</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr>
                                <td><span class="rank-badge top">1</span></td>
                                <td>부산 사상 EV 충전소</td>
                                <td>25,680,000원</td>
                                <td>912건</td>
                                <td class="up-text">+22.5%</td>
                            </tr>

                            <tr>
                                <td><span class="rank-badge top">2</span></td>
                                <td>서면 EV 스테이션</td>
                                <td>21,430,000원</td>
                                <td>768건</td>
                                <td class="up-text">+17.3%</td>
                            </tr>

                            <tr>
                                <td><span class="rank-badge top">3</span></td>
                                <td>해운대 센텀 충전소</td>
                                <td>18,920,000원</td>
                                <td>654건</td>
                                <td class="up-text">+15.8%</td>
                            </tr>

                            <tr>
                                <td><span class="rank-badge">4</span></td>
                                <td>부산역 환승센터</td>
                                <td>16,780,000원</td>
                                <td>598건</td>
                                <td class="up-text">+12.4%</td>
                            </tr>

                            <tr>
                                <td><span class="rank-badge">5</span></td>
                                <td>동래구 공영 충전소</td>
                                <td>12,640,000원</td>
                                <td>472건</td>
                                <td class="up-text">+8.7%</td>
                            </tr>
                        </tbody>
                    </table>
                </article>

                <article class="table-card">
                    <div class="card-header">
                        <h2>최근 매출 내역</h2>
                        <div class="card-header">
						    <a href="${pageContext.request.contextPath}/admin/stat/sales/history" class="section-more-link">전체 보기</a>
						</div>
                    </div>

                    <table class="sales-history-table">
                        <thead>
                            <tr>
                                <th>결제일시</th>
                                <th>충전소명</th>
                                <th>충전기</th>
                                <th>충전량</th>
                                <th>결제금액</th>
                                <th>상태</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr>
                                <td>2026-06-18 14:32:15</td>
                                <td>부산 사상 EV 충전소</td>
                                <td>B-03</td>
                                <td>32.4kWh</td>
                                <td>16,000원</td>
                                <td><span class="status-badge">완료</span></td>
                            </tr>

                            <tr>
                                <td>2026-06-18 13:58:07</td>
                                <td>서면 EV 스테이션</td>
                                <td>A-02</td>
                                <td>45.8kWh</td>
                                <td>22,500원</td>
                                <td><span class="status-badge">완료</span></td>
                            </tr>

                            <tr>
                                <td>2026-06-18 13:15:44</td>
                                <td>해운대 센텀 충전소</td>
                                <td>C-05</td>
                                <td>28.7kWh</td>
                                <td>14,100원</td>
                                <td><span class="status-badge">완료</span></td>
                            </tr>

                            <tr>
                                <td>2026-06-18 12:46:22</td>
                                <td>부산역 환승센터</td>
                                <td>A-01</td>
                                <td>35.1kWh</td>
                                <td>17,200원</td>
                                <td><span class="status-badge">완료</span></td>
                            </tr>

                            <tr>
                                <td>2026-06-18 12:10:03</td>
                                <td>동래구 공영 충전소</td>
                                <td>B-04</td>
                                <td>41.2kWh</td>
                                <td>20,300원</td>
                                <td><span class="status-badge">완료</span></td>
                            </tr>
                        </tbody>
                    </table>
                </article>

            </section>

        </main>

    </div>

</div>

</body>
</html>