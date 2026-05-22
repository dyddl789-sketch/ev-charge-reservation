<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

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
            <div class="visual-overlay">
                <h1>EV Charge</h1>
                <p>전기차 충전 최적화 및 예약 관리 서비스</p>
            </div>
        </section>

        <!-- 오른쪽 로그인 영역 -->
        <section class="login-form-area">

            <div class="login-header">
                <h2>로그인</h2>
                <p>서비스를 이용하려면 로그인하세요.</p>
            </div>

         <!-- 사용자 / 관리자 선택 -->
		<div class="login-tab">
    		<button type="button" class="tab-btn active" data-type="USER">일반회원</button>
    		<button type="button" class="tab-btn" data-type="ADMIN">관리자</button>
		</div>

            <!-- 로그인 폼 -->
            <form action="/member/loginProcess" method="post" class="login-form">
				
				<!-- 일반회원 / 관리자 구분값 -->
    			<input type="hidden" name="userType" id="userType" value="USER">
    			
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

                    <a href="/member/find">아이디/비밀번호 찾기</a>
                </div>

                <button type="submit" class="login-btn">
                    로그인
                </button>

            </form>

            <!-- 카카오 로그인 -->
            <a href="/oauth2/authorization/kakao" class="kakao-btn">
                카카오로 로그인
            </a>

            <!-- 회원가입 -->
            <a href="/member/join" class="join-btn">
                회원가입
            </a>

        </section>

    </div>

</div>

<script>
    const tabButtons = document.querySelectorAll(".tab-btn");
    const userTypeInput = document.getElementById("userType");
    const loginGuideText = document.querySelector(".login-header p");

    tabButtons.forEach(function(button) {
        button.addEventListener("click", function() {
            // 모든 탭 active 제거
            tabButtons.forEach(function(btn) {
                btn.classList.remove("active");
            });

            // 클릭한 탭 active 추가
            button.classList.add("active");

            // USER / ADMIN 값 변경
            const selectedType = button.dataset.type;
            userTypeInput.value = selectedType;

            // 안내 문구 변경
            if (selectedType === "ADMIN") {
                loginGuideText.textContent = "관리자 계정으로 로그인하세요.";
            } else {
                loginGuideText.textContent = "서비스를 이용하려면 로그인하세요.";
            }
        });
    });
</script>

</body>
</html>