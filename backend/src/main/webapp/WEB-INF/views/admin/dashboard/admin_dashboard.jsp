<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 관리자 대시보드</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
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
                    <h1>관리자 대시보드</h1>
                    <p>EV Charge 서비스 운영 현황을 한눈에 확인하고 관리할 수 있습니다.</p>
                </div>

                <form action="/admin/dashboard" method="get" class="date-box">
                    <input type="date" name="date" value="2026-05-20">
                </form>
            </section>

            <!-- 상단 요약 카드 -->
            <section class="summary-grid">

                <article class="summary-card">
                    <span>전체 회원 수</span>
                    <strong>28,745명</strong>
                    <p>어제 대비 +318</p>
                </article>

                <article class="summary-card">
                    <span>전체 충전소 수</span>
                    <strong>156개</strong>
                    <p>어제 대비 +1</p>
                </article>

                <article class="summary-card">
                    <span>전체 충전기 수</span>
                    <strong>1,248대</strong>
                    <p>어제 대비 +8</p>
                </article>

                <article class="summary-card">
                    <span>오늘 예약 수</span>
                    <strong>342건</strong>
                    <p>어제 대비 +27</p>
                </article>

            </section>

            <!-- 중간 영역 -->
            <section class="dashboard-grid">

                <!-- 충전기 상태 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>충전기 상태 현황</h2>
                    </div>

                    <div class="charger-status-grid">

                        <div>
                            <span class="status-icon green">✓</span>
                            <p>사용 가능</p>
                            <strong class="green-text">856대</strong>
                            <em>68.6%</em>
                        </div>

                        <div>
                            <span class="status-icon blue">■</span>
                            <p>사용중</p>
                            <strong class="blue-text">274대</strong>
                            <em>22.0%</em>
                        </div>

                        <div>
                            <span class="status-icon orange">⌚</span>
                            <p>예약됨</p>
                            <strong class="orange-text">68대</strong>
                            <em>5.5%</em>
                        </div>

                        <div>
                            <span class="status-icon red">!</span>
                            <p>점검중/고장</p>
                            <strong class="red-text">50대</strong>
                            <em>4.0%</em>
                        </div>

                    </div>
                </article>

                <!-- 오늘 예약 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>오늘 예약 현황</h2>
                        <a href="/admin/reservation/list">전체 보기</a>
                    </div>

                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>예약자</th>
                                <th>차량</th>
                                <th>충전소</th>
                                <th>충전기</th>
                                <th>예약 시간</th>
                                <th>상태</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr>
                                <td>김민수</td>
                                <td>아이오닉 5</td>
                                <td>부산 사상 EV 충전소</td>
                                <td>DC콤보 02</td>
                                <td>09:00 ~ 10:00</td>
                                <td><span class="table-badge reserved">예약 확정</span></td>
                            </tr>

                            <tr>
                                <td>이소연</td>
                                <td>EV6</td>
                                <td>서면 EV 충전소</td>
                                <td>DC콤보 01</td>
                                <td>10:30 ~ 11:30</td>
                                <td><span class="table-badge reserved">예약 확정</span></td>
                            </tr>

                            <tr>
                                <td>박정훈</td>
                                <td>테슬라 모델 3</td>
                                <td>해운대 센텀 충전소</td>
                                <td>DC콤보 03</td>
                                <td>11:00 ~ 12:00</td>
                                <td><span class="table-badge reserved">예약 확정</span></td>
                            </tr>

                            <tr>
                                <td>최지은</td>
                                <td>코나 EV</td>
                                <td>동래구 공영 충전소</td>
                                <td>DC콤보 02</td>
                                <td>13:00 ~ 14:00</td>
                                <td><span class="table-badge reserved">예약 확정</span></td>
                            </tr>

                            <tr>
                                <td>정하늘</td>
                                <td>아이오닉 6</td>
                                <td>수영강변 공영 충전소</td>
                                <td>DC콤보 01</td>
                                <td>14:30 ~ 15:30</td>
                                <td><span class="table-badge reserved">예약 확정</span></td>
                            </tr>
                        </tbody>
                    </table>
                </article>

            </section>

            <!-- 하단 영역 -->
            <section class="bottom-grid">

                <!-- 충전소 운영 현황 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>충전소 운영 현황</h2>
                        <a href="/admin/station/list">전체 보기</a>
                    </div>

                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>충전소명</th>
                                <th>주소</th>
                                <th>충전기 수</th>
                                <th>사용 가능</th>
                                <th>운영 상태</th>
                            </tr>
                        </thead>

                        <tbody>
                            <tr>
                                <td>부산 사상 EV 충전소</td>
                                <td>부산 사상구 괘법동 518-1</td>
                                <td>12</td>
                                <td>8대</td>
                                <td><span class="dot green-dot"></span>정상 운영</td>
                            </tr>

                            <tr>
                                <td>부산역 환승센터</td>
                                <td>부산 동구 중앙대로 206</td>
                                <td>10</td>
                                <td>7대</td>
                                <td><span class="dot green-dot"></span>정상 운영</td>
                            </tr>

                            <tr>
                                <td>서면 EV 충전소</td>
                                <td>부산 부산진구 중앙대로 672</td>
                                <td>8</td>
                                <td>5대</td>
                                <td><span class="dot green-dot"></span>정상 운영</td>
                            </tr>

                            <tr>
                                <td>해운대 센텀 충전소</td>
                                <td>부산 해운대구 센텀중앙로 55</td>
                                <td>10</td>
                                <td>6대</td>
                                <td><span class="dot green-dot"></span>정상 운영</td>
                            </tr>

                            <tr>
                                <td>동래구 공영 충전소</td>
                                <td>부산 동래구 명륜동 530-1</td>
                                <td>6</td>
                                <td>3대</td>
                                <td><span class="dot orange-dot"></span>일부 점검</td>
                            </tr>
                        </tbody>
                    </table>
                </article>

                <!-- 최근 알림 -->
                <article class="dashboard-card">
                    <div class="card-header">
                        <h2>최근 알림</h2>
                    </div>

                    <div class="notice-list">

                        <div>
                            <strong>서면 EV 충전소 DC콤보 04가 정상으로 복구되었습니다.</strong>
                            <span>10분 전</span>
                        </div>

                        <div>
                            <strong>오늘 예약 건수가 300건을 초과했습니다.</strong>
                            <span>1시간 전</span>
                        </div>

                        <div>
                            <strong>해운대 센텀 충전소 DC콤보 05 점검이 필요합니다.</strong>
                            <span>2시간 전</span>
                        </div>

                        <div>
                            <strong>신규 회원 25명이 가입했습니다.</strong>
                            <span>3시간 전</span>
                        </div>

                        <div>
                            <strong>오늘 매출이 1,200만원을 달성했습니다.</strong>
                            <span>4시간 전</span>
                        </div>

                    </div>
                </article>

            </section>

        </main>

    </div>

</div>

</body>
</html>