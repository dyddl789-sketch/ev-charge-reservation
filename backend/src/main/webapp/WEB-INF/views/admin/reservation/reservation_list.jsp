<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 예약 현황</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/reservation_list.css">
</head>
<body>

<div class="admin-page">

    <%@ include file="/WEB-INF/views/common/admin_header.jsp" %>

    <div class="admin-layout">

        <%@ include file="/WEB-INF/views/common/admin_sidebar.jsp" %>

        <main class="admin-content">

        <div class="page-header">
            <div>
                <h1>예약 현황</h1>
                <p>전체 충전 예약 상태와 예약 이슈를 관리합니다.</p>
            </div>
        </div>

        <!-- 예약 요약 카드 -->
        <div class="dashboard-stats">

            <div class="stat-card">
                <div class="stat-label">전체 예약</div>
                <div class="stat-value">${reservationPage.summary.totalCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">예약완료</div>
                <div class="stat-value">${reservationPage.summary.reservedCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">인증완료</div>
                <div class="stat-value">${reservationPage.summary.verifiedCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">충전중</div>
                <div class="stat-value">${reservationPage.summary.chargingCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">완료</div>
                <div class="stat-value">${reservationPage.summary.completedCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">취소</div>
                <div class="stat-value">${reservationPage.summary.canceledCount}</div>
            </div>

            <div class="stat-card">
                <div class="stat-label">노쇼</div>
                <div class="stat-value">${reservationPage.summary.noShowCount}</div>
            </div>

        </div>

        <!-- 오늘 예약 이슈 -->
        <section class="issue-panel">
            <div class="section-title">
                <h2>오늘 예약 이슈</h2>
            </div>

            <div class="issue-grid">

                <div class="issue-card">
                    <span class="issue-title">인증 대기</span>
                    <strong>${reservationPage.issue.waitingAuthCount}</strong>
                </div>

                <div class="issue-card">
                    <span class="issue-title">취소 건수</span>
                    <strong>${reservationPage.issue.cancelRequestCount}</strong>
                </div>

                <div class="issue-card">
                    <span class="issue-title">예상 노쇼</span>
                    <strong>${reservationPage.issue.expectedNoShowCount}</strong>
                </div>

                <div class="issue-card">
                    <span class="issue-title">시작 지연</span>
                    <strong>${reservationPage.issue.delayedStartCount}</strong>
                </div>

            </div>
        </section>

        <!-- 검색 영역 -->
        <section class="search-panel">
            <form method="get"
                  action="${pageContext.request.contextPath}/admin/reservation/list"
                  class="search-form">

                <select name="status">
                    <option value="">전체 상태</option>
                    <option value="예약완료" ${searchDTO.status eq '예약완료' ? 'selected' : ''}>예약완료</option>
                    <option value="인증완료" ${searchDTO.status eq '인증완료' ? 'selected' : ''}>인증완료</option>
                    <option value="충전중" ${searchDTO.status eq '충전중' ? 'selected' : ''}>충전중</option>
                    <option value="완료" ${searchDTO.status eq '완료' ? 'selected' : ''}>완료</option>
                    <option value="취소" ${searchDTO.status eq '취소' ? 'selected' : ''}>취소</option>
                    <option value="노쇼" ${searchDTO.status eq '노쇼' ? 'selected' : ''}>노쇼</option>
                </select>

                <select name="searchType">
                    <option value="">전체 검색</option>
                    <option value="memberName" ${searchDTO.searchType eq 'memberName' ? 'selected' : ''}>회원명</option>
                    <option value="stationName" ${searchDTO.searchType eq 'stationName' ? 'selected' : ''}>충전소명</option>
                    <option value="vehicleName" ${searchDTO.searchType eq 'vehicleName' ? 'selected' : ''}>차량명</option>
                </select>

                <input type="date" name="startDate" value="${searchDTO.startDate}">
                <input type="date" name="endDate" value="${searchDTO.endDate}">

                <input type="text"
                       name="keyword"
                       placeholder="검색어를 입력하세요"
                       value="${searchDTO.keyword}">

                <input type="hidden" name="size" value="${searchDTO.size}">

                <button type="submit" class="btn-search">검색</button>
            </form>
        </section>

        <!-- 예약 목록 -->
        <section class="table-panel">

            <div class="section-title">
                <h2>예약 목록</h2>
                <span>총 ${reservationPage.totalCount}건</span>
            </div>

            <table class="admin-table">
                <thead>
                    <tr>
                        <th>예약번호</th>
                        <th>예약시간</th>
                        <th>회원명</th>
                        <th>차량</th>
                        <th>충전소</th>
                        <th>충전기</th>
                        <th>SOC</th>
                        <th>예상시간</th>
                        <th>예상금액</th>
                        <th>인증코드</th>
                        <th>상태</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>
                    <c:choose>
                        <c:when test="${empty reservationPage.reservationList}">
                            <tr>
                                <td colspan="12" class="empty-row">
                                    조회된 예약 내역이 없습니다.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach var="reservation" items="${reservationPage.reservationList}">
                                <tr>
                                    <td>${reservation.reservationNo}</td>
                                    <td>${reservation.startTimeText}</td>
                                    <td>${reservation.memberName}</td>
                                    <td>${reservation.vehicleName}</td>
                                    <td>${reservation.stationName}</td>
                                    <td>${reservation.chargerName}</td>
                                    <td>${reservation.socText}</td>
                                    <td>${reservation.estimatedMinutes}분</td>
                                    <td>${reservation.estimatedCost}원</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty reservation.authCode}">
                                                -
                                            </c:when>
                                            <c:otherwise>
                                                ${reservation.authCode}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="status-badge ${reservation.statusClass}">
                                            ${reservation.status}
                                        </span>
                                    </td>
                                    <td class="action-cell">

                                        <c:if test="${reservation.status eq '예약완료' or reservation.status eq '인증완료'}">
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/reservation/cancel"
                                                  onsubmit="return confirm('예약을 취소 처리하시겠습니까?');">
                                                <input type="hidden" name="reservationId" value="${reservation.reservationId}">
                                                <button type="submit" class="btn-action cancel">취소</button>
                                            </form>
                                        </c:if>

                                        <c:if test="${reservation.status eq '예약완료' or reservation.status eq '인증완료'}">
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/reservation/noshow"
                                                  onsubmit="return confirm('노쇼 처리하시겠습니까?');">
                                                <input type="hidden" name="reservationId" value="${reservation.reservationId}">
                                                <button type="submit" class="btn-action noshow">노쇼</button>
                                            </form>
                                        </c:if>

                                        <c:if test="${reservation.status eq '인증완료'}">
                                            <form method="post"
                                                  action="${pageContext.request.contextPath}/admin/reservation/start"
                                                  onsubmit="return confirm('충전중 상태로 변경하시겠습니까?');">
                                                <input type="hidden" name="reservationId" value="${reservation.reservationId}">
                                                <button type="submit" class="btn-action start">충전시작</button>
                                            </form>
                                        </c:if>

                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>

        </section>

        <!-- 페이징 -->
        <div class="pagination">

            <c:if test="${!reservationPage.firstPage}">
                <a href="${pageContext.request.contextPath}/admin/reservation/list?page=${reservationPage.page - 1}&size=${reservationPage.size}&status=${searchDTO.status}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}&startDate=${searchDTO.startDate}&endDate=${searchDTO.endDate}">
                    이전
                </a>
            </c:if>

            <span class="page-info">
                ${reservationPage.page} / ${reservationPage.totalPage}
            </span>

            <c:if test="${!reservationPage.lastPage}">
                <a href="${pageContext.request.contextPath}/admin/reservation/list?page=${reservationPage.page + 1}&size=${reservationPage.size}&status=${searchDTO.status}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}&startDate=${searchDTO.startDate}&endDate=${searchDTO.endDate}">
                    다음
                </a>
            </c:if>
		 </div>
        </main>

    </div>

</div>

</body>
</html>