<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 등록/관리</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/station_manage.css">
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
                    <h1>충전소 등록/관리</h1>
                    <p>충전소 기본 정보와 충전기 정보를 등록하고 운영 상태를 관리합니다.</p>
                </div>

                <a href="/admin/station/list" class="list-btn">충전소 목록</a>
            </section>

            <section class="manage-layout">

                <!-- 충전소 등록 폼 -->
                <form action="/admin/station/save" method="post" class="station-form">

                    <section class="form-section">
                        <h2>충전소 기본 정보</h2>

                        <div class="form-grid">
                            <div class="form-group">
                                <label for="stationName">충전소명</label>
                                <input type="text" id="stationName" name="stationName" placeholder="예: 부산 사상 EV 충전소">
                            </div>

                            <div class="form-group full">
                                <label for="address">주소</label>
                                <div class="address-row">
                                    <input type="text" id="address" name="address" placeholder="주소를 입력하세요.">
                                    <button type="button" class="address-btn">주소 검색</button>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="latitude">위도</label>
                                <input type="text" id="latitude" name="latitude" placeholder="예: 35.1627">
                            </div>

                            <div class="form-group">
                                <label for="longitude">경도</label>
                                <input type="text" id="longitude" name="longitude" placeholder="예: 129.0520">
                            </div>

                            <div class="form-group">
                                <label for="openTime">운영 시작 시간</label>
                                <input type="time" id="openTime" name="openTime" value="00:00">
                            </div>

                            <div class="form-group">
                                <label for="closeTime">운영 종료 시간</label>
                                <input type="time" id="closeTime" name="closeTime" value="23:59">
                            </div>

                            <div class="form-group">
                                <label for="stationStatus">운영 상태</label>
                                <select id="stationStatus" name="stationStatus">
                                    <option value="운영중">운영중</option>
                                    <option value="점검중">점검중</option>
                                    <option value="운영중지">운영중지</option>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="parkingYn">주차 가능 여부</label>
                                <select id="parkingYn" name="parkingYn">
                                    <option value="Y">가능</option>
                                    <option value="N">불가능</option>
                                </select>
                            </div>
                        </div>
                    </section>

                    <section class="form-section">
                        <h2>충전기 정보</h2>

                        <div class="charger-form-box">

                            <div class="form-grid">
                                <div class="form-group">
                                    <label for="chargerName">충전기명</label>
                                    <input type="text" id="chargerName" name="chargerName" placeholder="예: 급속 1번">
                                </div>

                                <div class="form-group">
                                    <label for="chargerType">충전 타입</label>
                                    <select id="chargerType" name="chargerType">
                                        <option value="">충전 타입 선택</option>
                                        <option value="급속">급속</option>
                                        <option value="완속">완속</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="connectorType">커넥터 타입</label>
                                    <select id="connectorType" name="connectorType">
                                        <option value="">커넥터 타입 선택</option>
                                        <option value="DC콤보">DC콤보</option>
                                        <option value="AC완속">AC완속</option>
                                        <option value="차데모">차데모</option>
                                        <option value="테슬라">테슬라</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="speedKw">충전 속도</label>
                                    <input type="number" id="speedKw" name="speedKw" placeholder="예: 200">
                                    <p class="help-text">단위: kW</p>
                                </div>

                                <div class="form-group">
                                    <label for="pricePerKwh">충전 요금</label>
                                    <input type="number" id="pricePerKwh" name="pricePerKwh" placeholder="예: 250">
                                    <p class="help-text">단위: 원/kWh</p>
                                </div>

                                <div class="form-group">
                                    <label for="chargerStatus">충전기 상태</label>
                                    <select id="chargerStatus" name="chargerStatus">
                                        <option value="사용가능">사용가능</option>
                                        <option value="사용중">사용중</option>
                                        <option value="예약됨">예약됨</option>
                                        <option value="점검중">점검중</option>
                                        <option value="고장">고장</option>
                                    </select>
                                </div>
                            </div>

                            <div class="charger-add-area">
                                <button type="button" class="add-charger-btn">+ 충전기 추가</button>
                            </div>

                        </div>

                        <div class="charger-list-preview">
                            <h3>등록 예정 충전기</h3>

                            <table>
                                <thead>
                                    <tr>
                                        <th>충전기명</th>
                                        <th>충전 타입</th>
                                        <th>커넥터</th>
                                        <th>속도</th>
                                        <th>요금</th>
                                        <th>상태</th>
                                    </tr>
                                </thead>
                                <tbody id="chargerPreviewBody">
								    <tr>
								        <td>급속 1번</td>
								        <td>급속</td>
								        <td>DC콤보</td>
								        <td>200kW</td>
								        <td>250원/kWh</td>
								        <td><span class="status-badge active">사용가능</span></td>
								        <td>
								            <button type="button" class="delete-charger-btn">삭제</button>
								        </td>
								    </tr>
								    <tr>
								        <td>완속 1번</td>
								        <td>완속</td>
								        <td>AC완속</td>
								        <td>7kW</td>
								        <td>200원/kWh</td>
								        <td><span class="status-badge active">사용가능</span></td>
								        <td>
								            <button type="button" class="delete-charger-btn">삭제</button>
								        </td>
								    </tr>
								</tbody>
                            </table>
                        </div>

                    </section>

                    <div class="form-notice">
                        <p>※ 충전소 등록 후 충전소 목록 화면에서 운영 상태와 충전기 정보를 수정할 수 있습니다.</p>
                        <p>※ 실제 DB 연결 시 충전소 정보는 charging_station 테이블, 충전기 정보는 charger 테이블에 저장됩니다.</p>
                    </div>

                    <div class="form-buttons">
                        <button type="submit" class="submit-btn">충전소 등록</button>
                        <button type="reset" class="reset-form-btn">초기화</button>
                    </div>

                </form>

                <!-- 우측 안내 패널 -->
                <aside class="manage-guide">
                    <h2>등록 안내</h2>

                    <ul>
                        <li>충전소명, 주소, 위치 좌표는 충전소 탐색 화면에서 사용됩니다.</li>
                        <li>운영 시간은 예약 가능한 시간 계산에 활용됩니다.</li>
                        <li>충전기 속도와 요금은 예상 충전 시간과 비용 계산에 사용됩니다.</li>
                        <li>충전기 상태가 점검중 또는 고장인 경우 예약 대상에서 제외됩니다.</li>
                    </ul>

                    <div class="guide-box">
                        <strong>추천 입력 순서</strong>
                        <p>충전소 기본 정보 입력 → 충전기 정보 입력 → 등록 예정 충전기 확인 → 충전소 등록</p>
                    </div>
                </aside>

            </section>

        </main>

    </div>

