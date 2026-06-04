<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EV Charge 회원가입</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/member/join.css">
</head>
<body>

<main class="join-page">

    <section class="join-card">

        <h1>회원가입</h1>

        <c:if test="${not empty errorMsg}">
            <div class="error-message">${errorMsg}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/member/join"
              method="post"
              enctype="multipart/form-data"
              id="joinForm">

            <div class="form-group">
                <label>아이디</label>

                <div class="input-with-btn">
                    <input type="text"
                           name="userId"
                           id="userId"
                           placeholder="아이디를 입력하세요."
                           required>

                    <button type="button"
                            id="checkUserIdBtn">
                        중복확인
                    </button>
                </div>

                <small id="userIdCheckText"></small>
            </div>

			<div class="form-group">
			    <label>비밀번호</label>
			    <input type="password"
			           name="password"
			           id="password"
			           placeholder="영문자+숫자+특수문자 8~20자"
			           required>
			
			    <small id="passwordRuleText"></small>
			</div>

			<div class="form-group">
			    <label>비밀번호 확인</label>
			    <input type="password"
			           id="passwordConfirm"
			           placeholder="비밀번호를 다시 입력하세요."
			           required>
			
			    <small id="passwordConfirmText"></small>
			</div>

            <div class="form-group">
                <label>이름</label>
                <input type="text"
                       name="memberName"
                       placeholder="이름을 입력하세요."
                       required>
            </div>

			<div class="form-group">
			    <label>닉네임</label>
			    <input type="text"
			           name="nickname"
			           id="nickname"
			           class="join-input"
			           placeholder="닉네임을 입력하세요."
			           required>
			
			    <small id="nicknameCheckText"></small>
			</div>

            <div class="form-group">
                <label>이메일</label>

                <div class="email-row">
                    <input type="text"
                           name="emailId"
                           id="emailId"
                           placeholder="dyddl456"
                           required>

                    <span>@</span>

                    <select name="emailDomain"
                            id="emailDomain"
                            required>
                        <option value="naver.com">naver.com</option>
                        <option value="gmail.com">gmail.com</option>
                        <option value="daum.net">daum.net</option>
                        <option value="kakao.com">kakao.com</option>
                        <option value="direct">직접입력</option>
                    </select>
                </div>

				<input type="text"
				       name="emailDomainDirect"
				       id="emailDomainDirect"
				       class="direct-email"
				       placeholder="도메인만 입력 예: nate.com">

                <div class="email-auth-row">
                    <button type="button"
                            id="sendEmailCodeBtn"
                            class="email-auth-btn">
                        인증번호 발송
                    </button>
                </div>

                <div class="email-code-row">
                    <input type="text"
                           id="emailCode"
                           maxlength="6"
                           placeholder="인증번호 6자리">

                    <button type="button"
                            id="verifyEmailCodeBtn"
                            class="email-auth-btn">
                        인증확인
                    </button>
                </div>
                
				<small id="emailCheckText"></small>
				<small id="emailAuthText"></small>
            </div>

            <div class="form-group">
                <label>휴대폰 번호</label>

                <div class="phone-row">
                    <input type="text"
                           id="phone1"
                           value="010"
                           maxlength="3"
                           readonly>

                    <span>-</span>

                    <input type="text"
                           id="phone2"
                           maxlength="4"
                           required>

                    <span>-</span>

                    <input type="text"
                           id="phone3"
                           maxlength="4"
                           required>
                </div>

				<input type="hidden"
				       name="phone"
				       id="phone">
				
				<small id="phoneCheckText"></small>
            </div>

            <div class="form-group">
                <label>프로필 이미지</label>

                <div class="profile-preview-box">
                    <img src="${pageContext.request.contextPath}/images/member/profile/default-profile.png"
                         id="profilePreview"
                         class="profile-preview"
                         alt="기본 프로필">
                </div>

                <label class="profile-upload-btn">
                    이미지 선택
                    <input type="file"
                           name="profileImage"
                           id="profileImage"
                           accept="image/*">
                </label>

                <small>선택하지 않으면 기본 프로필 이미지가 적용됩니다.</small>
            </div>

            <div class="agree-box">
                <label>
                    <input type="checkbox"
                           id="agreeTerms"
                           required>
                    개인정보 수집 및 이용에 동의합니다.
                </label>
            </div>

            <button type="submit"
                    class="join-btn">
                회원가입
            </button>

        </form>

        <div class="join-bottom">
            <span>이미 계정이 있으신가요?</span>
            <a href="${pageContext.request.contextPath}/login">로그인</a>
        </div>

    </section>

</main>

<script>
    const EV_CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/member/join.js"></script>

</body>
</html>