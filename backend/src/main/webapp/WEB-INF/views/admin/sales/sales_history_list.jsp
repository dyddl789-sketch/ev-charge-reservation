<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 최근 매출 내역</title>

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
                    <h1>최근 매출 내역</h1>
                    <p>충전 완료 후 발생한 매출 내역을 최신순으로 확인합니다.</p>
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
                        <label>상태</label>
                        <select>
                            <option>전체</option>
                            <option>완료</option>
                            <option>취소</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <label>검색어</label>
                        <div>
                            <input type="text" placeholder="회원명, 충전소명, 차량명을 입력하세요.">
                            <button type="button">조회</button>
                        </div>
                    </div>
                </div>
            </section>

            <section class="summary-row">
                <article>
                    <span>총 매출</span>
                    <strong>128,450,000원</strong>
                </article>

                <article>
                    <span>결제 건수</span>
                    <strong>4,562건</strong>
                </article>

                <article>
                    <span>총 충전량</span>
                    <strong>18,420.6kWh</strong>
                </article>

                <article>
                    <span>평균 결제금액</span>
                    <strong>28,161원</strong>
                </article>
            </section>

            <section class="table-card">

                <div class="table-top">
                    <p>총 <strong>4,562건</strong></p>

                    <select>
                        <option>10개씩 보기</option>
                        <option>20개씩 보기</option>
                        <option>50개씩 보기</option>
                    </select>
                </div>

                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>결제일시</th>
                            <th>회원명</th>
                            <th>차량</th>
                            <th>충전소명</th>
                            <th>충전기</th>
                            <th>충전량</th>
                            <th>충전 시간</th>
                            <th>결제금액</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>2026-06-18 14:32:15</td>
                            <td>김민수</td>
                            <td>아이오닉 5</td>
                            <td>부산 사상 EV 충전소</td>
                            <td>B-03</td>
                            <td>32.4kWh</td>
                            <td>24분</td>
                            <td>16,000원</td>
                            <td><span class="status-badge">완료</span></td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td>2026-06-18 13:58:07</td>
                            <td>이소연</td>
                            <td>EV6</td>
                            <td>서면 EV 스테이션</td>
                            <td>A-02</td>
                            <td>45.8kWh</td>
                            <td>28분</td>
                            <td>22,500원</td>
                            <td><span class="status-badge">완료</span></td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td>2026-06-18 13:15:44</td>
                            <td>박지훈</td>
                            <td>기아 EV6</td>
                            <td>해운대 센텀 충전소</td>
                            <td>C-05</td>
                            <td>28.7kWh</td>
                            <td>21분</td>
                            <td>14,100원</td>
                            <td><span class="status-badge">완료</span></td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td>2026-06-18 12:46:22</td>
                            <td>최유리</td>
                            <td>아이오닉 6</td>
                            <td>부산역 환승센터</td>
                            <td>A-01</td>
                            <td>35.1kWh</td>
                            <td>26분</td>
                            <td>17,200원</td>
                            <td><span class="status-badge">완료</span></td>
                            <td><a href="#" class="detail-btn">상세</a></td>
                        </tr>

                        <tr>
                            <td>2026-06-18 12:10:03</td>
                            <td>정우성</td>
                            <td>테슬라 모델 3</td>
                            <td>동래구 공영 충전소</td>
                            <td>B-04</td>
                            <td>41.2kWh</td>
                            <td>30분</td>
                            <td>20,300원</td>
                            <td><span class="status-badge">완료</span></td>
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