</div>

<script>
    const addChargerBtn = document.querySelector(".add-charger-btn");
    const chargerPreviewBody = document.getElementById("chargerPreviewBody");

    const chargerNameInput = document.getElementById("chargerName");
    const chargerTypeSelect = document.getElementById("chargerType");
    const connectorTypeSelect = document.getElementById("connectorType");
    const speedKwInput = document.getElementById("speedKw");
    const pricePerKwhInput = document.getElementById("pricePerKwh");
    const chargerStatusSelect = document.getElementById("chargerStatus");

    addChargerBtn.addEventListener("click", function() {
        const chargerName = chargerNameInput.value.trim();
        const chargerType = chargerTypeSelect.value;
        const connectorType = connectorTypeSelect.value;
        const speedKw = speedKwInput.value.trim();
        const pricePerKwh = pricePerKwhInput.value.trim();
        const chargerStatus = chargerStatusSelect.value;

        if (chargerName === "") {
            alert("충전기명을 입력해주세요.");
            chargerNameInput.focus();
            return;
        }

        if (chargerType === "") {
            alert("충전 타입을 선택해주세요.");
            chargerTypeSelect.focus();
            return;
        }

        if (connectorType === "") {
            alert("커넥터 타입을 선택해주세요.");
            connectorTypeSelect.focus();
            return;
        }

        if (speedKw === "") {
            alert("충전 속도를 입력해주세요.");
            speedKwInput.focus();
            return;
        }

        if (pricePerKwh === "") {
            alert("충전 요금을 입력해주세요.");
            pricePerKwhInput.focus();
            return;
        }

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${chargerName}</td>
            <td>${chargerType}</td>
            <td>${connectorType}</td>
            <td>${speedKw}kW</td>
            <td>${pricePerKwh}원/kWh</td>
            <td><span class="status-badge active">${chargerStatus}</span></td>
            <td>
                <button type="button" class="delete-charger-btn">삭제</button>
            </td>
        `;

        chargerPreviewBody.appendChild(row);

        chargerNameInput.value = "";
        chargerTypeSelect.value = "";
        connectorTypeSelect.value = "";
        speedKwInput.value = "";
        pricePerKwhInput.value = "";
        chargerStatusSelect.value = "사용가능";

        alert("충전기가 등록 예정 목록에 추가되었습니다.");
    });

    chargerPreviewBody.addEventListener("click", function(e) {
        if (e.target.classList.contains("delete-charger-btn")) {
            const result = confirm("해당 충전기를 목록에서 삭제하시겠습니까?");

            if (!result) {
                return;
            }

            e.target.closest("tr").remove();
        }
    });
</script>

</body>
</html>