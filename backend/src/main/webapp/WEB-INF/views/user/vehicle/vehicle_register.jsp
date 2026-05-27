<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

            <form action="/vehicle/register" method="post" class="vehicle-form">

                <!-- 차량 모델 선택 -->
                <div class="form-section">
                    <h2>차량 모델 선택</h2>

                    <div class="form-grid">

                        <div class="form-group">
                            <label for="modelId">차량 모델</label>

							<select id="modelId" name="modelId" required>
							
							    <option value="">차량 모델 선택</option>
							
							    <c:forEach var="model"
							               items="${vehicleModelList}">
							
							        <option value="${model.modelId}"
							
							                data-manufacturer="${model.manufacturer}"
							
							                data-battery="${model.batteryCapacityKwh}"
							
							                data-connector="${model.connectorType}"
							
							                data-speed="${model.maxChargingSpeedKw}">
							
							            ${model.manufacturer}
							            ${model.modelName}
							
							        </option>
							
							    </c:forEach>
							
							</select>
                        </div>

                        <div class="form-group">
                            <label>제조사</label>
                            <input type="text" id="manufacturer" readonly>
                        </div>

                        <div class="form-group">
                            <label>배터리 용량</label>
                            <input type="text" id="batteryCapacity" readonly>
                        </div>

                        <div class="form-group">
                            <label>충전 타입</label>
                            <input type="text" id="connectorType" readonly>
                        </div>

                        <div class="form-group">
                            <label>최대 충전 속도</label>
                            <input type="text" id="maxChargingSpeed" readonly>
                        </div>

                    </div>
                </div>

                <!-- 사용자 차량 정보 -->
                <div class="form-section">
                    <h2>내 차량 정보</h2>

                    <div class="form-grid">

                        <div class="form-group">
                            <label for="vehicleNickname">차량 별칭</label>
                            <input type="text"
                                   id="vehicleNickname"
                                   name="vehicleNickname"
                                   placeholder="예: 내 차">
                        </div>

                        <div class="form-group">
                            <label for="plateNumber">차량 번호</label>
                            <input type="text"
                                   id="plateNumber"
                                   name="plateNumber"
                                   placeholder="예: 12가 3456">
                        </div>

                        <div class="form-group">
                            <label for="isDefault">기본 차량 여부</label>

                            <select id="isDefault" name="isDefault">
                                <option value="false">아니오</option>
                                <option value="true">예</option>
                            </select>
                        </div>

                    </div>
                </div>

                <div class="form-notice">
                    <p>※ 차량 모델 선택 시 충전 타입 및 배터리 정보가 자동 적용됩니다.</p>
                    <p>※ 기본 차량은 예약 화면에서 자동 선택됩니다.</p>
                </div>

                <div class="form-buttons">
                    <button type="submit" class="submit-btn">차량 등록</button>

                    <a href="/vehicle/list" class="cancel-btn">
                        취소
                    </a>
                </div>

            </form>

            <!-- 안내 영역 -->
            <aside class="guide-box">

                <h2>차량 정보가 필요한 이유</h2>

                <ul>
                    <li>차량 충전 타입에 맞는 충전소를 추천합니다.</li>
                    <li>배터리 용량 기준 예상 충전 시간을 계산합니다.</li>
                    <li>목표 배터리 잔량 기준 예상 비용을 계산합니다.</li>
                    <li>기본 차량은 예약 화면에서 자동 선택됩니다.</li>
                </ul>

            </aside>

        </section>

    </main>

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</div>

<script src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/vehicle/vehicle_register.js"></script>

</body>
</html>