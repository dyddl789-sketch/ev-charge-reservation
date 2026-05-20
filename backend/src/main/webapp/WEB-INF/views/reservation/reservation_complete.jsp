<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약 완료</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/reservation.css">
</head>
<body>

<div class="reservation-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="reservation-main">

        <section class="complete-wrap">

            <div class="complete-card">

                <div class="complete-icon">✓</div>

                <h1>예약이 완료되었습니다</h1>
                <p class="complete-desc">
                    선택한 충전소와 충전기에 대한 예약이 정상적으로 등록되었습니다.
                </p>

                <div class="complete-station-box">
                    <div>
                        <strong>부산 사상 EV 충전소</strong>
                        <p>부산 사상구 괘법동 518-1</p>
                    </div>
                </div>

                <div class="complete-summary">

                    <div>
                        <span>예약 상태</span>
                        <strong class="orange-text">예약완료</strong>
                    </div>

                    <div>
                        <span>예약 차량</span>
                        <strong>아이오닉 5</strong>
                    </div>

                    <div>
                        <span>충전기</span>
                        <strong>급속 1번</strong>
                    </div>

                    <div>
                        <span>예약 날짜</span>
                        <strong>${reservation.reservationDate}</strong>
                    </div>

                    <div>
                        <span>예약 시작</span>
                        <strong>${reservation.startTimeValue}</strong>
                    </div>

                    <div>
                        <span>예약 종료 예정</span>
                        <strong>${reservation.endTime}</strong>
                    </div>

                    <div>
                        <span>배터리 잔량</span>
                        <strong>${reservation.currentSoc}% → ${reservation.targetSoc}%</strong>
                    </div>

                    <div>
                        <span>예상 필요 충전량</span>
                        <strong>${reservation.requiredKwh}kWh</strong>
                    </div>

                    <div>
                        <span>예상 충전 시간</span>
                        <strong>${reservation.estimatedMinutes}분</strong>
                    </div>

                    <div class="complete-cost-row">
                        <span>예상 충전 비용</span>
                        <strong>${reservation.estimatedCost}원</strong>
                    </div>

                </div>

                <div class="complete-notice">
                    <p>※ 현재 화면은 예약 확정 후 완료 화면 테스트용입니다.</p>
                    <p>※ 나중에 `/reservation/save`에서 reservation 테이블 insert를 처리하면 실제 예약 정보가 저장됩니다.</p>
                    <p>※ 실제 충전이 시작되면 charging_session 테이블에 충전 사용 기록이 생성됩니다.</p>
                </div>

                <div class="complete-buttons">
                    <a href="/reservation/my" class="complete-main-btn">내 예약 보기</a>
                    <a href="/station/list" class="complete-outline-btn">충전소 탐색</a>
                    <a href="/main" class="complete-outline-btn">메인으로</a>
                </div>

            </div>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

</body>
</html>