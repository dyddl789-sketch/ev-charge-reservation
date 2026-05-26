<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약 상세</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/reservation_detail.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

            <!-- 제목 영역 -->
            <section class="detail-title-row">
                <div>
                    <h1>예약 상세</h1>
                    <p>예약 정보, 인증 코드, 충전 예정 정보를 확인합니다.</p>
                </div>

                <a href="/admin/reservation/list" class="back-btn">목록으로</a>
            </section>

            <!-- 예약 요약 -->
            <section class="reservation-overview-card">

                <div class="overview-item">
                    <span>예약번호</span>
                    <strong>R250519-001</strong>
                </div>

                <div class="overview-item">
                    <span>예약 상태</span>
                    <strong class="status-text reserved">예약완료</strong>
                </div>

                <div class="overview-item">
                    <span>예약일시</span>
                    <strong>2026-05-19 09:00</strong>
                </div>

                <div class="overview-item">
                    <span>종료 예정</span>
                    <strong>2026-05-19 10:00</strong>
                </div>

                <div class="overview-item">
                    <span>인증 코드</span>
                    <strong class="auth-code">A8K29Q</strong>
                </div>

                <div class="overview-item">
                    <span>예상 비용</span>
                    <strong>9,675원</strong>
                </div>

            </section>

            <!-- 상세 정보 -->
            <section class="detail-grid">

                <!-- 회원 정보 -->
                <article class="detail-card">
                    <h2>회원 정보</h2>

                    <div class="info-row">
                        <span>회원명</span>
                        <strong>김민수</strong>
                    </div>

                    <div class="info-row">
                        <span>아이디</span>
                        <strong>kms0520</strong>
                    </div>

                    <div class="info-row">
                        <span>연락처</span>
                        <strong>010-1234-5678</strong>
                    </div>

                    <div class="info-row">
                        <span>이메일</span>
                        <strong>kms0520@example.com</strong>
                    </div>
                </article>

                <!-- 차량 정보 -->
                <article class="detail-card">
                    <h2>차량 정보</h2>

                    <div class="info-row">
                        <span>차량명</span>
                        <strong>아이오닉 5</strong>
                    </div>

                    <div class="info-row">
                        <span>차량 번호</span>
                        <strong>12가 3456</strong>
                    </div>

                    <div class="info-row">
                        <span>충전 타입</span>
                        <strong>DC콤보</strong>
                    </div>

                    <div class="info-row">
                        <span>배터리 용량</span>
                        <strong>77.4kWh</strong>
                    </div>
                </article>

                <!-- 충전소 정보 -->
                <article class="detail-card">
                    <h2>충전소 정보</h2>

                    <div class="info-row">
                        <span>충전소명</span>
                        <strong>부산 사상 EV 충전소</strong>
                    </div>

                    <div class="info-row">
                        <span>주소</span>
                        <strong>부산 사상구 괘법동 518-1</strong>
                    </div>

                    <div class="info-row">
                        <span>충전기</span>
                        <strong>DC콤보 02</strong>
                    </div>

                    <div class="info-row">
                        <span>출력</span>
                        <strong>200kW</strong>
                    </div>
                </article>

                <!-- 예약 조건 -->
                <article class="detail-card">
                    <h2>예약 조건</h2>

                    <div class="info-row">
                        <span>현재 SOC</span>
                        <strong>30%</strong>
                    </div>

                    <div class="info-row">
                        <span>목표 SOC</span>
                        <strong>80%</strong>
                    </div>

                    <div class="info-row">
                        <span>예상 필요 충전량</span>
                        <strong>38.70kWh</strong>
                    </div>

                    <div class="info-row">
                        <span>예상 충전 시간</span>
                        <strong>24분</strong>
                    </div>
                </article>

                <!-- 비용 정보 -->
                <article class="detail-card">
                    <h2>비용 정보</h2>

                    <div class="info-row">
                        <span>kWh당 요금</span>
                        <strong>250원</strong>
                    </div>

                    <div class="info-row">
                        <span>예상 충전량</span>
                        <strong>38.70kWh</strong>
                    </div>

                    <div class="info-row">
                        <span>예상 비용</span>
                        <strong class="cost-text">9,675원</strong>
                    </div>

                    <div class="info-row">
                        <span>결제 상태</span>
                        <strong>충전 완료 후 정산</strong>
                    </div>
                </article>

                <!-- 처리 정보 -->
                <article class="detail-card">
                    <h2>처리 정보</h2>

                    <div class="info-row">
                        <span>예약 등록일</span>
                        <strong>2026-05-18 21:30</strong>
                    </div>

                    <div class="info-row">
                        <span>인증 완료 시간</span>
                        <strong>-</strong>
                    </div>

                    <div class="info-row">
                        <span>취소 시간</span>
                        <strong>-</strong>
                    </div>

                    <div class="info-row">
                        <span>노쇼 처리 시간</span>
                        <strong>-</strong>
                    </div>
                </article>

            </section>

            <!-- 상태 변경 이력 -->
            <section class="status-history-card">
                <h2>상태 변경 이력</h2>

                <table class="history-table">
                    <thead>
                        <tr>
                            <th>변경일시</th>
                            <th>상태</th>
                            <th>처리자</th>
                            <th>비고</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr>
                            <td>2026-05-18 21:30</td>
                            <td>예약완료</td>
                            <td>사용자</td>
                            <td>예약 생성</td>
                        </tr>

                        <tr>
                            <td>-</td>
                            <td>-</td>
                            <td>-</td>
                            <td>아직 변경 이력이 없습니다.</td>
                        </tr>
                    </tbody>
                </table>
            </section>

            <!-- 하단 버튼 -->
            <section class="detail-actions">
                <a href="/admin/reservation/list" class="outline-btn">목록으로</a>
                <button type="button" class="cancel-reservation-btn">예약 취소</button>
            </section>

        </main>

    </div>

</div>

<script>
    const cancelBtn = document.querySelector(".cancel-reservation-btn");

    if (cancelBtn) {
        cancelBtn.addEventListener("click", function() {
            const result = confirm("해당 예약을 취소 처리하시겠습니까?");

            if (!result) {
                return;
            }

            alert("예약이 취소 처리되었습니다.");
            location.href = "/admin/reservation/list";
        });
    }
</script>

</body>
</html>