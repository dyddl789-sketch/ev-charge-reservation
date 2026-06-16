<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c"
    uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">

<title>회원정보 수정</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/common/common.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/common/header.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member/member_edit.css">

</head>
<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="mypage-container">

    <div class="mypage-header">
        <h1>회원정보 수정</h1>
        <p>닉네임, 비밀번호, 프로필 이미지를 변경할 수 있습니다.</p>
    </div>

    <section class="mypage-card">

        <c:if test="${not empty errorMsg}">
            <div class="error-message">${errorMsg}</div>
        </c:if>

        <c:if test="${not empty msg}">
            <div class="success-message">${msg}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/member/mypage/edit"
              method="post"
              enctype="multipart/form-data"
              id="memberEditForm">

			<div class="profile-section">
			
			    <img src="${empty member.profileImageUrl
			            ? pageContext.request.contextPath.concat('/images/member/default-profile.png')
			            : pageContext.request.contextPath.concat(member.profileImageUrl)}"
			         alt="프로필 이미지"
			         class="member-profile-image"
			         id="profilePreview">
			
			    <label class="image-upload-btn">
			        사진 변경
			        <input type="file"
			               name="profileImage"
			               id="profileImage"
			               accept="image/*">
			    </label>
			
			    <small class="image-guide">
			        지원 형식: jpg, jpeg, png, gif, webp / 최대 5MB
			    </small>
			
			</div>

            <div class="info-grid">

                <div class="form-group">
                    <label>아이디</label>
                    <input type="text" value="${member.userId}" readonly>
                </div>

                <div class="form-group">
                    <label>이름</label>
                    <input type="text" value="${member.memberName}" readonly>
                </div>

                <div class="form-group">
                    <label>이메일</label>
                    <input type="text" value="${member.email}" readonly>
                </div>

                <div class="form-group">
                    <label>닉네임</label>
                    <input type="text"
                           name="nickname"
                           value="${member.nickname}"
                           placeholder="닉네임을 입력하세요."
                           required>
                </div>

            </div>

            <div class="password-box">

                <div class="section-title">
                    <h3>비밀번호 변경</h3>
                    <p>비밀번호를 변경하지 않으려면 비워두세요.</p>
                </div>

                <c:choose>

                    <c:when test="${member.loginType eq 'LOCAL'}">

                        <div class="password-grid">

                            <div class="form-group full">
                                <label>현재 비밀번호</label>
                                <input type="password"
                                       name="currentPassword"
                                       id="currentPassword"
                                       placeholder="현재 비밀번호를 입력하세요.">
                            </div>

                            <div class="form-group">
                                <label>새 비밀번호</label>
                                <input type="password"
                                       name="newPassword"
                                       id="newPassword"
                                       placeholder="영문자+숫자+특수문자 8~20자">
                            </div>

                            <div class="form-group">
                                <label>새 비밀번호 확인</label>
                                <input type="password"
                                       name="newPasswordConfirm"
                                       id="newPasswordConfirm"
                                       placeholder="새 비밀번호를 다시 입력하세요.">
                            </div>

                        </div>

                    </c:when>

                    <c:otherwise>

                        <div class="oauth-password-notice">
                            카카오 로그인 회원은 비밀번호를 변경할 수 없습니다.
                            비밀번호 변경은 카카오 계정에서 진행해 주세요.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <div class="button-area">

                <a href="${pageContext.request.contextPath}/main"
                   class="cancel-btn">
                    취소
                </a>

                <button type="submit"
                        class="save-btn">
                    저장하기
                </button>

            </div>

        </form>

    </section>

</main>

<script>
    document.addEventListener("DOMContentLoaded", function () {

        const form = document.getElementById("memberEditForm");

        const currentPassword = document.getElementById("currentPassword");
        const newPassword = document.getElementById("newPassword");
        const newPasswordConfirm = document.getElementById("newPasswordConfirm");

        const profileImage = document.getElementById("profileImage");
        const profilePreview = document.getElementById("profilePreview");

        // 프로필 이미지 미리보기
        if (profileImage) {
            profileImage.addEventListener("change", function () {

                const file = this.files[0];

                if (!file) {
                    return;
                }

                profilePreview.src = URL.createObjectURL(file);
            });
        }

        // 비밀번호 변경 입력 검증
        form.addEventListener("submit", function (event) {

            if (!currentPassword || !newPassword || !newPasswordConfirm) {
                return;
            }

            const currentPasswordValue = currentPassword.value.trim();
            const newPasswordValue = newPassword.value.trim();
            const newPasswordConfirmValue = newPasswordConfirm.value.trim();

            const passwordRegex =
                /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$/;

            const isPasswordChange =
                currentPasswordValue.length > 0
                || newPasswordValue.length > 0
                || newPasswordConfirmValue.length > 0;

            if (!isPasswordChange) {
                return;
            }

            if (currentPasswordValue.length === 0) {
                alert("현재 비밀번호를 입력하세요.");
                currentPassword.focus();
                event.preventDefault();
                return;
            }

            if (!passwordRegex.test(newPasswordValue)) {
                alert("새 비밀번호는 영문자, 숫자, 특수문자를 포함한 8~20자로 입력하세요.");
                newPassword.focus();
                event.preventDefault();
                return;
            }

            if (newPasswordValue !== newPasswordConfirmValue) {
                alert("새 비밀번호가 서로 일치하지 않습니다.");
                newPasswordConfirm.focus();
                event.preventDefault();
            }
        });
    });
</script>

</body>
</html>