<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main/main.css">
</head>
<body>

<div class="main-page">
<jsp:include page="/WEB-INF/views/common/header.jsp" />

    <main class="dashboard">

        <!-- 왼쪽 패널 -->
        <aside class="left-panel">
			
			<section class="welcome-card">
			
			    <sec:authorize access="isAnonymous()">
			        <div class="guest-box">
			            <h2>로그인이 필요합니다.</h2>
			            <p>로그인 후 차량 관리, 예약 관리, 충전소 검색 서비스를 이용할 수 있습니다.</p>
			        </div>
			
			        <div class="welcome-buttons">
			            <a href="${pageContext.request.contextPath}/login" class="primary-btn">
			                로그인하기
			            </a>
			            <a href="${pageContext.request.contextPath}/login" class="outline-btn">
			                충전소 검색
			            </a>
			        </div>
			    </sec:authorize>
			
				<sec:authorize access="isAuthenticated()">
				    <sec:authentication property="principal.profileImageUrl" var="profileImageUrl" />
				    <sec:authentication property="principal.nickname" var="nickname" />
				
				    <div class="member-profile-card">
				        <img src="${pageContext.request.contextPath}${profileImageUrl}"
				             alt="프로필 이미지"
				             class="member-profile-image">
				
				        <div class="member-profile-info">
				            <strong>${nickname}</strong>
				            <span>EV Charge 회원</span>
				        </div>
				    </div>
				
				    <div class="welcome-buttons">
				        <a href="${pageContext.request.contextPath}/station/map" class="primary-btn">
				            충전소 검색
				        </a>
				        <a href="${pageContext.request.contextPath}/reservation/my" class="outline-btn">
				            예약 관리하기
				        </a>
				    </div>
				</sec:authorize>
			
			</section>

            <section class="side-card vehicle-card">
                <div class="side-card-header">
                    <h2>내 차량</h2>
                    <a href="${pageContext.request.contextPath}/vehicle/list">차량 관리 ›</a>
                </div>

                <c:choose>
                    <c:when test="${not empty mainVehicle}">
                        <div class="vehicle-info">
                            <div class="vehicle-img">
                                <c:choose>
                                    <c:when test="${not empty mainVehicle.imageUrl}">
                                        <img src="${pageContext.request.contextPath}${mainVehicle.imageUrl}"
                                             alt="${mainVehicle.modelName}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="vehicle-placeholder">EV</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="vehicle-detail">
                                <strong>
                                    <c:choose>
                                        <c:when test="${not empty mainVehicle.vehicleNickname}">
                                            ${mainVehicle.vehicleNickname}
                                        </c:when>
                                        <c:otherwise>
                                            ${mainVehicle.modelName}
                                        </c:otherwise>
                                    </c:choose>
                                </strong>
                                <p>${mainVehicle.batteryCapacityKwh} kWh · ${mainVehicle.connectorType}</p>
                                <c:if test="${mainVehicle.isDefault}">
                                    <span class="vehicle-tag">기본 차량</span>
                                </c:if>
                            </div>
                        </div>

                        <div class="vehicle-spec-list">
                            <div>
                                <span>제조사</span>
                                <strong>${mainVehicle.manufacturer}</strong>
                            </div>
                            <div>
                                <span>최대 충전 속도</span>
                                <strong>${mainVehicle.maxChargingSpeedKw} kW</strong>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state vehicle-empty">
                            <strong>등록된 차량이 없습니다.</strong>
                            <a href="${pageContext.request.contextPath}/vehicle/register">차량 등록하기</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

        </aside>

        <!-- 중앙 컨텐츠 -->
        <section class="center-content">

            <!-- 요약 카드 -->
            <section class="summary-grid">

                <article class="summary-card">
                    <div>
                        <c:choose>
                            <c:when test="${not empty defaultLocation}">
                                <span>${defaultLocation.locationName} 주변 충전소</span>
                                <strong>${nearbyStationCount} 곳</strong>
                                <p>${nearbyRadiusKm}km 이내 운영중 충전소</p>
                            </c:when>
                            <c:otherwise>
                                <span>기본 출발지 주변 충전소</span>
                                <strong>-</strong>
                                <p>기본 출발지를 설정해 주세요</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </article>

                <article class="summary-card">
                    <div>
                        <span>사용 가능한 충전기</span>
                        <c:choose>
                            <c:when test="${not empty defaultLocation}">
                                <strong>${nearbyAvailableChargerCount} 대</strong>
                                <p>${defaultLocation.locationName} 기준</p>
                            </c:when>
                            <c:otherwise>
                                <strong>-</strong>
                                <p>기본 출발지 설정 필요</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </article>

                <article class="summary-card">
                    <div>
                        <span>다음 예약</span>
                        <c:choose>
                            <c:when test="${not empty nextReservation}">
                                <strong>${nextReservation.startTimeText}</strong>
                                <p>${nextReservation.stationName}</p>
                            </c:when>
                            <c:otherwise>
                                <strong>없음</strong>
                                <p>예정된 예약이 없습니다</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </article>

                <article class="summary-card">
                    <div>
                        <span>이번 달 충전 비용</span>
                        <strong><fmt:formatNumber value="${thisMonthChargingCost}" type="number" /> 원</strong>
                        <p>완료된 충전 내역 기준</p>
                    </div>
                </article>

            </section>

            <!-- 추천 충전소 -->
            <section class="content-card recommend-section">

                <div class="section-header">
                    <div>
                        <h2>추천 충전소</h2>
                        <c:choose>
                            <c:when test="${not empty defaultLocation}">
                                <p>${defaultLocation.locationName} 기준 가까운 충전소를 추천합니다.</p>
                            </c:when>
                            <c:otherwise>
                                <p>기본 출발지를 설정하면 주변 충전소를 추천합니다.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <a href="${pageContext.request.contextPath}/station/map">지도 보기 ›</a>
                </div>

                <c:choose>
                    <c:when test="${not empty nearbyStationList}">
                        <div class="station-grid">
                            <c:forEach var="station" items="${nearbyStationList}" varStatus="status">
                                <article class="station-card">

								    <c:set var="stationImageUrl"
								           value="${empty station.imageUrl ? '/images/station/station-default.jpg' : station.imageUrl}" />
								
								    <div class="station-img"
								         style="background-image: url('${pageContext.request.contextPath}${stationImageUrl}');">
								        <c:if test="${status.index == 0}">
								            <span class="station-badge">가장 가까움</span>
								        </c:if>
								    </div>

                                    <div class="station-body">
                                        <div class="station-title-row">
                                            <h3>${station.stationName}</h3>
                                            <span class="recommend-tag">추천</span>
                                        </div>

                                        <p class="station-address">${station.address}</p>

                                        <div class="station-meta">
                                            <span>
                                                <fmt:formatNumber value="${station.distanceKm}" maxFractionDigits="1" />km
                                            </span>
                                            <span>사용 ${station.availableChargerCount}대</span>
                                            <span>전체 ${station.chargerCount}대</span>
                                        </div>

                                        <div class="station-tags">
                                            <span>기본 출발지 기준</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${station.availableChargerCount > 0}">사용 가능</c:when>
                                                    <c:otherwise>확인 필요</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <span>${nearbyRadiusKm}km 이내</span>
                                        </div>

                                        <div class="station-buttons">
                                            <a href="${pageContext.request.contextPath}/station/detail?stationId=${station.stationId}"
                                               class="detail-btn">상세보기</a>
                                            <a href="${pageContext.request.contextPath}/reservation/form/station?stationId=${station.stationId}"
                                               class="reserve-btn">예약하기</a>
                                        </div>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state station-empty">
                            <c:choose>
                                <c:when test="${empty defaultLocation}">
                                    <strong>기본 출발지가 없습니다.</strong>
                                    <p>출발지를 등록하고 기본 위치로 설정하면 주변 충전소를 추천해드립니다.</p>
                                    <a href="${pageContext.request.contextPath}/station/map">출발지 등록하기</a>
                                </c:when>
                                <c:otherwise>
                                    <strong>${defaultLocation.locationName} 주변 충전소가 없습니다.</strong>
                                    <p>${nearbyRadiusKm}km 이내 운영중 충전소가 없습니다. 지도에서 더 넓은 범위로 확인해보세요.</p>
                                    <a href="${pageContext.request.contextPath}/station/map">지도에서 확인하기</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>

            </section>

        </section>

        <!-- 오른쪽 패널 -->
        <aside class="right-panel">
            <section class="right-card reservation-card">
                <div class="right-card-header">
                    <h2>최근 예약 내역</h2>
                    <a href="${pageContext.request.contextPath}/reservation/my">전체 보기 ›</a>
                </div>

                <c:choose>
                    <c:when test="${not empty nextReservation}">
					    <div class="reservation-text-item">
					        <strong>${nextReservation.stationName}</strong>
					        <p>${nextReservation.startTimeText} ~ ${nextReservation.endTimeText}</p>
					        <span>${nextReservation.status}</span>
					    </div>
					</c:when>

                    <c:otherwise>
                        <div class="empty-state small-empty">
                            <strong>예정된 예약이 없습니다.</strong>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="right-card saved-location-card">
			    <div class="right-card-header">
			        <h2>저장 위치</h2>
			        <a href="${pageContext.request.contextPath}/station/map">관리하기 ›</a>
			    </div>
			
			    <div class="saved-location-list">
			        <c:choose>
			            <c:when test="${not empty savedLocationList}">
			                <c:forEach var="location" items="${savedLocationList}">
			                    <div class="saved-location-item">
			                        <strong>${location.locationName}</strong>
			                        <p>${location.address}</p>
			                    </div>
			                </c:forEach>
			            </c:when>
			
			            <c:otherwise>
			                <div class="empty-mini-box">
			                    <strong>저장된 위치가 없습니다.</strong>
			                    <p>집, 회사, 학교를 등록해보세요.</p>
			                </div>
			            </c:otherwise>
			        </c:choose>
			    </div>
			</section>

        </aside>

    </main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</div>

<script>
    const params = new URLSearchParams(window.location.search);
    const authMsg = params.get("authMsg");

    if (authMsg === "adminOnly") {
        alert("관리자만 접근할 수 있습니다.");
    }
</script>

<jsp:include page="/WEB-INF/views/common/chat_widget.jsp" />
</body>
</html>
