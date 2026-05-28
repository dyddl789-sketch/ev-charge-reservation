<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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

            <a href="${pageContext.request.contextPath}/station/map" class="back-btn">
                충전소 탐색으로
            </a>
        </section>

        <section class="reservation-layout">

            <!-- 왼쪽: 예약 입력 -->
            <section class="reservation-form-card">

                <div class="section-title">
                    <h2>예약 정보 입력</h2>
                    <p>예약 조건을 입력하면 예상 충전량, 시간, 비용이 자동 계산됩니다.</p>
                </div>

                <form action="${pageContext.request.contextPath}/reservation/register"
                      method="post"
                      id="reservationForm">

                    <!-- 예약 저장에 필요한 값 -->
                    <input type="hidden" name="chargerId" value="${charger.chargerId}">
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
                                <strong>${charger.stationName}</strong>
                                <p>${charger.address}</p>
                            </div>
                            <span>${charger.stationStatus}</span>
                        </div>
                    </div>

                    <!-- 차량 선택 -->
                    <div class="form-section">
                        <h3>차량 선택</h3>

                        <div class="form-group">
                            <label for="vehicleId">예약 차량</label>

                            <c:choose>
                                <c:when test="${empty vehicleList}">
                                    <select id="vehicleId" name="vehicleId" disabled>
                                        <option value="">등록된 차량이 없습니다.</option>
                                    </select>

                                    <p class="help-text">
                                        예약을 진행하려면 먼저 내 차량을 등록해야 합니다.
                                    </p>
                                </c:when>

                                <c:otherwise>
                                    <select id="vehicleId" name="vehicleId">
                                        <c:forEach var="vehicle" items="${vehicleList}">
                                            <option value="${vehicle.vehicleId}"
                                                    data-name="${vehicle.modelName}"
                                                    data-battery="${vehicle.batteryCapacityKwh}"
                                                    data-connector="${vehicle.connectorType}">
                                                ${vehicle.modelName} / ${vehicle.batteryCapacityKwh}kWh / ${vehicle.connectorType}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- 충전기 정보 -->
                    <div class="form-section">
                        <h3>충전기 정보</h3>

                        <div class="charger-list">
                            <label class="charger-option">
                                <input type="radio"
                                       name="selectedCharger"
                                       value="${charger.chargerId}"
                                       data-name="${charger.chargerName}"
                                       data-connector="${charger.connectorType}"
                                       data-speed="${charger.chargingSpeedKw}"
                                       data-price="${charger.pricePerKwh}"
                                       checked>

                                <div class="charger-content">
                                    <strong>${charger.chargerName}</strong>
                                    <p>
                                        ${charger.connectorType}
                                        · ${charger.chargingSpeedKw}kW
                                        · ${charger.pricePerKwh}원/kWh
                                    </p>
                                    <span class="available">${charger.chargerStatus}</span>
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

                                <select id="startTimeValue" name="startTimeValue">
								    <c:forEach var="hour" begin="0" end="23">
								        <fmt:formatNumber value="${hour}" pattern="00" var="hourText" />
								
								        <c:forEach var="minute" begin="0" end="55" step="5">
								            <fmt:formatNumber value="${minute}" pattern="00" var="minuteText" />
								
								            <c:choose>
								                <c:when test="${hour == 18 and minute == 0}">
								                    <option value="${hourText}:${minuteText}" selected="selected">
								                        ${hourText}:${minuteText}
								                    </option>
								                </c:when>
								
								                <c:otherwise>
								                    <option value="${hourText}:${minuteText}">
								                        ${hourText}:${minuteText}
								                    </option>
								                </c:otherwise>
								            </c:choose>
								        </c:forEach>
								    </c:forEach>
								</select>

                                <p class="help-text">예약을 시작할 시간을 선택하세요.</p>
                            </div>
                        </div>
                    </div>

                    <!-- 배터리 잔량 -->
                    <div class="form-section">
                        <h3>배터리 잔량</h3>

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="currentSoc">현재 배터리 잔량</label>
                                <input type="number"
                                       id="currentSoc"
                                       name="currentSoc"
                                       min="0"
                                       max="100"
                                       value="30">
                                <p class="help-text">예약 시 현재 배터리 잔량입니다.</p>
                            </div>

                            <div class="form-group">
                                <label for="targetSoc">목표 배터리 잔량</label>
                                <input type="number"
                                       id="targetSoc"
                                       name="targetSoc"
                                       min="1"
                                       max="100"
                                       value="80">
                                <p class="help-text">현재 잔량보다 높아야 합니다.</p>
                            </div>
                        </div>
                    </div>

                    <div class="form-buttons">
                        <c:choose>
                            <c:when test="${empty vehicleList}">
                                <button type="submit" class="submit-btn" disabled>
                                    예약
                                </button>
                            </c:when>

                            <c:otherwise>
                                <button type="submit" class="submit-btn">
                                    예약
                                </button>
                            </c:otherwise>
                        </c:choose>
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
                        <strong>${charger.stationName}</strong>
                        <p id="summaryCharger">
                            ${charger.chargerName} · ${charger.connectorType}
                        </p>
                    </div>
                </div>

                <div class="summary-list">
                    <div>
                        <span>선택 차량</span>
                        <strong id="summaryVehicle">-</strong>
                    </div>

                    <div>
                        <span>배터리 용량</span>
                        <strong id="summaryBattery">-</strong>
                    </div>

                    <div>
                        <span>현재 → 목표</span>
                        <strong id="summarySoc">30% → 80%</strong>
                    </div>

                    <div>
                        <span>예상 필요 충전량</span>
                        <strong id="summaryRequiredKwh">-</strong>
                    </div>

                    <div>
                        <span>충전기 출력</span>
                        <strong id="summarySpeed">${charger.chargingSpeedKw}kW</strong>
                    </div>

                    <div>
                        <span>예상 충전 시간</span>
                        <strong id="summaryMinutes">-</strong>
                    </div>

                    <div>
                        <span>예약 종료 예정</span>
                        <strong id="summaryEndTime">-</strong>
                    </div>

                    <div class="cost-row">
                        <span>예상 충전 비용</span>
                        <strong id="summaryCost">-</strong>
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

