<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원 목록</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/admin.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin/member.css">
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
                    <h1>회원 목록</h1>
                    <p>가입한 회원 정보를 확인하고 회원 상태를 관리합니다.</p>
                </div>
            </section>

            <!-- 요약 카드 -->
            <section class="member-summary">

                <article class="member-summary-card">
                    <div>
                        <span>전체 회원 수</span>
                        <strong>
                            <fmt:formatNumber value="${memberPage.totalCount}" pattern="#,###"/>명
                        </strong>
                    </div>
                </article>

                <article class="member-summary-card">
                    <div>
                        <span>회원</span>
                        <strong>
                            <fmt:formatNumber value="${memberPage.activeCount}" pattern="#,###"/>명
                        </strong>
                    </div>
                </article>

                <article class="member-summary-card">
                    <div>
                        <span>탈퇴 회원</span>
                        <strong>
                            <fmt:formatNumber value="${memberPage.inactiveCount}" pattern="#,###"/>명
                        </strong>
                    </div>
                </article>

            </section>

            <!-- 검색 영역 -->
            <section class="member-search-card">

                <form action="${pageContext.request.contextPath}/admin/member/list" method="get">

                    <div class="search-row">
                        <div class="search-group">
                            <label for="memberStatus">회원 상태</label>
                            <select id="memberStatus" name="status">
                                <option value="" ${empty searchDTO.status ? 'selected' : ''}>전체</option>
                                <option value="ACTIVE" ${searchDTO.status == 'ACTIVE' ? 'selected' : ''}>회원</option>
                                <option value="INACTIVE" ${searchDTO.status == 'INACTIVE' ? 'selected' : ''}>탈퇴 회원</option>
                                <option value="BLOCKED" ${searchDTO.status == 'BLOCKED' ? 'selected' : ''}>정지 회원</option>
                            </select>
                        </div>

                        <div class="search-group">
                            <label for="joinStart">가입일</label>
                            <input type="date"
                                   id="joinStart"
                                   name="joinStart"
                                   value="${searchDTO.joinStart}">
                        </div>

                        <span class="date-wave">~</span>

                        <div class="search-group">
                            <label for="joinEnd">종료일</label>
                            <input type="date"
                                   id="joinEnd"
                                   name="joinEnd"
                                   value="${searchDTO.joinEnd}">
                        </div>

                        <div class="search-actions">
                            <a href="${pageContext.request.contextPath}/admin/member/list" class="reset-btn">
                                초기화
                            </a>
                        </div>
                    </div>

                    <div class="search-row second">
                        <div class="search-group">
                            <label for="searchType">검색 기준</label>
                            <select id="searchType" name="searchType">
                                <option value="name" ${searchDTO.searchType == 'name' ? 'selected' : ''}>이름</option>
                                <option value="id" ${searchDTO.searchType == 'id' ? 'selected' : ''}>아이디</option>
                                <option value="email" ${searchDTO.searchType == 'email' ? 'selected' : ''}>이메일</option>
                                <option value="phone" ${searchDTO.searchType == 'phone' ? 'selected' : ''}>연락처</option>
                            </select>
                        </div>

                        <div class="keyword-box">
                            <input type="text"
                                   name="keyword"
                                   value="${searchDTO.keyword}"
                                   placeholder="검색어를 입력하세요.">

                            <input type="hidden" name="page" value="1">
                            <input type="hidden" name="size" value="${searchDTO.size}">

                            <button type="submit">검색</button>
                        </div>
                    </div>

                </form>

            </section>

            <!-- 회원 목록 테이블 -->
            <section class="member-table-card">

                <div class="table-top">
                    <p>
                        총
                        <strong>
                            <fmt:formatNumber value="${memberPage.searchCount}" pattern="#,###"/>건
                        </strong>
                    </p>

                    <form action="${pageContext.request.contextPath}/admin/member/list" method="get">
                        <input type="hidden" name="status" value="${searchDTO.status}">
                        <input type="hidden" name="joinStart" value="${searchDTO.joinStart}">
                        <input type="hidden" name="joinEnd" value="${searchDTO.joinEnd}">
                        <input type="hidden" name="searchType" value="${searchDTO.searchType}">
                        <input type="hidden" name="keyword" value="${searchDTO.keyword}">
                        <input type="hidden" name="page" value="1">

                        <select name="size" onchange="this.form.submit()">
                            <option value="10" ${searchDTO.size == 10 ? 'selected' : ''}>10개씩 보기</option>
                            <option value="20" ${searchDTO.size == 20 ? 'selected' : ''}>20개씩 보기</option>
                            <option value="50" ${searchDTO.size == 50 ? 'selected' : ''}>50개씩 보기</option>
                        </select>
                    </form>
                </div>

                <table class="member-table">
                    <thead>
                        <tr>
                            <th><input type="checkbox"></th>
                            <th>번호</th>
                            <th>회원 구분</th>
                            <th>아이디</th>
                            <th>이름</th>
                            <th>이메일</th>
                            <th>연락처</th>
                            <th>가입일</th>
                            <th>상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:choose>
                            <c:when test="${empty memberPage.memberList}">
                                <tr>
                                    <td colspan="10">조회된 회원이 없습니다.</td>
                                </tr>
                            </c:when>

                            <c:otherwise>
                                <c:forEach var="member" items="${memberPage.memberList}">
                                    <tr>
                                        <td><input type="checkbox" value="${member.memberId}"></td>
                                        <td>${member.memberId}</td>
                                        <td>
                                            <span class="member-type ${member.userTypeClass}">
                                                ${member.userTypeText}
                                            </span>
                                        </td>
                                        <td>${member.userId}</td>
                                        <td>${member.memberName}</td>
                                        <td>${member.email}</td>
                                        <td>${empty member.phone ? '-' : member.phone}</td>
                                        <td>${member.createdAtText}</td>
                                        <td>
                                            <span class="status-badge ${member.statusClass}">
                                                ${member.statusText}
                                            </span>
                                        </td>
                                        <td>
											<a href="${pageContext.request.contextPath}/admin/member/detail?memberId=${member.memberId}"
											   class="detail-btn">
											    상세보기
											</a>

                                            <c:choose>
                                                <c:when test="${member.status == 'ACTIVE'}">
                                                    <form action="${pageContext.request.contextPath}/admin/member/withdraw"
                                                          method="post"
                                                          class="inline-form withdraw-form">
                                                        <input type="hidden" name="memberId" value="${member.memberId}">
                                                        <button type="submit" class="withdraw-btn">
                                                            탈퇴처리
                                                        </button>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/admin/member/restore"
                                                          method="post"
                                                          class="inline-form restore-form">
                                                        <input type="hidden" name="memberId" value="${member.memberId}">
                                                        <button type="submit" class="restore-btn">
                                                            복구
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="pagination">

                    <c:if test="${memberPage.page > 1}">
                        <a href="${pageContext.request.contextPath}/admin/member/list?page=1&size=${searchDTO.size}&status=${searchDTO.status}&joinStart=${searchDTO.joinStart}&joinEnd=${searchDTO.joinEnd}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            «
                        </a>

                        <a href="${pageContext.request.contextPath}/admin/member/list?page=${memberPage.page - 1}&size=${searchDTO.size}&status=${searchDTO.status}&joinStart=${searchDTO.joinStart}&joinEnd=${searchDTO.joinEnd}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            ‹
                        </a>
                    </c:if>

                    <c:forEach var="pageNo" begin="1" end="${memberPage.totalPage}">
                        <a class="${pageNo == memberPage.page ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/admin/member/list?page=${pageNo}&size=${searchDTO.size}&status=${searchDTO.status}&joinStart=${searchDTO.joinStart}&joinEnd=${searchDTO.joinEnd}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            ${pageNo}
                        </a>
                    </c:forEach>

                    <c:if test="${memberPage.page < memberPage.totalPage}">
                        <a href="${pageContext.request.contextPath}/admin/member/list?page=${memberPage.page + 1}&size=${searchDTO.size}&status=${searchDTO.status}&joinStart=${searchDTO.joinStart}&joinEnd=${searchDTO.joinEnd}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            ›
                        </a>

                        <a href="${pageContext.request.contextPath}/admin/member/list?page=${memberPage.totalPage}&size=${searchDTO.size}&status=${searchDTO.status}&joinStart=${searchDTO.joinStart}&joinEnd=${searchDTO.joinEnd}&searchType=${searchDTO.searchType}&keyword=${searchDTO.keyword}">
                            »
                        </a>
                    </c:if>

                </div>

            </section>

        </main>

    </div>

</div>

<script>
    const withdrawForms = document.querySelectorAll(".withdraw-form");

    withdrawForms.forEach(function(form) {
        form.addEventListener("submit", function(event) {
            const result = confirm("해당 회원을 탈퇴 처리하시겠습니까?");

            if (!result) {
                event.preventDefault();
            }
        });
    });

    const restoreForms = document.querySelectorAll(".restore-form");

    restoreForms.forEach(function(form) {
        form.addEventListener("submit", function(event) {
            const result = confirm("해당 회원을 복구하시겠습니까?");

            if (!result) {
                event.preventDefault();
            }
        });
    });
</script>

</body>
</html>