<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약하기</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservation/reservation.css">
</head>
<body>

<div class="reservation-page">

    <jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="reservation-main">

        <section class="page-title">
            <div>
                <h1>충전 예약하기</h1>
                <p>차량, 충전기, 배터리 잔량을 기준으로 예상 충전 시간과 비용을 계산합니다.</p>
            </div>

            <a href="/station/list" class="back-btn">충전소 탐색으로</a>
        </section>

        <section class="reservation-layout">

            <!-- 왼쪽: 예약 입력 -->
            <section class="reservation-form-card">

                <div class="section-title">
                    <h2>예약 정보 입력</h2>
                    <p>예약 조건을 입력하면 예상 충전량, 시간, 비용이 자동 계산됩니다.</p>
                </div>

                <form action="/reservation/save" method="post" id="reservationForm">

                    <!-- 계산 결과 DB 저장용 -->
                    <input type="hidden" name="startTime" id="startTimeInput">
                    <input type="hidden" name="endTime" id="endTimeInput">
                    <input type="hidden" name="requiredKwh" id="requiredKwhInput">
                    <input type="hidden" name="estimatedMinutes" id="estimatedMinutesInput">
                    <input type="hidden" name="estimatedCost" id="estimatedCostInput">

                    <!-- 충전소 정보 -->
                    <div class="form-section">
                        <h3>충전소 정보</h3>

                        <div class="station-box">
                            <div>
                                <strong>부산 사상 EV 충전소</strong>
                                <p>부산 사상구 괘법동 518-1</p>
                            </div>
                            <span>운영중</span>
                        </div>
                    </div>

                    <!-- 차량 선택 -->
                    <div class="form-section">
                        <h3>차량 선택</h3>

                        <div class="form-group">
                            <label for="vehicleId">예약 차량</label>
                            <select id="vehicleId" name="vehicleId">
                                <option value="1"
                                    data-name="아이오닉 5"
                                    data-battery="77.4"
                                    data-connector="DC콤보">
                                    아이오닉 5 / 77.4kWh / DC콤보
                                </option>

                                <option value="2"
                                    data-name="EV6"
                                    data-battery="70.4"
                                    data-connector="DC콤보">
                                    EV6 / 70.4kWh / DC콤보
                                </option>
                            </select>
                        </div>
                    </div>

                    <!-- 충전기 선택 -->
                    <div class="form-section">
                        <h3>충전기 선택</h3>

                        <div class="charger-list">

                            <label class="charger-option">
                                <input type="radio" name="chargerId" value="1"
                                    data-name="급속 1번"
                                    data-speed="200"
                                    data-price="250"
                                    checked>

                                <div class="charger-content">
                                    <strong>급속 1번</strong>
                                    <p>DC콤보 · 200kW · 250원/kWh</p>
                                    <span class="available">사용 가능</span>
                                </div>
                            </label>

                            <label class="charger-option">
                                <input type="radio" name="chargerId" value="2"
                                    data-name="급속 2번"
                                    data-speed="100"
                                    data-price="250">

                                <div class="charger-content">
                                    <strong>급속 2번</strong>
                                    <p>DC콤보 · 100kW · 250원/kWh</p>
                                    <span class="available">사용 가능</span>
                                </div>
                            </label>

                            <label class="charger-option">
                                <input type="radio" name="chargerId" value="3"
                                    data-name="완속 1번"
                                    data-speed="7"
                                    data-price="200">

                                <div class="charger-content">
                                    <strong>완속 1번</strong>
                                    <p>AC완속 · 7kW · 200원/kWh</p>
                                    <span class="available">사용 가능</span>
                                </div>
                            </label>

                        </div>
                    </div>

                    <!-- 예약 시간 -->
                    <div class="form-section">
                        <h3>예약 시간</h3>

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="reservationDate">예약 날짜</label>
                                <input type="date" id="reservationDate" name="reservationDate">
                            </div>

                            <div class="form-group">
                                <label for="startTimeValue">시작 시간</label>
                                <input type="time" id="startTimeValue" name="startTimeValue" value="18:00">
                            </div>
                        </div>
                    </div>

                    <!-- 배터리 잔량 -->
                    <div class="form-section">
                        <h3>배터리 잔량</h3>

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="currentSoc">현재 배터리 잔량</label>
                                <input type="number" id="currentSoc" name="currentSoc" min="0" max="100" value="30">
                                <p class="help-text">예약 시 현재 배터리 잔량입니다.</p>
                            </div>

                            <div class="form-group">
                                <label for="targetSoc">목표 배터리 잔량</label>
                                <input type="number" id="targetSoc" name="targetSoc" min="1" max="100" value="80">
                                <p class="help-text">현재 잔량보다 높아야 합니다.</p>
                            </div>
                        </div>
                    </div>

                    <div class="form-buttons">
                        <button type="button" class="calc-btn" id="calcBtn">예상 결과 계산</button>
                        <button type="submit" class="submit-btn">예약 확정</button>
                    </div>

                </form>

            </section>

            <!-- 오른쪽: 예약 요약 -->
            <aside class="reservation-summary-card">

                <div class="summary-header">
                    <h2>예약 예상 결과</h2>
                    <p>입력한 예약 조건을 기준으로 계산됩니다.</p>
                </div>

                <div class="summary-station">
                    <span class="summary-icon">⚡</span>
                    <div>
                        <strong>부산 사상 EV 충전소</strong>
                        <p id="summaryCharger">급속 1번 · DC콤보</p>
                    </div>
                </div>

                <div class="summary-list">

                    <div>
                        <span>선택 차량</span>
                        <strong id="summaryVehicle">아이오닉 5</strong>
                    </div>

                    <div>
                        <span>배터리 용량</span>
                        <strong id="summaryBattery">77.4kWh</strong>
                    </div>

                    <div>
                        <span>현재 → 목표</span>
                        <strong id="summarySoc">30% → 80%</strong>
                    </div>

                    <div>
                        <span>예상 필요 충전량</span>
                        <strong id="summaryRequiredKwh">38.70kWh</strong>
                    </div>

                    <div>
                        <span>충전기 출력</span>
                        <strong id="summarySpeed">200kW</strong>
                    </div>

                    <div>
                        <span>예상 충전 시간</span>
                        <strong id="summaryMinutes">12분</strong>
                    </div>

                    <div>
                        <span>예약 종료 예정</span>
                        <strong id="summaryEndTime">18:12</strong>
                    </div>

                    <div class="cost-row">
                        <span>예상 충전 비용</span>
                        <strong id="summaryCost">9,675원</strong>
                    </div>

                </div>

                <div class="summary-notice">
                    <p>※ 예약 확정 후 충전 인증 코드가 발급됩니다.</p>
					<p>※ 예약 시간에 충전소에서 인증 코드를 입력하면 충전이 가능합니다.</p>
					<p>※ 예약 시간 내 인증하지 않으면 노쇼로 처리될 수 있습니다.</p>
                </div>

            </aside>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