<c:if test="${not empty errorMsg}">
    <script>
        alert("${errorMsg}");
    </script>
</c:if>

<script>
    const vehicleSelect = document.getElementById("vehicleId");
    const currentSocInput = document.getElementById("currentSoc");
    const targetSocInput = document.getElementById("targetSoc");
    const startTimeValueInput = document.getElementById("startTimeValue");
    const reservationDateInput = document.getElementById("reservationDate");
    const form = document.getElementById("reservationForm");

    const startTimeInput = document.getElementById("startTimeInput");
    const endTimeInput = document.getElementById("endTimeInput");
    const requiredKwhInput = document.getElementById("requiredKwhInput");
    const estimatedMinutesInput = document.getElementById("estimatedMinutesInput");
    const estimatedCostInput = document.getElementById("estimatedCostInput");

    function getSelectedCharger() {
        return document.querySelector("input[name='selectedCharger']:checked");
    }

    function formatNumber(value) {
        return Number(value).toLocaleString("ko-KR");
    }

    function pad(value) {
        return String(value).padStart(2, "0");
    }

    function formatTimeText(date) {
        return pad(date.getHours()) + ":" + pad(date.getMinutes());
    }

    /*
     * LocalDateTime으로 서버에 보내기 위한 형식
     *
     * 서버 DTO:
     * LocalDateTime startTime
     * LocalDateTime endTime
     *
     * 전송 예:
     * 2026-05-28T23:50
     */
    function formatDateTimeLocal(date) {
        return date.getFullYear()
            + "-"
            + pad(date.getMonth() + 1)
            + "-"
            + pad(date.getDate())
            + "T"
            + pad(date.getHours())
            + ":"
            + pad(date.getMinutes());
    }

    /*
     * 오른쪽 예약 요약에 보여줄 날짜 + 시간 문자열
     *
     * 표시 예:
     * 2026-05-29 00:30
     */
    function formatDateTimeText(date) {
        return formatDateTimeLocal(date).replace("T", " ");
    }

    function setTodayDefault() {
        if (reservationDateInput.value) {
            return;
        }

        const today = new Date();
        const year = today.getFullYear();
        const month = pad(today.getMonth() + 1);
        const day = pad(today.getDate());

        reservationDateInput.value = year + "-" + month + "-" + day;
    }

    /*
     * 예약 예상 결과 계산
     *
     * showAlert:
     * - false: 입력 중 자동 계산용, alert를 띄우지 않음
     * - true : 예약 버튼 클릭 시 검증용, alert를 띄움
     */
    function calculateReservation(showAlert) {
        showAlert = showAlert === true;

        if (!vehicleSelect || vehicleSelect.disabled || vehicleSelect.options.length === 0) {
            return false;
        }

        const selectedVehicle = vehicleSelect.options[vehicleSelect.selectedIndex];
        const selectedCharger = getSelectedCharger();

        if (!selectedVehicle || !selectedCharger) {
            return false;
        }

        const vehicleName = selectedVehicle.dataset.name;
        const batteryCapacity = Number(selectedVehicle.dataset.battery);

        const chargerName = selectedCharger.dataset.name;
        const chargerConnector = selectedCharger.dataset.connector;
        const chargingSpeed = Number(selectedCharger.dataset.speed);
        const pricePerKwh = Number(selectedCharger.dataset.price);

        /*
         * input 값이 비어 있는 상태를 먼저 검사한다.
         *
         * 숫자를 입력하는 도중에는 값이 잠깐 비거나,
         * 현재 배터리보다 낮은 값이 될 수 있다.
         * 이때는 alert를 띄우지 않고 계산만 중단한다.
         */
        if (currentSocInput.value === "" || targetSocInput.value === "") {
            if (showAlert) {
                alert("배터리 잔량을 입력해주세요.");
            }

            return false;
        }

        const currentSoc = Number(currentSocInput.value);
        const targetSoc = Number(targetSocInput.value);

        if (!batteryCapacity || batteryCapacity <= 0) {
            if (showAlert) {
                alert("차량 배터리 정보를 확인할 수 없습니다.");
            }

            return false;
        }

        if (!chargingSpeed || chargingSpeed <= 0) {
            if (showAlert) {
                alert("충전기 출력 정보를 확인할 수 없습니다.");
            }

            return false;
        }

        if (pricePerKwh < 0 || Number.isNaN(pricePerKwh)) {
            if (showAlert) {
                alert("충전 요금 정보를 확인할 수 없습니다.");
            }

            return false;
        }

        if (currentSoc < 0 || currentSoc > 100 || targetSoc < 0 || targetSoc > 100) {
            if (showAlert) {
                alert("배터리 잔량은 0부터 100 사이로 입력해주세요.");
            }

            return false;
        }

        if (targetSoc <= currentSoc) {
            if (showAlert) {
                alert("목표 배터리 잔량은 현재 배터리 잔량보다 높아야 합니다.");
                targetSocInput.focus();
            }

            return false;
        }

        const requiredKwh = batteryCapacity * (targetSoc - currentSoc) / 100;
        const estimatedMinutes = Math.ceil((requiredKwh / chargingSpeed) * 60);
        const estimatedCost = Math.round(requiredKwh * pricePerKwh);

        const startTimeValue = startTimeValueInput.value;
        let endTimeText = "-";

        if (reservationDateInput.value && startTimeValue) {
            const startDateTime = new Date(reservationDateInput.value + "T" + startTimeValue);
            const endDateTime = new Date(startDateTime.getTime() + estimatedMinutes * 60 * 1000);

            /*
             * 서버 전송용 hidden 값
             *
             * 예:
             * startTime = 2026-05-28T23:50
             * endTime   = 2026-05-29T00:30
             */
            startTimeInput.value = formatDateTimeLocal(startDateTime);
            endTimeInput.value = formatDateTimeLocal(endDateTime);

            /*
             * 화면 표시용 값
             */
            endTimeText = formatDateTimeText(endDateTime);
        }

        document.getElementById("summaryVehicle").textContent = vehicleName;
        document.getElementById("summaryBattery").textContent = batteryCapacity.toFixed(1) + "kWh";
        document.getElementById("summarySoc").textContent = currentSoc + "% → " + targetSoc + "%";
        document.getElementById("summaryRequiredKwh").textContent = requiredKwh.toFixed(2) + "kWh";
        document.getElementById("summarySpeed").textContent = chargingSpeed + "kW";
        document.getElementById("summaryMinutes").textContent = estimatedMinutes + "분";
        document.getElementById("summaryEndTime").textContent = endTimeText;
        document.getElementById("summaryCost").textContent = formatNumber(estimatedCost) + "원";
        document.getElementById("summaryCharger").textContent = chargerName + " · " + chargerConnector;

        requiredKwhInput.value = requiredKwh.toFixed(2);
        estimatedMinutesInput.value = estimatedMinutes;
        estimatedCostInput.value = estimatedCost;

        return true;
    }

    /*
     * 입력 중에는 alert 없이 조용히 계산만 시도한다.
     */
    if (vehicleSelect && !vehicleSelect.disabled) {
        vehicleSelect.addEventListener("change", function() {
            calculateReservation(false);
        });
    }

    currentSocInput.addEventListener("input", function() {
        calculateReservation(false);
    });

    targetSocInput.addEventListener("input", function() {
        calculateReservation(false);
    });

    startTimeValueInput.addEventListener("change", function() {
        calculateReservation(false);
    });

    reservationDateInput.addEventListener("change", function() {
        calculateReservation(false);
    });

    document.querySelectorAll("input[name='selectedCharger']").forEach(function(radio) {
        radio.addEventListener("change", function() {
            calculateReservation(false);
        });
    });

    /*
     * 예약 버튼을 눌렀을 때만 alert를 띄운다.
     */
    form.addEventListener("submit", function(e) {
        if (!vehicleSelect || vehicleSelect.disabled) {
            alert("예약할 차량을 먼저 등록해주세요.");
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
            return;
        }

        const calculated = calculateReservation(true);

        if (!calculated) {
            e.preventDefault();
        }
    });

    setTodayDefault();
    calculateReservation(false);
</script>

</body>
</html>