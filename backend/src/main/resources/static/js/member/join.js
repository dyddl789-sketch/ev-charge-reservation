document.addEventListener("DOMContentLoaded", function () {

    const joinForm = document.getElementById("joinForm");

    const userId = document.getElementById("userId");
    const checkUserIdBtn = document.getElementById("checkUserIdBtn");
    const userIdCheckText = document.getElementById("userIdCheckText");

    const password = document.getElementById("password");
    const passwordConfirm = document.getElementById("passwordConfirm");
    const passwordRuleText = document.getElementById("passwordRuleText");
    const passwordConfirmText = document.getElementById("passwordConfirmText");

    const nickname = document.getElementById("nickname");
    const nicknameCheckText = document.getElementById("nicknameCheckText");

    const emailId = document.getElementById("emailId");
    const emailDomain = document.getElementById("emailDomain");
    const emailDomainDirect = document.getElementById("emailDomainDirect");
    const emailCheckText = document.getElementById("emailCheckText");

    const sendEmailCodeBtn = document.getElementById("sendEmailCodeBtn");
    const verifyEmailCodeBtn = document.getElementById("verifyEmailCodeBtn");
    const emailCode = document.getElementById("emailCode");
    const emailAuthText = document.getElementById("emailAuthText");

    const phone1 = document.getElementById("phone1");
    const phone2 = document.getElementById("phone2");
    const phone3 = document.getElementById("phone3");
    const phone = document.getElementById("phone");
    const phoneCheckText = document.getElementById("phoneCheckText");

    const profileImage = document.getElementById("profileImage");
    const profilePreview = document.getElementById("profilePreview");

    let isUserIdChecked = false;
    let checkedUserId = "";

    let isNicknameAvailable = false;
    let checkedNickname = "";

    let isEmailAvailable = false;
    let checkedEmail = "";

    let isEmailVerified = false;
    let verifiedEmail = "";

    let isPhoneAvailable = false;
    let checkedPhone = "";

    // 이메일 직접입력 표시
    emailDomain.addEventListener("change", function () {

        resetEmailCheck();

        if (emailDomain.value === "direct") {
            emailDomainDirect.style.display = "block";
            emailDomainDirect.required = true;
        } else {
            emailDomainDirect.style.display = "none";
            emailDomainDirect.required = false;
            emailDomainDirect.value = "";
        }

        checkEmailAvailable();
    });

    // 아이디 변경 시 중복확인 초기화
    userId.addEventListener("input", function () {
        isUserIdChecked = false;
        checkedUserId = "";
        setMessage(userIdCheckText, "", "");
    });

    // 아이디 중복확인
    checkUserIdBtn.addEventListener("click", function () {

        const value = userId.value.trim();

        if (value.length < 4) {
            setMessage(userIdCheckText, "아이디는 4자 이상 입력하세요.", "error");
            userId.focus();
            return;
        }

        fetch(EV_CONTEXT_PATH + "/member/check-user-id?userId=" + encodeURIComponent(value))
            .then(response => response.json())
            .then(data => {

                if (data.available) {
                    isUserIdChecked = true;
                    checkedUserId = value;
                    setMessage(userIdCheckText, "사용 가능한 아이디입니다.", "success");
                } else {
                    isUserIdChecked = false;
                    checkedUserId = "";
                    setMessage(userIdCheckText, "이미 사용 중인 아이디입니다.", "error");
                }
            })
            .catch(function () {
                setMessage(userIdCheckText, "아이디 확인 중 오류가 발생했습니다.", "error");
            });
    });

    // 비밀번호 형식 실시간 검증
    password.addEventListener("input", function () {
        checkPasswordRule();
        checkPasswordConfirm();
    });

    // 비밀번호 확인 실시간 검증
    passwordConfirm.addEventListener("input", checkPasswordConfirm);

    // 닉네임 중복검사
    nickname.addEventListener("blur", checkNicknameAvailable);

    nickname.addEventListener("input", function () {
        isNicknameAvailable = false;
        checkedNickname = "";
        setMessage(nicknameCheckText, "", "");
    });

	// 이메일 입력값 변경 시 기존 중복확인/인증 결과 초기화
	emailId.addEventListener("input", resetEmailCheck);

	emailDomainDirect.addEventListener("input", function () {

	    if (emailDomainDirect.value.includes("@")) {

	        emailDomainDirect.value =
	            emailDomainDirect.value.replaceAll("@", "");

	        setMessage(
	            emailCheckText,
	            "도메인만 입력하세요. 예: nate.com",
	            "error"
	        );

	        return;
	    }

	    resetEmailCheck();
	});

	// 이메일 입력 완료 후 중복확인
	emailId.addEventListener("blur", checkEmailAvailable);
	emailDomainDirect.addEventListener("blur", checkEmailAvailable);

    // 이메일 인증번호 발송
    sendEmailCodeBtn.addEventListener("click", function () {

        const email = getFullEmail();

        if (!isValidEmail(email)) {
            setMessage(emailCheckText, "이메일 형식이 올바르지 않습니다.", "error");
            return;
        }

        if (!isEmailAvailable || checkedEmail !== email) {
            alert("사용 가능한 이메일인지 먼저 확인해 주세요.");
            checkEmailAvailable();
            return;
        }

        isEmailVerified = false;
        verifiedEmail = "";
        setMessage(emailAuthText, "", "");

        sendEmailCodeBtn.disabled = true;
        sendEmailCodeBtn.textContent = "발송 중...";

        fetch(EV_CONTEXT_PATH + "/member/email-code/send", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: "email=" + encodeURIComponent(email)
        })
            .then(response => response.json())
            .then(data => {

                setMessage(
                    emailAuthText,
                    data.message,
                    data.success ? "success" : "error"
                );

                if (data.success) {
                    emailCode.focus();
                }
            })
            .catch(function () {
                setMessage(emailAuthText, "인증번호 발송 중 오류가 발생했습니다.", "error");
            })
            .finally(function () {
                sendEmailCodeBtn.disabled = false;
                sendEmailCodeBtn.textContent = "인증번호 발송";
            });
    });

    // 이메일 인증번호 확인
    verifyEmailCodeBtn.addEventListener("click", function () {

        const email = getFullEmail();
        const code = emailCode.value.trim();

        if (!isValidEmail(email)) {
            alert("이메일을 정확히 입력하세요.");
            return;
        }

        if (checkedEmail !== email) {
            alert("이메일이 변경되었습니다. 다시 인증번호를 발송해 주세요.");
            return;
        }

        if (code.length !== 6) {
            alert("인증번호 6자리를 입력하세요.");
            emailCode.focus();
            return;
        }

        fetch(EV_CONTEXT_PATH + "/member/email-code/verify", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body:
                "email=" + encodeURIComponent(email)
                + "&code=" + encodeURIComponent(code)
        })
            .then(response => response.json())
            .then(data => {

                setMessage(
                    emailAuthText,
                    data.message,
                    data.success ? "success" : "error"
                );

                if (data.success) {
                    isEmailVerified = true;
                    verifiedEmail = email;
                } else {
                    isEmailVerified = false;
                    verifiedEmail = "";
                }
            })
            .catch(function () {
                setMessage(emailAuthText, "이메일 인증 확인 중 오류가 발생했습니다.", "error");
            });
    });

    // 휴대폰 숫자만 입력
    [phone2, phone3].forEach(input => {

        input.addEventListener("input", function () {
            this.value = this.value.replace(/[^0-9]/g, "");

            isPhoneAvailable = false;
            checkedPhone = "";
            setMessage(phoneCheckText, "", "");

            if (this === phone2 && this.value.length === 4) {
                phone3.focus();
            }
        });

        input.addEventListener("blur", checkPhoneAvailable);
    });

    // 회원가입 프로필 이미지 미리보기
    if (profileImage && profilePreview) {
        profileImage.addEventListener("change", function () {

            const file = this.files[0];

            if (!file) {
                return;
            }

            profilePreview.src = URL.createObjectURL(file);
        });
    }

    // 회원가입 최종 검증
    joinForm.addEventListener("submit", function (event) {

        const currentUserId = userId.value.trim();
        const currentNickname = nickname.value.trim();
        const currentEmail = getFullEmail();
        const currentPhone = getFullPhone();

        if (!isUserIdChecked || checkedUserId !== currentUserId) {
            alert("아이디 중복확인을 해주세요.");
            userId.focus();
            event.preventDefault();
            return;
        }

        if (!checkPasswordRule()) {
            alert("비밀번호 형식을 확인해 주세요.");
            password.focus();
            event.preventDefault();
            return;
        }

        if (!checkPasswordConfirm()) {
            alert("비밀번호 확인을 다시 입력해 주세요.");
            passwordConfirm.focus();
            event.preventDefault();
            return;
        }

        if (!isNicknameAvailable || checkedNickname !== currentNickname) {
            alert("닉네임 중복확인을 완료해 주세요.");
            nickname.focus();
            event.preventDefault();
            return;
        }

        if (!isEmailAvailable || checkedEmail !== currentEmail) {
            alert("사용 가능한 이메일인지 확인해 주세요.");
            emailId.focus();
            event.preventDefault();
            return;
        }

        if (!isEmailVerified || verifiedEmail !== currentEmail) {
            alert("이메일 인증을 완료해 주세요.");
            emailCode.focus();
            event.preventDefault();
            return;
        }

        if (!isPhoneAvailable || checkedPhone !== currentPhone) {
            alert("사용 가능한 휴대폰 번호인지 확인해 주세요.");
            phone2.focus();
            event.preventDefault();
            return;
        }

        phone.value = currentPhone;
    });

    function checkNicknameAvailable() {

        const value = nickname.value.trim();

        isNicknameAvailable = false;
        checkedNickname = "";

        if (value.length === 0) {
            setMessage(nicknameCheckText, "", "");
            return;
        }

        if (value.length < 2) {
            setMessage(nicknameCheckText, "닉네임은 2자 이상 입력하세요.", "error");
            return;
        }

        fetch(EV_CONTEXT_PATH + "/member/check-nickname?nickname=" + encodeURIComponent(value))
            .then(response => response.json())
            .then(data => {

                if (data.available) {
                    isNicknameAvailable = true;
                    checkedNickname = value;
                    setMessage(nicknameCheckText, "사용 가능한 닉네임입니다.", "success");
                } else {
                    isNicknameAvailable = false;
                    checkedNickname = "";
                    setMessage(nicknameCheckText, "이미 사용 중인 닉네임입니다.", "error");
                }
            })
            .catch(function () {
                setMessage(nicknameCheckText, "닉네임 확인 중 오류가 발생했습니다.", "error");
            });
    }

    function checkEmailAvailable() {

        const email = getFullEmail();

        isEmailAvailable = false;
        checkedEmail = "";
        isEmailVerified = false;
        verifiedEmail = "";

        if (email.length === 0) {
            setMessage(emailCheckText, "", "");
            setMessage(emailAuthText, "", "");
            return;
        }

        if (!isValidEmail(email)) {
            setMessage(emailCheckText, "이메일 형식이 올바르지 않습니다.", "error");
            return;
        }

        fetch(EV_CONTEXT_PATH + "/member/check-email?email=" + encodeURIComponent(email))
            .then(response => response.json())
            .then(data => {

                if (data.available) {
                    isEmailAvailable = true;
                    checkedEmail = email;
                    setMessage(emailCheckText, "사용 가능한 이메일입니다.", "success");
                } else {
                    isEmailAvailable = false;
                    checkedEmail = "";
                    setMessage(emailCheckText, "이미 사용 중인 이메일입니다.", "error");
                }
            })
            .catch(function () {
                setMessage(emailCheckText, "이메일 확인 중 오류가 발생했습니다.", "error");
            });
    }

    function checkPhoneAvailable() {

        const currentPhone = getFullPhone();

        isPhoneAvailable = false;
        checkedPhone = "";

        if (phone2.value.length === 0 && phone3.value.length === 0) {
            setMessage(phoneCheckText, "", "");
            return;
        }

        if (phone2.value.length !== 4 || phone3.value.length !== 4) {
            setMessage(phoneCheckText, "휴대폰 번호를 정확히 입력하세요.", "error");
            return;
        }

        fetch(EV_CONTEXT_PATH + "/member/check-phone?phone=" + encodeURIComponent(currentPhone))
            .then(response => response.json())
            .then(data => {

                if (data.available) {
                    isPhoneAvailable = true;
                    checkedPhone = currentPhone;
                    setMessage(phoneCheckText, "사용 가능한 휴대폰 번호입니다.", "success");
                } else {
                    isPhoneAvailable = false;
                    checkedPhone = "";
                    setMessage(phoneCheckText, "이미 사용 중인 전화번호입니다.", "error");
                }
            })
            .catch(function () {
                setMessage(phoneCheckText, "휴대폰 번호 확인 중 오류가 발생했습니다.", "error");
            });
    }

    function checkPasswordRule() {

        const passwordRegex =
            /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$/;

        if (password.value.length === 0) {
            setMessage(passwordRuleText, "", "");
            return false;
        }

        if (!passwordRegex.test(password.value)) {
            setMessage(
                passwordRuleText,
                "영문자, 숫자, 특수문자를 포함한 8~20자로 입력하세요.",
                "error"
            );
            return false;
        }

        setMessage(passwordRuleText, "사용 가능한 비밀번호입니다.", "success");
        return true;
    }

    function checkPasswordConfirm() {

        if (passwordConfirm.value.length === 0) {
            setMessage(passwordConfirmText, "", "");
            return false;
        }

        if (password.value !== passwordConfirm.value) {
            setMessage(passwordConfirmText, "비밀번호가 서로 일치하지 않습니다.", "error");
            return false;
        }

        setMessage(passwordConfirmText, "비밀번호가 일치합니다.", "success");
        return true;
    }

    function resetEmailCheck() {
        isEmailAvailable = false;
        checkedEmail = "";
        isEmailVerified = false;
        verifiedEmail = "";

        setMessage(emailCheckText, "", "");
        setMessage(emailAuthText, "", "");
    }

	function getFullEmail() {

	    const id = emailId.value.trim();

	    let domain = emailDomain.value === "direct"
	        ? emailDomainDirect.value.trim()
	        : emailDomain.value;

	    if (id.length === 0 || domain.length === 0) {
	        return "";
	    }

	    if (domain.includes("@")) {
	        setMessage(
	            emailCheckText,
	            "직접입력 칸에는 nate.com 처럼 도메인만 입력하세요.",
	            "error"
	        );
	        return "";
	    }

	    return id + "@" + domain;
	}

    function getFullPhone() {
        return phone1.value + "-" + phone2.value + "-" + phone3.value;
    }

    function isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function setMessage(element, message, className) {

        if (!element) {
            return;
        }

        element.textContent = message;
        element.className = className;
    }
});