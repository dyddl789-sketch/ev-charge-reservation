<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 등록/관리</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/station_manage.css">

<script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
<script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJavascriptKey}&libraries=services&autoload=false"></script>
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

            <!-- 제목 영역: 등록/수정 모드 분리 -->
            <section class="admin-title-row">
                <div>
                    <c:choose>
                        <c:when test="${station.editMode}">
                            <h1>충전소 수정</h1>
                            <p>기존 충전소 정보와 충전기 목록을 수정합니다.</p>
                        </c:when>

                        <c:otherwise>
                            <h1>충전소 등록</h1>
                            <p>다음 주소검색과 카카오 좌표 변환으로 충전소를 등록합니다.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <a href="${pageContext.request.contextPath}/admin/station/list"
                   class="detail-btn">
                    목록으로
                </a>
            </section>

            <!-- 수정 화면에서만 요약 표시 -->
            <c:if test="${station.editMode}">
                <section class="station-info-summary">

                    <article>
                        <span>충전소 ID</span>
                        <strong>${station.stationId}</strong>
                    </article>

                    <article>
                        <span>등록 충전기</span>
                        <strong>
                            <fmt:formatNumber value="${station.chargerCount}" pattern="#,###"/>대
                        </strong>
                    </article>

                    <article>
                        <span>사용 가능</span>
                        <strong>
                            <fmt:formatNumber value="${station.availableChargerCount}" pattern="#,###"/>대
                        </strong>
                    </article>

                    <article>
                        <span>화면 모드</span>
                        <strong>수정</strong>
                    </article>

                </section>
            </c:if>

            <form action="${pageContext.request.contextPath}/admin/station/${station.editMode ? 'update' : 'register'}"
                  method="post"
                  id="stationForm">

                <input type="hidden"
                       name="stationId"
                       value="${station.stationId}">

                <div id="deleteChargerArea"></div>

                <!-- 충전소 기본 정보 -->
                <section class="station-form-card">

                    <div class="form-section-title">
                        <h2>
                            <c:choose>
                                <c:when test="${station.editMode}">
                                    충전소 기본 정보 수정
                                </c:when>
                                <c:otherwise>
                                    충전소 기본 정보 등록
                                </c:otherwise>
                            </c:choose>
                        </h2>

                        <p>
                            주소 검색 버튼을 통해 주소를 선택하면 위도/경도가 자동 입력됩니다.
                        </p>
                    </div>

                    <div class="station-form-grid">

                        <div class="form-field">
                            <label>충전소명</label>
                            <input type="text"
                                   name="stationName"
                                   value="${station.stationName}"
                                   placeholder="예: 부산역 EV 충전소"
                                   required>
                        </div>

                        <div class="form-field">
                            <label>운영기관</label>
                            <input type="text"
                                   name="operatorName"
                                   value="${station.operatorName}"
                                   placeholder="예: EV Charge">
                        </div>

                        <div class="form-field full address-field">
                            <label>주소</label>

                            <div class="address-input-row">
                                <input type="text"
                                       id="address"
                                       name="address"
                                       value="${station.address}"
                                       placeholder="주소 검색 버튼을 눌러 주소를 선택하세요."
                                       readonly
                                       required>

                                <button type="button"
                                        class="address-btn"
                                        id="addressSearchBtn">
                                    주소 검색
                                </button>
                            </div>
                        </div>

                        <div class="form-field">
                            <label>위도</label>
                            <input type="number"
                                   step="0.000001"
                                   id="latitude"
                                   name="latitude"
                                   value="${station.latitude}"
                                   placeholder="주소 선택 시 자동 입력"
                                   readonly
                                   required>
                        </div>

                        <div class="form-field">
                            <label>경도</label>
                            <input type="number"
                                   step="0.000001"
                                   id="longitude"
                                   name="longitude"
                                   value="${station.longitude}"
                                   placeholder="주소 선택 시 자동 입력"
                                   readonly
                                   required>
                        </div>

                        <div class="form-field">
                            <label>운영 시작 시간</label>
                            <input type="time"
                                   name="openTime"
                                   value="${empty station.openTime ? '00:00' : station.openTime}"
                                   required>
                        </div>

                        <div class="form-field">
                            <label>운영 종료 시간</label>
                            <input type="time"
                                   name="closeTime"
                                   value="${empty station.closeTime ? '23:59' : station.closeTime}"
                                   required>
                        </div>

                        <div class="form-field">
                            <label>운영 상태</label>
                            <select name="stationStatus"
                                    required>
                                <option value="운영중" ${station.stationStatus == '운영중' ? 'selected' : ''}>운영중</option>
                                <option value="점검중" ${station.stationStatus == '점검중' ? 'selected' : ''}>점검중</option>
                                <option value="운영중지" ${station.stationStatus == '운영중지' ? 'selected' : ''}>운영중지</option>
                            </select>
                        </div>

                    </div>

                </section>

                <!-- 충전기 정보 -->
                <section class="charger-form-card">

                    <div class="form-section-title row">
                        <div>
                            <h2>
                                <c:choose>
                                    <c:when test="${station.editMode}">
                                        충전기 목록 수정
                                    </c:when>
                                    <c:otherwise>
                                        충전기 등록
                                    </c:otherwise>
                                </c:choose>
                            </h2>

                            <p>
                                기존 충전기는 ID를 유지한 채 수정하고, 새 충전기는 추가 등록됩니다.
                            </p>
                        </div>

                        <button type="button"
                                class="add-charger-btn"
                                id="addChargerBtn">
                            + 충전기 추가
                        </button>
                    </div>

                    <div class="charger-table-wrap">

                        <table class="charger-table">

                            <thead>
                            <tr>
                                <th>충전기 ID</th>
                                <th>충전기명</th>
                                <th>충전 타입</th>
                                <th>커넥터 타입</th>
                                <th>출력(kW)</th>
                                <th>요금(원/kWh)</th>
                                <th>상태</th>
                                <th>삭제</th>
                            </tr>
                            </thead>

                            <tbody id="chargerTableBody">

                            <c:forEach var="charger"
                                       items="${station.chargerList}"
                                       varStatus="status">

                                <tr>
                                    <td>
                                        <input type="hidden"
                                               name="chargerList[${status.index}].chargerId"
                                               value="${charger.chargerId}">

                                        <span class="charger-id-text">
                                            ${charger.chargerId}
                                        </span>
                                    </td>

                                    <td>
                                        <input type="text"
                                               name="chargerList[${status.index}].chargerName"
                                               value="${charger.chargerName}"
                                               required>
                                    </td>

                                    <td>
                                        <select name="chargerList[${status.index}].chargerType"
                                                required>
                                            <option value="급속" ${charger.chargerType == '급속' ? 'selected' : ''}>급속</option>
                                            <option value="완속" ${charger.chargerType == '완속' ? 'selected' : ''}>완속</option>
                                            <option value="초급속" ${charger.chargerType == '초급속' ? 'selected' : ''}>초급속</option>
                                        </select>
                                    </td>

                                    <td>
                                        <select name="chargerList[${status.index}].connectorType"
                                                required>
                                            <option value="DC콤보" ${charger.connectorType == 'DC콤보' ? 'selected' : ''}>DC콤보</option>
                                            <option value="AC완속" ${charger.connectorType == 'AC완속' ? 'selected' : ''}>AC완속</option>
                                            <option value="NACS" ${charger.connectorType == 'NACS' ? 'selected' : ''}>NACS</option>
                                            <option value="CHAdeMO" ${charger.connectorType == 'CHAdeMO' ? 'selected' : ''}>CHAdeMO</option>
                                        </select>
                                    </td>

                                    <td>
                                        <input type="number"
                                               name="chargerList[${status.index}].chargingSpeedKw"
                                               value="${charger.chargingSpeedKw}"
                                               min="1"
                                               step="0.1"
                                               required>
                                    </td>

                                    <td>
                                        <input type="number"
                                               name="chargerList[${status.index}].pricePerKwh"
                                               value="${charger.pricePerKwh}"
                                               min="0"
                                               required>
                                    </td>

                                    <td>
                                        <select name="chargerList[${status.index}].status"
                                                required>
                                            <option value="사용가능" ${charger.status == '사용가능' ? 'selected' : ''}>사용가능</option>
                                            <option value="예약중" ${charger.status == '예약중' ? 'selected' : ''}>예약중</option>
                                            <option value="사용중" ${charger.status == '사용중' ? 'selected' : ''}>사용중</option>
                                            <option value="점검중" ${charger.status == '점검중' ? 'selected' : ''}>점검중</option>
                                            <option value="고장" ${charger.status == '고장' ? 'selected' : ''}>고장</option>
                                        </select>
                                    </td>

                                    <td>
                                        <button type="button"
                                                class="remove-charger-btn"
                                                data-charger-id="${charger.chargerId}">
                                            삭제
                                        </button>
                                    </td>
                                </tr>

                            </c:forEach>

                            </tbody>

                        </table>

                    </div>

                    <p class="form-help-text">
                        충전기는 최소 1개 이상 등록해야 합니다. 예약/충전 이력이 있는 충전기는 DB에서 삭제되지 않을 수 있습니다.
                    </p>

                </section>

                <!-- 버튼 영역 -->
                <section class="form-action-row">

                    <a href="${pageContext.request.contextPath}/admin/station/list"
                       class="cancel-btn">
                        취소
                    </a>

                    <button type="submit"
                            class="submit-btn ${station.editMode ? 'update-mode' : 'register-mode'}">
                        ${station.editMode ? '수정 저장' : '충전소 등록'}
                    </button>

                </section>

            </form>

        </main>

    </div>

