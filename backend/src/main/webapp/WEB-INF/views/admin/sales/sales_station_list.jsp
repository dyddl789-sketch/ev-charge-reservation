<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소별 매출 순위</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/sales_list.css">
</head>
<body>

<div class="admin-page">
    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>
    <div class="admin-layout">
        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>
        <main class="admin-content">

            <section class="page-title-row">
                <div>
                    <h1>충전소별 매출 순위</h1>
                    <p>기간별 충전소 매출, 이용 건수, 충전량을 순위로 확인합니다.</p>
                </div>

                <a href="/admin/stat/sales" class="back-btn">매출 통계로 돌아가기</a>
            </section>

            <section class="search-card">
                <div class="search-row">
                    <div class="search-group">
                        <label>기간</label>
                        <div class="date-range">
                            <input type="date" value="2026-05-01">
                            <span>~</span>
                            <input type="date" value="2026-05-31">
                        </div>
                    </div>

                    <div class="search-group">
                        <label>지역</label>
                        <select>
                            <option>전체</option>
                            <option>부산</option>
                            <option>서울</option>
                            <option>대구</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <label>충전소명</label>
                        <div>
                            <input type="text" placeholder="충전소명을 입력하세요.">
                            <button type="button">조회</button>
                        </div>
                    </div>
                </div>
            </section>

            <section class="summary-row">
                <article>
                    <span>조회 충전소</span>
                    <strong>156곳</strong>
                </article>

                <article>
                    <span>총 매출</span>
                    <strong>128,450,000원</strong>
                </article>

                <article>
                    <span>총 결제 건수</span>
                    <strong>4,562건</strong>
                </article>

                <article>
                    <span>총 충전량</span>
                    <strong>18,420.6kWh</strong>
                </article>
            </section>

            <section class="table-card">

                <div class="table-top">
                    <p>총 <strong>156건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>순위</th>
                            <th>충전소명</th>
                            <th>주소</th>
                            <th>총 매출액</th>
                            <th>결제 건수</th>
                            <th>총 충전량</th>
                            <th>평균 결제금액</th>
                            <th>전월 대비</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td><span class="rank-badge top">1</span></td>
                            <td>부산 사상 EV 충전소</td>
                            <td>부산 사상구 괘법동 518-1</td>
                            <td>25,680,000원</td>
                            <td>912건</td>
                            <td>3,842.5kWh</td>
                            <td>28,157원</td>
                            <td class="up-text">+22.5%</td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge top">2</span></td>
                            <td>서면 EV 스테이션</td>
                            <td>부산 부산진구 중앙대로 672</td>
                            <td>21,430,000원</td>
                            <td>768건</td>
                            <td>3,102.8kWh</td>
                            <td>27,903원</td>
                            <td class="up-text">+17.3%</td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge top">3</span></td>
                            <td>해운대 센텀 충전소</td>
                            <td>부산 해운대구 센텀중앙로 55</td>
                            <td>18,920,000원</td>
                            <td>654건</td>
                            <td>2,745.2kWh</td>
                            <td>28,929원</td>
                            <td class="up-text">+15.8%</td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">4</span></td>
                            <td>부산역 환승센터</td>
                            <td>부산 동구 중앙대로 206</td>
                            <td>16,780,000원</td>
                            <td>598건</td>
                            <td>2,416.8kWh</td>
                            <td>28,060원</td>
                            <td class="up-text">+12.4%</td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td><span class="rank-badge">5</span></td>
                            <td>동래구 공영 충전소</td>
                            <td>부산 동래구 명륜동 530-1</td>
                            <td>12,640,000원</td>
                            <td>472건</td>
                            <td>1,892.1kWh</td>
                            <td>26,779원</td>
                            <td class="up-text">+8.7%</td>
                            <td><a href="#" class="detail-btn">상세</a></td>
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