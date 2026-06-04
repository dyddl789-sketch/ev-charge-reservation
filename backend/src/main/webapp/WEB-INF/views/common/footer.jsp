<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<footer class="footer">
    <div class="footer-inner">

        
        <nav class="footer-nav">
            <a href="${pageContext.request.contextPath}/main">
                홈
            </a>

            <a href="${pageContext.request.contextPath}/station/map">
                충전소 검색
            </a>

            <sec:authorize access="isAnonymous()">
                <a href="${pageContext.request.contextPath}/login">
                    로그인
                </a>
            </sec:authorize>

            <sec:authorize access="isAuthenticated()">
                <a href="${pageContext.request.contextPath}/logout">
                    로그아웃
                </a>
            </sec:authorize>
        </nav>

    </div>
</footer>