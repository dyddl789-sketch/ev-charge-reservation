<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>충전소 상세</title>

<!-- 공통 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">

<!-- 충전소 상세 화면 전용 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/station/station_detail.css">
</head>
<body>

<!-- 사용자 공통 헤더 -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="station-detail-page">

    <!-- 충전소 기본 정보 영역 -->
    <section class="detail-header">

        <!-- 목록으로 돌아가기 -->
        <a href="${pageContext.request.contextPath}/station/list" class="back-link">
            ← 목록으로
        </a>

        <!-- 충전소명 -->
        <h2>${station.stationName}</h2>

        <!-- 주소 -->
        <p>${station.address}</p>

        <!-- 운영 정보 -->
        <div class="detail-meta">
            <span>운영기관: ${station.operatorName}</span>
            <span>운영시간: ${station.openTime} ~ ${station.closeTime}</span>
            <span>상태: ${station.stationStatus}</span>
        </div>
    </section>

    
	<section class="charger-section">
	    <h3>충전기 목록</h3>
	
	    <c:choose>
	
	        <c:when test="${empty chargerList}">
	            <div class="empty-box">
	                등록된 충전기가 없습니다.
	            </div>
	        </c:when>
	
	        <c:otherwise>
	            <div class="charger-list">
	
	                <c:forEach var="charger" items="${chargerList}">
	
	                    <article class="charger-card">
	
	                        <div class="charger-info">
	                            <h4>${charger.chargerName}</h4>
	
	                            <p>
	                                ${charger.chargerType}
	                                · ${charger.connectorType}
	                                · ${charger.chargingSpeedKw}kW
	                            </p>
	
	                            <p>
	                                요금: ${charger.pricePerKwh}원/kWh
	                            </p>
	
	                            <span class="charger-status">
	                                ${charger.status}
	                            </span>
	                        </div>
	
	                        <div class="charger-action">
	
	                            <c:choose>
	                                <c:when test="${charger.status eq '사용가능'}">
	                                    <a href="${pageContext.request.contextPath}/reservation/form?chargerId=${charger.chargerId}">
	                                        예약하기
	                                    </a>
	                                </c:when>
	
	                                <c:otherwise>
	                                    <button type="button" disabled>
	                                        예약불가
	                                    </button>
	                                </c:otherwise>
	                            </c:choose>
	
	                        </div>
	
	                    </article>
	
	                </c:forEach>
	            </div>
	        </c:otherwise>
	
	    </c:choose>

	</section>

</main>

<!-- 사용자 공통 푸터 -->
<%@ include file="/WEB-INF/views/common/footer.jsp" %>

</body>
</html>