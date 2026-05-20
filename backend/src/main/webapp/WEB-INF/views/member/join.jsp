<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원가입</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member/join.css">
</head>
<body>

<div class="join-page">

    <div class="join-card">

        <div class="join-header">
            <h1>회원가입</h1>
            <p>EV Charge 서비스를 이용하기 위해 회원 정보를 입력하세요.</p>
        </div>

        <form action="/member/joinProcess" method="post" class="join-form" onsubmit="return joinSubmit();">

            <!-- DB 기본값 처리용 -->
            <input type="hidden" name="userType" value="USER">
            <input type="hidden" name="loginType" value="LOCAL">
            <input type="hidden" name="status" value="ACTIVE">

            <!-- 아이디 -->
            <div class="form-group">
                <label for="userId">아이디</label>
                <span class="guide-text">로그인에 사용할 아이디입니다.</span>

                <div class="id-row">
                    <input type="text" id="userId" name="userId" placeholder="아이디 입력 (4~20자)">
                    <button type="button" class="check-btn">중복 확인</button>
                </div>
            </div>

            <!-- 비밀번호 -->
            <div class="form-group">
                <label for="password">비밀번호</label>
                <span class="guide-text">소셜 로그인 회원은 비밀번호가 없을 수 있습니다.</span>

                <input type="password" id="password" name="password"
                       placeholder="비밀번호 입력 (8~20자)">
            </div>

            <!-- 비밀번호 확인 -->
            <div class="form-group">
                <label for="passwordCheck">비밀번호 확인</label>
                <span class="error-text" id="passwordError">비밀번호가 일치하지 않습니다.</span>

                <input type="password" id="passwordCheck" name="passwordCheck"
                       placeholder="비밀번호 재입력">
            </div>

            <!-- 이름 -->
            <div class="form-group">
                <label for="memberName">이름</label>
                <input type="text" id="memberName" name="memberName" placeholder="이름을 입력해주세요">
            </div>

            <!-- 닉네임 -->
            <div class="form-group">
                <label for="nickname">닉네임</label>
                <input type="text" id="nickname" name="nickname" placeholder="닉네임을 입력해주세요">
            </div>

            <!-- 전화번호 -->
            <div class="form-group">
                <label for="phone">전화번호</label>
                <input type="text" id="phone" name="phone" placeholder="01012345678">
            </div>

            <!-- 이메일 -->
            <div class="form-group">
                <label for="email">이메일</label>
                <input type="email" id="email" name="email" placeholder="example@email.com">
            </div>

            <div class="button-row">
                <button type="submit" class="submit-btn">가입하기</button>
                <a href="/member/login" class="cancel-btn">가입취소</a>
            </div>

        </form>

    </div>

</div>

<script>
    function joinSubmit() {
        const userId = document.getElementById("userId").value.trim();
        const password = document.getElementById("password").value;
        const passwordCheck = document.getElementById("passwordCheck").value;
        const memberName = document.getElementById("memberName").value.trim();
        const phone = document.getElementById("phone").value.trim();
        const email = document.getElementById("email").value.trim();
        const passwordError = document.getElementById("passwordError");

        passwordError.style.display = "none";

        if (userId === "") {
            alert("아이디를 입력해주세요.");
            document.getElementById("userId").focus();
            return false;
        }

        if (password === "") {
            alert("비밀번호를 입력해주세요.");
            document.getElementById("password").focus();
            return false;
        }

        if (password !== passwordCheck) {
            passwordError.style.display = "inline-block";
            document.getElementById("passwordCheck").focus();
            return false;
        }

        if (memberName === "") {
            alert("이름을 입력해주세요.");
            document.getElementById("memberName").focus();
            return false;
        }

        if (phone === "") {
            alert("전화번호를 입력해주세요.");
            document.getElementById("phone").focus();
            return false;
        }

        if (email === "") {
            alert("이메일을 입력해주세요.");
            document.getElementById("email").focus();
            return false;
        }

        return true;
    }
</script>

</body>
</html>