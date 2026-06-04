<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>충전소 검색</title>

<!-- 공통 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">

<!-- 충전소 목록 화면 전용 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/station/station_list.css">
</head>
<body>

<!-- 사용자 공통 헤더 -->
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="station-page">

    <!-- 페이지 제목 영역 -->
    <div class="station-list-header">
	    <div>
	        <h2>충전소 검색</h2>
	    </div>
	
	    <a href="${pageContext.request.contextPath}/station/map" class="map-view-btn">
	        지도 보기
	    </a>
	</div>

    <!-- 검색 영역 -->
    <section class="station-search-box">

        <!--
            검색 form

            GET 방식 사용 이유:
            - 검색 조건이 URL에 남음
            - 새로고침/공유가 쉬움

            요청 예:
            /station/list?keyword=부산
        -->
        <form action="${pageContext.request.contextPath}/station/list" method="get">

            <!--
                value="${keyword}"
                검색 후에도 입력한 검색어가 유지되도록 한다.
            -->
            <input type="text"
                   name="keyword"
                   value="${keyword}"
                   placeholder="충전소명, 주소, 운영기관 검색">

            <button type="submit">검색</button>
        </form>
    </section>

    <!-- 충전소 목록 영역 -->
    <section class="station-list-box">

        <!--
            조회된 충전소가 없을 때
        -->
        <c:choose>
            <c:when test="${empty stationList}">
                <div class="empty-box">
                    조회된 충전소가 없습니다.
                </div>
            </c:when>

            
            <c:otherwise>

                
                <c:forEach var="station" items="${stationList}">

                    <article class="station-card">

                        <!-- 충전소 정보 -->
                        <div class="station-info">

                            <!-- 충전소명 -->
                            <h3>${station.stationName}</h3>

                            <!-- 주소 -->
                            <p>${station.address}</p>

                            <!-- 운영기관 / 상태 -->
                            <div class="station-meta">
                                <span>운영기관: ${station.operatorName}</span>
                                <span>상태: ${station.stationStatus}</span>
                            </div>

                            <!-- 충전기 개수 -->
                            <div class="station-meta">
                                <span>전체 충전기: ${station.chargerCount}대</span>
                                <span>사용 가능: ${station.availableChargerCount}대</span>
                            </div>
                        </div>

                        <!-- 상세보기 버튼 -->
                        <div class="station-card-actions">
						    <a href="${pageContext.request.contextPath}/station/detail?stationId=${station.stationId}" class="detail-btn">
						        상세보기
						    </a>
						
						    <a href="${pageContext.request.contextPath}/station/map?stationId=${station.stationId}" class="map-btn">
						        지도에서 보기
						    </a>
						</div>

                    </article>

                </c:forEach>
            </c:otherwise>
        </c:choose>

    </section>

</main>

<!-- 사용자 공통 푸터 -->
<%@ include file="/WEB-INF/views/common/footer.jsp" %>

</body>
</html>