</div>

<script>
    const addChargerBtn = document.getElementById("addChargerBtn");
    const chargerTableBody = document.getElementById("chargerTableBody");
    const stationForm = document.getElementById("stationForm");
    const deleteChargerArea = document.getElementById("deleteChargerArea");

    const addressInput = document.getElementById("address");
    const latitudeInput = document.getElementById("latitude");
    const longitudeInput = document.getElementById("longitude");
    const addressSearchBtn = document.getElementById("addressSearchBtn");

    let chargerIndex = chargerTableBody.querySelectorAll("tr").length;

    // 카카오 주소 검색 후 좌표 자동 입력
    addressSearchBtn.addEventListener("click", function() {

        new daum.Postcode({
            oncomplete: function(data) {

                const selectedAddress = data.roadAddress || data.jibunAddress;

                if (!selectedAddress) {
                    alert("선택된 주소가 없습니다.");
                    return;
                }

                addressInput.value = selectedAddress;

                kakao.maps.load(function() {

                    const geocoder = new kakao.maps.services.Geocoder();

                    geocoder.addressSearch(selectedAddress, function(result, status) {

                        if (status !== kakao.maps.services.Status.OK
                                || !result
                                || result.length === 0) {

                            alert("주소의 좌표를 찾을 수 없습니다.");
                            return;
                        }

                        longitudeInput.value = result[0].x;
                        latitudeInput.value = result[0].y;
                    });
                });
            }
        }).open();
    });

    // 충전기 입력 행 추가
    function addChargerRow() {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>
                <input type="hidden"
                       name="chargerList[\${chargerIndex}].chargerId"
                       value="">
                <span class="charger-id-text new">신규</span>
            </td>

            <td>
                <input type="text"
                       name="chargerList[\${chargerIndex}].chargerName"
                       placeholder="예: 급속충전기 1"
                       required>
            </td>

            <td>
                <select name="chargerList[\${chargerIndex}].chargerType"
                        required>
                    <option value="급속">급속</option>
                    <option value="완속">완속</option>
                    <option value="초급속">초급속</option>
                </select>
            </td>

            <td>
                <select name="chargerList[\${chargerIndex}].connectorType"
                        required>
                    <option value="DC콤보">DC콤보</option>
                    <option value="AC완속">AC완속</option>
                    <option value="NACS">NACS</option>
                    <option value="CHAdeMO">CHAdeMO</option>
                </select>
            </td>

            <td>
                <input type="number"
                       name="chargerList[\${chargerIndex}].chargingSpeedKw"
                       min="1"
                       step="0.1"
                       placeholder="100"
                       required>
            </td>

            <td>
                <input type="number"
                       name="chargerList[\${chargerIndex}].pricePerKwh"
                       min="0"
                       placeholder="347"
                       required>
            </td>

            <td>
                <select name="chargerList[\${chargerIndex}].status"
                        required>
                    <option value="사용가능">사용가능</option>
                    <option value="예약중">예약중</option>
                    <option value="사용중">사용중</option>
                    <option value="점검중">점검중</option>
                    <option value="고장">고장</option>
                </select>
            </td>

            <td>
                <button type="button"
                        class="remove-charger-btn">
                    삭제
                </button>
            </td>
        `;

        chargerTableBody.appendChild(row);
        chargerIndex++;
    }

    addChargerBtn.addEventListener("click", function() {
        addChargerRow();
    });

    // 충전기 행 삭제
    chargerTableBody.addEventListener("click", function(event) {

        if (!event.target.classList.contains("remove-charger-btn")) {
            return;
        }

        const row = event.target.closest("tr");
        const chargerId = event.target.dataset.chargerId;

        if (chargerId) {
            const hidden = document.createElement("input");

            hidden.type = "hidden";
            hidden.name = "deleteChargerIds";
            hidden.value = chargerId;

            deleteChargerArea.appendChild(hidden);
        }

        row.remove();
    });

    // 등록/수정 전 검증
    stationForm.addEventListener("submit", function(event) {

        const chargerRows = chargerTableBody.querySelectorAll("tr");

        if (chargerRows.length === 0) {
            alert("충전기를 최소 1개 이상 등록해 주세요.");
            event.preventDefault();
            return;
        }

        if (!addressInput.value || !latitudeInput.value || !longitudeInput.value) {
            alert("주소 검색을 통해 주소와 좌표를 입력해 주세요.");
            event.preventDefault();
            return;
        }

        const result = confirm("${station.editMode ? '충전소 정보를 수정하시겠습니까?' : '충전소를 등록하시겠습니까?'}");

        if (!result) {
            event.preventDefault();
        }
    });

    // 신규 등록 화면에서는 기본 충전기 1개 자동 추가
    if (chargerIndex === 0) {
        addChargerRow();
    }
</script>

</body>
</html>