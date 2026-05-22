<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 차량 등록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/vehicle/vehicle.css">
</head>

<body>

<div class="vehicle-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="vehicle-main">

        <section class="page-title">
            <div>
                <h1>차량 등록</h1>
                <p>차량 정보를 등록하면 충전소 추천과 예상 충전 시간을 계산할 수 있습니다.</p>
            </div>

            <a href="/vehicle/list" class="back-btn">목록으로</a>
        </section>

        <section class="register-layout">

            <form action="/vehicle/registerProcess" method="post" class="vehicle-form">

                <div class="form-section">
                    <h2>기본 정보</h2>

                    <div class="form-grid">
                        <div class="form-group">
                            <label for="vehicleName">차량명</label>
                            <input type="text" id="vehicleName" name="vehicleName" placeholder="예: 아이오닉 5">
                        </div>

                        <div class="form-group">
                            <label for="manufacturer">제조사</label>
                            <select id="manufacturer" name="manufacturer">
                                <option value="">제조사 선택</option>
                                <option value="HYUNDAI">현대</option>
                                <option value="KIA">기아</option>
                                <option value="TESLA">테슬라</option>
                                <option value="BMW">BMW</option>
                                <option value="BENZ">벤츠</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="vehicleNumber">차량 번호</label>
                            <input type="text" id="vehicleNumber" name="vehicleNumber" placeholder="예: 12가 3456">
                        </div>

                        <div class="form-group">
                            <label for="chargeType">충전 타입</label>
                            <select id="chargeType" name="chargeType">
                                <option value="">충전 타입 선택</option>
                                <option value="DC_COMBO">DC콤보</option>
                                <option value="AC_SLOW">AC완속</option>
                                <option value="TESLA">테슬라</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <h2>배터리 정보</h2>

                    <div class="form-grid battery-grid">
                        <div class="form-group">
                            <label for="batteryCapacity">배터리 용량</label>
                            <input type="number" id="batteryCapacity" name="batteryCapacity" placeholder="예: 77.4">
                            <p class="help-text">단위: kWh</p>
                        </div>

                        

                        <div class="form-group">
                            <label for="mainVehicle">기본 차량 여부</label>
                            <select id="mainVehicle" name="mainVehicle">
                                <option value="N">아니오</option>
                                <option value="Y">예</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="form-notice">
                    <p>※ 기본 차량은 메인페이지의 내 차량 정보와 예약 추천 기준으로 사용됩니다.</p>
                    <p>※ 현재 배터리 잔량은 예약 화면에서 입력받습니다.</p>
                </div>

                <div class="form-buttons">
                    <button type="submit" class="submit-btn">차량 등록</button>
                    <a href="/vehicle/list" class="cancel-btn">취소</a>
                </div>

            </form>

            <aside class="guide-box">
                <h2>차량 정보가 필요한 이유</h2>

                <ul>
                    <li>차량의 충전 타입과 맞는 충전소를 추천합니다.</li>
                    <li>차량 배터리 용량을 기준으로 예상 충전 시간을 계산합니다.</li>
                    <li>목표 배터리 잔량에 따라 예상 비용을 계산할 수 있습니다.</li>
                    <li>기본 차량은 예약 화면에서 자동 선택됩니다.</li>
                </ul>
            </aside>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

</body>
</html>