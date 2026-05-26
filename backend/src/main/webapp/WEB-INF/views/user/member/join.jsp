<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- JSTL 사용을 위한 태그 라이브러리 --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
    사용자 공통 헤더 include

    절대 경로로 include하면 JSP 위치가 바뀌어도 깨질 가능성이 적다.
--%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원가입</title>

<%-- 공통 CSS --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">

<%-- 회원가입/로그인 CSS --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member/join.css">
</head>
<body>

<main class="join-page">

    <section class="join-card">

        <h1>회원가입</h1>
        <p>EV Charge 서비스를 이용하기 위한 계정을 생성합니다.</p>

        <%--
            회원가입 실패 메시지 출력 영역

            Controller에서
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            로 전달한 값을 출력한다.
        --%>
        <c:if test="${not empty errorMsg}">
            <div class="error-message">${errorMsg}</div>
        </c:if>

        <%--
            회원가입 form

            action:
            POST /member/join 요청을 보낸다.

            중요:
            input의 name 값은 MemberDTO의 필드명과 같아야 한다.
        --%>
        <form action="${pageContext.request.contextPath}/member/join" method="post">

            <%--
                MemberDTO.userId로 자동 매핑
                DB 컬럼: user_id
            --%>
            <div class="form-group">
                <label>아이디</label>
                <input type="text" name="userId" placeholder="아이디를 입력하세요." required>
            </div>

            <%--
                MemberDTO.password로 자동 매핑
                DB 컬럼: password
            --%>
            <div class="form-group">
                <label>비밀번호</label>
                <input type="password" name="password" placeholder="비밀번호를 입력하세요." required>
            </div>

            <%--
                MemberDTO.memberName으로 자동 매핑
                DB 컬럼: member_name
            --%>
            <div class="form-group">
                <label>이름</label>
                <input type="text" name="memberName" placeholder="이름을 입력하세요." required>
            </div>

            <%--
                MemberDTO.nickname으로 자동 매핑
                DB 컬럼: nickname
            --%>
            <div class="form-group">
                <label>닉네임</label>
                <input type="text" name="nickname" placeholder="닉네임을 입력하세요.">
            </div>

            <%--
                MemberDTO.phone으로 자동 매핑
                DB 컬럼: phone

                phone은 DB에서 unique지만 null 가능하다.
            --%>
            <div class="form-group">
                <label>휴대폰 번호</label>
                <input type="text" name="phone" placeholder="010-1234-5678">
            </div>

            <%--
                MemberDTO.email로 자동 매핑
                DB 컬럼: email

                email은 DB에서 not null + unique이므로 필수 입력
            --%>
            <div class="form-group">
                <label>이메일</label>
                <input type="email" name="email" placeholder="example@email.com" required>
            </div>

            <button type="submit" class="join-btn">회원가입</button>

        </form>

        <div class="join-bottom">
            <span>이미 계정이 있으신가요?</span>

            <%--
                로그인 화면으로 이동
                GET /member/login
            --%>
            <a href="${pageContext.request.contextPath}/member/login">로그인</a>
        </div>

    </section>

</main>

</body>
</html>