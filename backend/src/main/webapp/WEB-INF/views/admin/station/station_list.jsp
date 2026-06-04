<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 충전소 목록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/station_list.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

            <!-- 제목 영역 -->
            <section class="admin-title-row">
                <div>
                    <h1>충전소 목록</h1>
                    <p>등록된 충전소 정보를 확인하고 운영 상태를 관리합니다.</p>
                </div>

                <a href="${pageContext.request.contextPath}/admin/station/manage"
                   class="add-station-btn">
                    + 충전소 등록
                </a>
            </section>

            <!-- 요약 카드 -->
            <section class="station-summary">

                <article class="station-summary-card">
                    <div>
                        <span>전체 충전소</span>
                        <strong>
                            <fmt:formatNumber value="${stationPage.totalCount}" pattern="#,###"/>개
                        </strong>
                    </div>

                    <i class="summary-icon total">⚡</i>
                </article>

                <article class="station-summary-card">
                    <div>
                        <span>운영중</span>
                        <strong>
                            <fmt:formatNumber value="${stationPage.activeCount}" pattern="#,###"/>개
                        </strong>
                    </div>

                    <i class="summary-icon active">✓</i>
                </article>

                <article class="station-summary-card">
                    <div>
                        <span>점검중</span>
                        <strong>
                            <fmt:formatNumber value="${stationPage.checkCount}" pattern="#,###"/>개
                        </strong>
                    </div>

                    <i class="summary-icon check">!</i>
                </article>

            </section>

            <!-- 검색 영역 -->
            <section class="station-search-card">

                <form action="${pageContext.request.contextPath}/admin/station/list"
                      method="get">

                    <div class="search-row">

                        <div class="search-group">
                            <label>지역</label>
                            <select name="region">
                                <option value="">전체</option>
                                <option value="서울" ${searchDTO.region == '서울' ? 'selected' : ''}>서울</option>
                                <option value="부산" ${searchDTO.region == '부산' ? 'selected' : ''}>부산</option>
                                <option value="대구" ${searchDTO.region == '대구' ? 'selected' : ''}>대구</option>
                                <option value="인천" ${searchDTO.region == '인천' ? 'selected' : ''}>인천</option>
                                <option value="광주" ${searchDTO.region == '광주' ? 'selected' : ''}>광주</option>
                                <option value="대전" ${searchDTO.region == '대전' ? 'selected' : ''}>대전</option>
                                <option value="울산" ${searchDTO.region == '울산' ? 'selected' : ''}>울산</option>
                                <option value="경기" ${searchDTO.region == '경기' ? 'selected' : ''}>경기</option>
                                <option value="강원" ${searchDTO.region == '강원' ? 'selected' : ''}>강원</option>
                                <option value="충북" ${searchDTO.region == '충북' ? 'selected' : ''}>충북</option>
                                <option value="충남" ${searchDTO.region == '충남' ? 'selected' : ''}>충남</option>
                                <option value="전북" ${searchDTO.region == '전북' ? 'selected' : ''}>전북</option>
                                <option value="전남" ${searchDTO.region == '전남' ? 'selected' : ''}>전남</option>
                                <option value="경북" ${searchDTO.region == '경북' ? 'selected' : ''}>경북</option>
                                <option value="경남" ${searchDTO.region == '경남' ? 'selected' : ''}>경남</option>
                                <option value="제주" ${searchDTO.region == '제주' ? 'selected' : ''}>제주</option>
                            </select>
                        </div>

                        <div class="search-group">
                            <label>운영 상태</label>
                            <select name="stationStatus">
                                <option value="">전체</option>
                                <option value="운영중" ${searchDTO.stationStatus == '운영중' ? 'selected' : ''}>운영중</option>
                                <option value="점검중" ${searchDTO.stationStatus == '점검중' ? 'selected' : ''}>점검중</option>
                                <option value="운영중지" ${searchDTO.stationStatus == '운영중지' ? 'selected' : ''}>운영중지</option>
                            </select>
                        </div>

                        <div class="search-group">
                            <label>충전 타입</label>
                            <select name="chargerType">
                                <option value="">전체</option>
                                <option value="완속" ${searchDTO.chargerType == '완속' ? 'selected' : ''}>완속</option>
                                <option value="급속" ${searchDTO.chargerType == '급속' ? 'selected' : ''}>급속</option>
                                <option value="초급속" ${searchDTO.chargerType == '초급속' ? 'selected' : ''}>초급속</option>
                            </select>
                        </div>

                        <div class="search-actions">
                            <a href="${pageContext.request.contextPath}/admin/station/list"
                               class="reset-btn">
                                초기화
                            </a>
                        </div>

                    </div>

                    <div class="search-row second">

                        <div class="search-group">
                            <label>검색 기준</label>
                            <select name="searchType">
                                <option value="stationName" ${searchDTO.searchType == 'stationName' ? 'selected' : ''}>충전소명</option>
                                <option value="address" ${searchDTO.searchType == 'address' ? 'selected' : ''}>주소</option>
                            </select>
                        </div>

                        <div class="keyword-box">
                            <input type="text"
                                   name="keyword"
                                   value="${searchDTO.keyword}"
                                   placeholder="충전소명 또는 주소를 입력하세요.">

                            <input type="hidden"
                                   name="size"
                                   value="${searchDTO.size}">

                            <button type="submit">
                                검색
                            </button>
                        </div>

                    </div>

                </form>

            </section>

            <!-- 목록 영역 -->
            <section class="station-table-card">

                <div class="table-top">
                    <p>
                        총
                        <strong>
                            <fmt:formatNumber value="${stationPage.searchCount}" pattern="#,###"/>
                        </strong>
                        건
                    </p>

                    <form action="${pageContext.request.contextPath}/admin/station/list"
                          method="get">

                        <input type="hidden" name="region" value="${searchDTO.region}">
                        <input type="hidden" name="stationStatus" value="${searchDTO.stationStatus}">
                        <input type="hidden" name="chargerType" value="${searchDTO.chargerType}">
                        <input type="hidden" name="searchType" value="${searchDTO.searchType}">
                        <input type="hidden" name="keyword" value="${searchDTO.keyword}">

                        <select name="size"
                                onchange="this.form.submit()">
                            <option value="10" ${searchDTO.size == 10 ? 'selected' : ''}>10개씩 보기</option>
                            <option value="20" ${searchDTO.size == 20 ? 'selected' : ''}>20개씩 보기</option>
                            <option value="50" ${searchDTO.size == 50 ? 'selected' : ''}>50개씩 보기</option>
                        </select>

                    </form>
                </div>

                <table class="station-table">

                    <thead>
                    <tr>
                        <th>
                            <input type="checkbox">
                        </th>
                        <th>번호</th>
                        <th>충전소명</th>
                        <th>주소</th>
                        <th>충전기 수</th>
                        <th>사용 가능</th>
                        <th>충전 타입</th>
                        <th>운영 상태</th>
                        <th>관리</th>
                    </tr>
                    </thead>

                    <tbody>

                    <c:choose>

                        <c:when test="${empty stationPage.stationList}">
                            <tr>
                                <td colspan="9">
                                    조회된 충전소가 없습니다.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>

                            <c:forEach var="station"
                                       items="${stationPage.stationList}">

                                <tr>
                                    <td>
                                        <input type="checkbox"
                                               name="stationId"
                                               value="${station.stationId}">
                                    </td>

                                    <td>${station.stationId}</td>

                                    <td class="station-name">
                                        ${station.stationName}
                                    </td>

                                    <td>${station.address}</td>

                                    <td>
                                        <fmt:formatNumber value="${station.chargerCount}" pattern="#,###"/>대
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${station.availableChargerCount}" pattern="#,###"/>대
                                    </td>

                                    <td>
                                        <c:if test="${station.ultra}">
                                            <span class="type-badge ultra">
                                                초급속
                                            </span>
                                        </c:if>

                                        <c:if test="${station.fast}">
                                            <span class="type-badge fast">
                                                급속
                                            </span>
                                        </c:if>

                                        <c:if test="${station.slow}">
                                            <span class="type-badge slow">
                                                완속
                                            </span>
                                        </c:if>
                                    </td>

                                    <td>
                                        <span class="status-badge ${station.stationStatusClass}">
                                            ${station.stationStatus}
                                        </span>
                                    </td>

                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/station/manage?stationId=${station.stationId}"
                                           class="edit-btn">
                                            수정
                                        </a>
                                    </td>
                                </tr>

                            </c:forEach>

                        </c:otherwise>

                    </c:choose>

                    </tbody>

                </table>

                <!-- 페이징 -->
                <div class="pagination">

                    <c:if test="${!stationPage.firstPage}">
                        <a href="${pageContext.request.contextPath}/admin/station/list?page=1&size=${searchDTO.size}&region=${searchDTO.region}&stationStatus=${searchDTO.stationStatus}&chargerType=${searchDTO.chargerType}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            «
                        </a>

                        <a href="${pageContext.request.contextPath}/admin/station/list?page=${stationPage.page - 1}&size=${searchDTO.size}&region=${searchDTO.region}&stationStatus=${searchDTO.stationStatus}&chargerType=${searchDTO.chargerType}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            ‹
                        </a>
                    </c:if>

                    <c:forEach begin="1"
                               end="${stationPage.totalPage}"
                               var="pageNo">

                        <a href="${pageContext.request.contextPath}/admin/station/list?page=${pageNo}&size=${searchDTO.size}&region=${searchDTO.region}&stationStatus=${searchDTO.stationStatus}&chargerType=${searchDTO.chargerType}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}"
                           class="${pageNo == stationPage.page ? 'active' : ''}">
                            ${pageNo}
                        </a>

                    </c:forEach>

                    <c:if test="${!stationPage.lastPage}">
                        <a href="${pageContext.request.contextPath}/admin/station/list?page=${stationPage.page + 1}&size=${searchDTO.size}&region=${searchDTO.region}&stationStatus=${searchDTO.stationStatus}&chargerType=${searchDTO.chargerType}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            ›
                        </a>

                        <a href="${pageContext.request.contextPath}/admin/station/list?page=${stationPage.totalPage}&size=${searchDTO.size}&region=${searchDTO.region}&stationStatus=${searchDTO.stationStatus}&chargerType=${searchDTO.chargerType}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            »
                        </a>
                    </c:if>

                </div>

            </section>

        </main>

    </div>

</div>

</body>
</html>