<script>
    const vehicleSelect = document.getElementById("vehicleId");
    const currentSocInput = document.getElementById("currentSoc");
    const targetSocInput = document.getElementById("targetSoc");
    const startTimeValueInput = document.getElementById("startTimeValue");
    const reservationDateInput = document.getElementById("reservationDate");
    const calcBtn = document.getElementById("calcBtn");
    const form = document.getElementById("reservationForm");

    const startTimeInput = document.getElementById("startTimeInput");
    const endTimeInput = document.getElementById("endTimeInput");
    const requiredKwhInput = document.getElementById("requiredKwhInput");
    const estimatedMinutesInput = document.getElementById("estimatedMinutesInput");
    const estimatedCostInput = document.getElementById("estimatedCostInput");

    function getSelectedCharger() {
        return document.querySelector("input[name='chargerId']:checked");
    }

    function formatNumber(value) {
        return Number(value).toLocaleString("ko-KR");
    }

    function calculateReservation() {
        const selectedVehicle = vehicleSelect.options[vehicleSelect.selectedIndex];
        const selectedCharger = getSelectedCharger();

        const vehicleName = selectedVehicle.dataset.name;
        const batteryCapacity = Number(selectedVehicle.dataset.battery);
        const connectorType = selectedVehicle.dataset.connector;

        const chargerName = selectedCharger.dataset.name;
        const chargingSpeed = Number(selectedCharger.dataset.speed);
        const pricePerKwh = Number(selectedCharger.dataset.price);

        const currentSoc = Number(currentSocInput.value);
        const targetSoc = Number(targetSocInput.value);

        if (currentSoc < 0 || currentSoc > 100 || targetSoc < 0 || targetSoc > 100) {
            alert("배터리 잔량은 0부터 100 사이로 입력해주세요.");
            return false;
        }

        if (targetSoc <= currentSoc) {
            alert("목표 배터리 잔량은 현재 배터리 잔량보다 높아야 합니다.");
            targetSocInput.focus();
            return false;
        }

        const requiredKwh = batteryCapacity * (targetSoc - currentSoc) / 100;
        const estimatedMinutes = Math.ceil((requiredKwh / chargingSpeed) * 60);
        const estimatedCost = Math.round(requiredKwh * pricePerKwh);

        const startTimeValue = startTimeValueInput.value;
        let endTimeText = "-";

        if (startTimeValue) {
            const timeParts = startTimeValue.split(":");
            const endDate = new Date();

            endDate.setHours(Number(timeParts[0]));
            endDate.setMinutes(Number(timeParts[1]) + estimatedMinutes);

            const hour = String(endDate.getHours()).padStart(2, "0");
            const minute = String(endDate.getMinutes()).padStart(2, "0");

            endTimeText = hour + ":" + minute;
        }

        document.getElementById("summaryVehicle").textContent = vehicleName;
        document.getElementById("summaryBattery").textContent = batteryCapacity.toFixed(1) + "kWh";
        document.getElementById("summarySoc").textContent = currentSoc + "% → " + targetSoc + "%";
        document.getElementById("summaryRequiredKwh").textContent = requiredKwh.toFixed(2) + "kWh";
        document.getElementById("summarySpeed").textContent = chargingSpeed + "kW";
        document.getElementById("summaryMinutes").textContent = estimatedMinutes + "분";
        document.getElementById("summaryEndTime").textContent = endTimeText;
        document.getElementById("summaryCost").textContent = formatNumber(estimatedCost) + "원";
        document.getElementById("summaryCharger").textContent = chargerName + " · " + connectorType;

        requiredKwhInput.value = requiredKwh.toFixed(2);
        estimatedMinutesInput.value = estimatedMinutes;
        estimatedCostInput.value = estimatedCost;

        if (reservationDateInput.value && startTimeValue) {
            startTimeInput.value = reservationDateInput.value + "T" + startTimeValue;
            endTimeInput.value = reservationDateInput.value + "T" + endTimeText;
        }

        return true;
    }

    calcBtn.addEventListener("click", calculateReservation);

    vehicleSelect.addEventListener("change", calculateReservation);
    currentSocInput.addEventListener("input", calculateReservation);
    targetSocInput.addEventListener("input", calculateReservation);
    startTimeValueInput.addEventListener("change", calculateReservation);
    reservationDateInput.addEventListener("change", calculateReservation);

    document.querySelectorAll("input[name='chargerId']").forEach(function(radio) {
        radio.addEventListener("change", calculateReservation);
    });

    form.addEventListener("submit", function(e) {
        const calculated = calculateReservation();

        if (!calculated) {
            e.preventDefault();
            return;
        }

        if (!reservationDateInput.value) {
            alert("예약 날짜를 선택해주세요.");
            reservationDateInput.focus();
            e.preventDefault();
            return;
        }

        if (!startTimeValueInput.value) {
            alert("예약 시작 시간을 선택해주세요.");
            startTimeValueInput.focus();
            e.preventDefault();
        }
    });

    calculateReservation();
</script>

</body>
</html>