<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약 현황</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/reservation.css">
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
                    <h1>예약 현황</h1>
                    <p>전체 예약 진행 상태와 인증, 취소, 노쇼 현황을 확인하고 관리합니다.</p>
                </div>

                <form action="/admin/reservation/list" method="get" class="date-box">
                    <input type="date" id="reservationDate" name="date" value="2026-05-20">
                </form>
            </section>

            <!-- 요약 카드 -->
            <section class="reservation-summary">

                <article class="reservation-summary-card">
                    <span>전체 예약 수</span>
                    <strong>1,248건</strong>
                    <p>오늘 기준 전체 예약</p>
                </article>

                <article class="reservation-summary-card">
                    <span>예약완료</span>
                    <strong class="orange-text">312건</strong>
                    <p>인증 대기 상태</p>
                </article>

                <article class="reservation-summary-card">
                    <span>인증완료</span>
                    <strong class="blue-text">186건</strong>
                    <p>충전 시작 대기</p>
                </article>

                <article class="reservation-summary-card">
                    <span>충전중</span>
                    <strong class="green-text">74건</strong>
                    <p>현재 충전 진행</p>
                </article>

                <article class="reservation-summary-card">
                    <span>완료</span>
                    <strong>548건</strong>
                    <p>충전 완료</p>
                </article>

                <article class="reservation-summary-card">
                    <span>취소 / 노쇼</span>
                    <strong class="red-text">128건</strong>
                    <p>취소 96건 / 노쇼 32건</p>
                </article>

            </section>

            <!-- 검색 영역 -->
            <section class="reservation-search-card">

                <div class="filter-row">
                    <span class="filter-title">상태 필터</span>

                    <button type="button" class="filter-btn active">전체</button>
                    <button type="button" class="filter-btn">예약완료</button>
                    <button type="button" class="filter-btn">인증완료</button>
                    <button type="button" class="filter-btn">충전중</button>
                    <button type="button" class="filter-btn">완료</button>
                    <button type="button" class="filter-btn">취소</button>
                    <button type="button" class="filter-btn">노쇼</button>
                </div>

                <div class="search-row">
                    <div class="search-group">
                        <label for="searchType">검색 기준</label>
                        <select id="searchType">
                            <option value="member">회원명</option>
                            <option value="vehicle">차량명</option>
                            <option value="station">충전소명</option>
                            <option value="authCode">인증코드</option>
                        </select>
                    </div>

                    <div class="keyword-box">
                        <input type="text" placeholder="회원명, 차량명, 충전소명, 인증코드를 입력하세요.">
                        <button type="button">검색</button>
                    </div>

                    <div class="search-group date">
                        <label for="startDate">시작일</label>
                        <input type="date" id="startDate" value="2026-05-01">
                    </div>

                    <span class="date-wave">~</span>

                    <div class="search-group date">
                        <label for="endDate">종료일</label>
                        <input type="date" id="endDate" value="2026-05-20">
                    </div>
                </div>

            </section>

            <section class="reservation-content-grid">

                <!-- 예약 목록 -->
                <section class="reservation-table-card">

                    <div class="table-top">
                        <p>총 <strong>1,248건</strong></p>

                        <select>
                            <option>10개씩 보기</option>
                            <option>20개씩 보기</option>
                            <option>50개씩 보기</option>
                        </select>
                    </div>

                    <table class="reservation-table">
                        <thead>
                            <tr>
                                <th>예약번호</th>
                                <th>예약일시</th>
                                <th>회원명</th>
                                <th>차량</th>
                                <th>충전소</th>
                                <th>충전기</th>
                                <th>SOC</th>
                                <th>예상시간</th>
                                <th>예상비용</th>
                                <th>인증코드</th>
                                <th>상태</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr>
                                <td>R250519-001</td>
                                <td>2026-05-19 09:00</td>
                                <td>김민수</td>
                                <td>아이오닉 5</td>
                                <td>부산 사상 EV 충전소</td>
                                <td>DC콤보 02</td>
                                <td>30% → 80%</td>
                                <td>24분</td>
                                <td>9,675원</td>
                                <td>A8K29Q</td>
                                <td><span class="status-badge reserved">예약완료</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                    <button type="button" class="cancel-btn">취소</button>
                                </td>
                            </tr>

                            <tr>
                                <td>R250519-002</td>
                                <td>2026-05-19 10:30</td>
                                <td>이소연</td>
                                <td>EV6</td>
                                <td>서면 EV 충전소</td>
                                <td>DC콤보 01</td>
                                <td>35% → 90%</td>
                                <td>28분</td>
                                <td>11,250원</td>
                                <td>B4J17P</td>
                                <td><span class="status-badge verified">인증완료</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                    <button type="button" class="start-btn">충전시작</button>
                                </td>
                            </tr>

                            <tr>
                                <td>R250519-003</td>
                                <td>2026-05-19 11:00</td>
                                <td>박정훈</td>
                                <td>테슬라 모델 3</td>
                                <td>해운대 센텀 충전소</td>
                                <td>급속 03</td>
                                <td>42% → 85%</td>
                                <td>21분</td>
                                <td>8,300원</td>
                                <td>C6M55X</td>
                                <td><span class="status-badge charging">충전중</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                </td>
                            </tr>

                            <tr>
                                <td>R250519-004</td>
                                <td>2026-05-19 12:30</td>
                                <td>최지은</td>
                                <td>코나 EV</td>
                                <td>동래구 공영 충전소</td>
                                <td>DC콤보 02</td>
                                <td>25% → 85%</td>
                                <td>30분</td>
                                <td>10,800원</td>
                                <td>D7J31K</td>
                                <td><span class="status-badge completed">완료</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                </td>
                            </tr>

                            <tr>
                                <td>R250519-005</td>
                                <td>2026-05-19 13:00</td>
                                <td>정하늘</td>
                                <td>아이오닉 6</td>
                                <td>수영강변 공영 충전소</td>
                                <td>DC콤보 01</td>
                                <td>50% → 90%</td>
                                <td>18분</td>
                                <td>6,750원</td>
                                <td>E9P88L</td>
                                <td><span class="status-badge canceled">취소</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                    <button type="button" class="delete-btn">삭제</button>
                                </td>
                            </tr>

                            <tr>
                                <td>R250519-006</td>
                                <td>2026-05-19 14:30</td>
                                <td>강태우</td>
                                <td>EV9</td>
                                <td>부산 사상 EV 충전소</td>
                                <td>DC콤보 03</td>
                                <td>60% → 90%</td>
                                <td>18분</td>
                                <td>6,480원</td>
                                <td>F2Q91M</td>
                                <td><span class="status-badge noshow">노쇼</span></td>
                                <td>
                                    <a href="/admin/reservation/detail?reservationId=1" class="detail-btn">상세</a>
                                    <button type="button" class="delete-btn">삭제</button>
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

                <!-- 우측 정보 패널 -->
                <aside class="reservation-side-panel">

                    <section class="issue-card">
                        <div class="side-card-header">
                            <h2>오늘 예약 이슈</h2>
                            <a href="#">전체 보기</a>
                        </div>

                        <div class="issue-list">
                            <div>
                                <span>인증 대기</span>
                                <strong>24건</strong>
                            </div>

                            <div>
                                <span>취소 요청</span>
                                <strong>8건</strong>
                            </div>

                            <div>
                                <span>노쇼 예정</span>
                                <strong>3건</strong>
                            </div>

                            <div>
                                <span>충전 시작 지연</span>
                                <strong>5건</strong>
                            </div>
                        </div>
                    </section>

                </aside>

            </section>

        </main>

    </div>

</div>

<script>
    const filterButtons = document.querySelectorAll(".filter-btn");

    filterButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            filterButtons.forEach(function(item) {
                item.classList.remove("active");
            });

            button.classList.add("active");
        });
    });

    const cancelButtons = document.querySelectorAll(".cancel-btn");

    cancelButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            const result = confirm("해당 예약을 취소 처리하시겠습니까?");

            if (!result) {
                return;
            }

            alert("예약이 취소 처리되었습니다.");
        });
    });
</script>

</body>
</html>