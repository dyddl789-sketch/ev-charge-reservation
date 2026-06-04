<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 로그인</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member/login.css">
</head>
<body>

<div class="login-page">
    <div class="login-card">

        <!-- 왼쪽 이미지 영역 -->
		<section class="login-visual">
		
		    <a href="${pageContext.request.contextPath}/main"
		       class="visual-link">
		
		        <div class="visual-overlay">
		
		            <h1>EV Charge</h1>
		
		            <p>
		                전기차 충전 최적화 및 예약 관리 서비스
		            </p>
		
		        </div>
		
		    </a>
		
		</section>

        <!-- 오른쪽 로그인 영역 -->
        <section class="login-form-area">

            <div class="login-header">
                <h2>로그인</h2>
                <p>서비스를 이용하려면 로그인하세요.</p>

                <c:if test="${not empty errorMsg}">
                    <div class="error-message">${errorMsg}</div>
                </c:if>

                <c:if test="${not empty msg}">
                    <div class="success-message">${msg}</div>
                </c:if>
            </div>

            <!-- 로그인 폼 -->
            <form action="${pageContext.request.contextPath}/login" method="post" class="login-form">

                <div class="input-box">
                    <span class="input-icon">👤</span>
                    <input type="text" name="userId" placeholder="아이디">
                </div>

                <div class="input-box">
                    <span class="input-icon">🔒</span>
                    <input type="password" name="password" placeholder="비밀번호">
                </div>

                <div class="login-options">
                    <label>
                        <input type="checkbox" name="rememberId">
                        아이디 저장
                    </label>

                    <a href="${pageContext.request.contextPath}/member/find">
                        아이디/비밀번호 찾기
                    </a>
                </div>

                <button type="submit" class="login-btn">
                    로그인
                </button>

            </form>

            <!-- 카카오 로그인 -->
            <a href="${pageContext.request.contextPath}/oauth2/authorization/kakao" class="kakao-btn">
                카카오로 로그인
            </a>

            <!-- 회원가입 -->
            <a href="${pageContext.request.contextPath}/member/join" class="join-btn">
                회원가입
            </a>

        </section>

    </div>
</div>

<script>
    const params = new URLSearchParams(window.location.search);
    const authMsg = params.get("authMsg");

    if (authMsg === "loginRequired") {
        alert("로그인이 필요한 페이지입니다.");
    }
</script>

</body>
</html>