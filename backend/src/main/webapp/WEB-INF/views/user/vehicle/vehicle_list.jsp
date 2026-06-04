<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 내 차량</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/vehicle/vehicle.css">
</head>

<body>

<div class="vehicle-page">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="vehicle-main">

        <section class="page-title">
            <div>
                <h1>내 차량</h1>
                <p>등록된 차량 정보를 기준으로 충전소 추천과 예약 정보를 제공합니다.</p>
            </div>

            <a href="${pageContext.request.contextPath}/vehicle/register" class="add-btn">차량 등록</a>
        </section>

		<section class="vehicle-summary">
		    <div class="summary-card total-card">
		        <span>등록 차량</span>
		        <strong>${vehicleList.size()}대</strong>
		    </div>
		
		    <div class="summary-card default-card">
		        <span>기본 차량</span>
		
		        <strong id="defaultVehicleName">
		            <c:forEach var="vehicle" items="${vehicleList}">
		                <c:if test="${vehicle.isDefault}">
		                    <c:choose>
		                        <c:when test="${not empty vehicle.vehicleNickname}">
		                            ${vehicle.vehicleNickname}
		                        </c:when>
		                        <c:otherwise>
		                            ${vehicle.modelName}
		                        </c:otherwise>
		                    </c:choose>
		                </c:if>
		            </c:forEach>
		        </strong>
		    </div>
		</section>

        <section class="vehicle-list">

            <c:forEach var="vehicle" items="${vehicleList}">

                <article class="vehicle-card ${vehicle.isDefault ? 'main-vehicle' : ''}"
                         data-vehicle-id="${vehicle.vehicleId}">

                    <div class="vehicle-image">
                        <img src="${pageContext.request.contextPath}${vehicle.imageUrl}"
                             alt="${vehicle.modelName}">
                    </div>

                    <div class="vehicle-info">
                        <div class="vehicle-title-row">
                            <div>
                                <c:if test="${vehicle.isDefault}">
                                    <span class="badge">기본 차량</span>
                                </c:if>

                                <h2>
                                    <c:choose>
                                        <c:when test="${not empty vehicle.vehicleNickname}">
                                            ${vehicle.vehicleNickname}
                                        </c:when>
                                        <c:otherwise>
                                            ${vehicle.modelName}
                                        </c:otherwise>
                                    </c:choose>
                                </h2>
                            </div>

                            <span class="vehicle-status">
                                <c:choose>
                                    <c:when test="${vehicle.isDefault}">사용중</c:when>
                                    <c:otherwise>대기</c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <p class="vehicle-desc">
                            ${vehicle.manufacturer} · ${vehicle.connectorType} · ${vehicle.modelName}
                        </p>

                        <div class="vehicle-data">
                            <div>
                                <span>배터리 용량</span>
                                <strong>${vehicle.batteryCapacityKwh}kWh</strong>
                            </div>

                            <div>
                                <span>최대 충전 속도</span>
                                <strong>${vehicle.maxChargingSpeedKw}kW</strong>
                            </div>

                            <div>
                                <span>충전 타입</span>
                                <strong>${vehicle.connectorType}</strong>
                            </div>

                            <div>
                                <span>차량 번호</span>
                                <strong>${vehicle.plateNumber}</strong>
                            </div>
                        </div>

						<div class="card-buttons">
						
						    <c:choose>
						        <c:when test="${vehicle.isDefault}">
						            <button type="button"
						                    class="outline-btn active-btn"
						                    disabled>
						                설정됨
						            </button>
						        </c:when>
						
						        <c:otherwise>
						            <button type="button"
						                    class="outline-btn default-btn"
						                    data-vehicle-id="${vehicle.vehicleId}">
						                기본 차량 설정
						            </button>
						        </c:otherwise>
						    </c:choose>
						
								    <button type="button"
									        class="danger-btn delete-btn"
									        data-vehicle-id="${vehicle.vehicleId}">
									    삭제
									</button>
						
						</div>
                    </div>
                </article>

            </c:forEach>

            <c:if test="${empty vehicleList}">
                <div class="empty-box">
                    <p>등록된 차량이 없습니다.</p>
                    <a href="${pageContext.request.contextPath}/vehicle/register" class="add-btn">
                        차량 등록하기
                    </a>
                </div>
            </c:if>

        </section>

    </main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</div>
<script src="${pageContext.request.contextPath}/js/jquery.js"></script>
<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/vehicle/vehicle_list.js"></script>

</body>
</html>