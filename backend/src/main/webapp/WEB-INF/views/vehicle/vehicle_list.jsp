<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 내 차량</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/vehicle/vehicle.css"></head>
<body>

<div class="vehicle-page">
<jsp:include page="/WEB-INF/views/common/header.jsp" />
   

    <main class="vehicle-main">

        <section class="page-title">
            <div>
                <h1>내 차량</h1>
                <p>등록된 차량 정보를 기준으로 충전소 추천과 예약 정보를 제공합니다.</p>
            </div>

            <a href="/vehicle/register" class="add-btn">차량 등록</a>
        </section>

        <!-- 요약 카드 -->
        <section class="vehicle-summary">
            <div class="summary-card">
                <span>등록 차량</span>
                <strong>2대</strong>
            </div>

            <div class="summary-card">
                <span>기본 차량</span>
                <strong>아이오닉 5</strong>
            </div>

            
        </section>

        <!-- 차량 목록 -->
        <section class="vehicle-list">

            <article class="vehicle-card main-vehicle">
                <div class="vehicle-image">
                    <span>EV</span>
                </div>

                <div class="vehicle-info">
                    <div class="vehicle-title-row">
                        <div>
                            <span class="badge">기본 차량</span>
                            <h2>아이오닉 5</h2>
                        </div>
                        <span class="vehicle-status">사용중</span>
                    </div>

                    <p class="vehicle-desc">현대 · DC콤보 · 장거리 주행용</p>

                    <div class="vehicle-data">
                        <div>
                            <span>배터리 용량</span>
                            <strong>77.4kWh</strong>
                        </div>
                        <div>
                            <span>현재 배터리</span>
                            <strong>62%</strong>
                        </div>
                        <div>
                            <span>충전 타입</span>
                            <strong>DC콤보</strong>
                        </div>
                        <div>
                            <span>차량 번호</span>
                            <strong>12가 3456</strong>
                        </div>
                    </div>

                    <div class="card-buttons">
                        <button type="button" class="outline-btn">기본 차량 설정</button>
                        <button type="button" class="danger-btn">삭제</button>
                    </div>
                </div>
            </article>

            <article class="vehicle-card">
                <div class="vehicle-image">
                    <span>EV</span>
                </div>

                <div class="vehicle-info">
                    <div class="vehicle-title-row">
                        <div>
                            <h2>EV6</h2>
                        </div>
                        <span class="vehicle-status sub">대기</span>
                    </div>

                    <p class="vehicle-desc">기아 · DC콤보 · 출퇴근용</p>

                    <div class="vehicle-data">
                        <div>
                            <span>배터리 용량</span>
                            <strong>70.4kWh</strong>
                        </div>
                        <div>
                            <span>현재 배터리</span>
                            <strong>48%</strong>
                        </div>
                        <div>
                            <span>충전 타입</span>
                            <strong>DC콤보</strong>
                        </div>
                        <div>
                            <span>차량 번호</span>
                            <strong>34나 7890</strong>
                        </div>
                    </div>

                    <div class="card-buttons">
                        <button type="button" class="outline-btn">기본 차량 설정</button>
                        <button type="button" class="danger-btn">삭제</button>
                    </div>
                </div>
            </article>

        </section>

    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</div>

<script>
    const msg = "${msg}";

    if (msg !== "") {
        alert(msg);
    }
</script>

</body>
</html>