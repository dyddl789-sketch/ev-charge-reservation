import { useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  checkEmail,
  checkNickname,
  checkPhone,
  checkUserId,
  sendEmailCode,
  verifyEmailCode,
} from "../../apis/memberApi";
import "../../styles/auth.css";

const initialCheck = {
  userId: { checked: false, value: "" },
  nickname: { checked: false, value: "" },
  email: { checked: false, value: "" },
  emailVerified: { checked: false, value: "" },
  phone: { checked: false, value: "" },
};

const JoinPage = () => {
  console.log("JoinPage 렌더링");

  const phone3Ref = useRef(null);
  const fileInputRef = useRef(null);

  const [form, setForm] = useState({
    userId: "",
    password: "",
    passwordConfirm: "",
    memberName: "",
    nickname: "",
    emailId: "",
    emailDomain: "naver.com",
    emailDomainDirect: "",
    emailCode: "",
    phone1: "010",
    phone2: "",
    phone3: "",
    agreeTerms: false,
  });

  const [checkState, setCheckState] = useState(initialCheck);
  const [messages, setMessages] = useState({});
  const [profilePreview, setProfilePreview] = useState("/images/member/profile/default-profile.png");
  const [sendingEmail, setSendingEmail] = useState(false);

  const fullEmail = useMemo(() => {
    const id = form.emailId.trim();
    const domain = form.emailDomain === "direct" ? form.emailDomainDirect.trim() : form.emailDomain;

    if (!id || !domain) {
      return "";
    }

    return `${id}@${domain}`;
  }, [form.emailId, form.emailDomain, form.emailDomainDirect]);

  const fullPhone = useMemo(() => {
    return `${form.phone1}-${form.phone2}-${form.phone3}`;
  }, [form.phone1, form.phone2, form.phone3]);

  const setMessage = (key, text, type = "") => {
    setMessages((prev) => ({
      ...prev,
      [key]: { text, type },
    }));
  };

  const resetCheck = (key) => {
    setCheckState((prev) => ({
      ...prev,
      [key]: { checked: false, value: "" },
    }));
  };

  const resetEmailCheck = () => {
    setCheckState((prev) => ({
      ...prev,
      email: { checked: false, value: "" },
      emailVerified: { checked: false, value: "" },
    }));
    setMessage("email", "", "");
    setMessage("emailAuth", "", "");
  };

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    const nextValue = type === "checkbox" ? checked : value;

    console.log("회원가입 입력 변경 =>", name, nextValue);

    setForm((prev) => ({
      ...prev,
      [name]: nextValue,
    }));

    if (name === "userId") {
      resetCheck("userId");
      setMessage("userId", "", "");
    }

    if (name === "nickname") {
      resetCheck("nickname");
      setMessage("nickname", "", "");
    }

    if (["emailId", "emailDomain", "emailDomainDirect"].includes(name)) {
      resetEmailCheck();
    }
  };

  const handlePhoneChange = (event) => {
    const { name, value } = event.target;
    const onlyNumber = value.replace(/[^0-9]/g, "");

    console.log("휴대폰 입력 변경 =>", name, onlyNumber);

    setForm((prev) => ({
      ...prev,
      [name]: onlyNumber,
    }));

    resetCheck("phone");
    setMessage("phone", "", "");

    if (name === "phone2" && onlyNumber.length === 4) {
      phone3Ref.current?.focus();
    }
  };

  const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const checkPasswordRule = () => {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$/;

    if (!form.password) {
      setMessage("password", "", "");
      return false;
    }

    if (!passwordRegex.test(form.password)) {
      setMessage("password", "영문자, 숫자, 특수문자를 포함한 8~20자로 입력하세요.", "error");
      return false;
    }

    setMessage("password", "사용 가능한 비밀번호입니다.", "success");
    return true;
  };

  const checkPasswordConfirm = () => {
    if (!form.passwordConfirm) {
      setMessage("passwordConfirm", "", "");
      return false;
    }

    if (form.password !== form.passwordConfirm) {
      setMessage("passwordConfirm", "비밀번호가 서로 일치하지 않습니다.", "error");
      return false;
    }

    setMessage("passwordConfirm", "비밀번호가 일치합니다.", "success");
    return true;
  };

  const handleUserIdCheck = async () => {
    const value = form.userId.trim();

    if (value.length < 4) {
      setMessage("userId", "아이디는 4자 이상 입력하세요.", "error");
      return;
    }

    try {
      const data = await checkUserId(value);

      if (data.available) {
        setCheckState((prev) => ({ ...prev, userId: { checked: true, value } }));
        setMessage("userId", "사용 가능한 아이디입니다.", "success");
      } else {
        resetCheck("userId");
        setMessage("userId", "이미 사용 중인 아이디입니다.", "error");
      }
    } catch (error) {
      console.log("아이디 중복확인 오류", error);
      setMessage("userId", "아이디 확인 중 오류가 발생했습니다.", "error");
    }
  };

  const handleNicknameCheck = async () => {
    const value = form.nickname.trim();

    if (!value) {
      setMessage("nickname", "", "");
      return;
    }

    if (value.length < 2) {
      setMessage("nickname", "닉네임은 2자 이상 입력하세요.", "error");
      return;
    }

    try {
      const data = await checkNickname(value);

      if (data.available) {
        setCheckState((prev) => ({ ...prev, nickname: { checked: true, value } }));
        setMessage("nickname", "사용 가능한 닉네임입니다.", "success");
      } else {
        resetCheck("nickname");
        setMessage("nickname", "이미 사용 중인 닉네임입니다.", "error");
      }
    } catch (error) {
      console.log("닉네임 중복확인 오류", error);
      setMessage("nickname", "닉네임 확인 중 오류가 발생했습니다.", "error");
    }
  };

  const handleEmailCheck = async () => {
    if (!fullEmail) {
      setMessage("email", "", "");
      return;
    }

    if (form.emailDomainDirect.includes("@")) {
      setMessage("email", "직접입력 칸에는 nate.com 처럼 도메인만 입력하세요.", "error");
      return;
    }

    if (!isValidEmail(fullEmail)) {
      setMessage("email", "이메일 형식이 올바르지 않습니다.", "error");
      return;
    }

    try {
      const data = await checkEmail(fullEmail);

      if (data.available) {
        setCheckState((prev) => ({
          ...prev,
          email: { checked: true, value: fullEmail },
          emailVerified: { checked: false, value: "" },
        }));
        setMessage("email", "사용 가능한 이메일입니다.", "success");
      } else {
        resetEmailCheck();
        setMessage("email", "이미 사용 중인 이메일입니다.", "error");
      }
    } catch (error) {
      console.log("이메일 중복확인 오류", error);
      setMessage("email", "이메일 확인 중 오류가 발생했습니다.", "error");
    }
  };

  const handleSendEmailCode = async () => {
    if (!isValidEmail(fullEmail)) {
      setMessage("email", "이메일 형식이 올바르지 않습니다.", "error");
      return;
    }

    if (!checkState.email.checked || checkState.email.value !== fullEmail) {
      alert("사용 가능한 이메일인지 먼저 확인해 주세요.");
      handleEmailCheck();
      return;
    }

    try {
      setSendingEmail(true);
      const data = await sendEmailCode(fullEmail);
      setMessage("emailAuth", data.message, data.success ? "success" : "error");
    } catch (error) {
      console.log("인증번호 발송 오류", error);
      setMessage("emailAuth", "인증번호 발송 중 오류가 발생했습니다.", "error");
    } finally {
      setSendingEmail(false);
    }
  };

  const handleVerifyEmailCode = async () => {
    const code = form.emailCode.trim();

    if (!isValidEmail(fullEmail)) {
      alert("이메일을 정확히 입력하세요.");
      return;
    }

    if (checkState.email.value !== fullEmail) {
      alert("이메일이 변경되었습니다. 다시 인증번호를 발송해 주세요.");
      return;
    }

    if (code.length !== 6) {
      alert("인증번호 6자리를 입력하세요.");
      return;
    }

    try {
      const data = await verifyEmailCode(fullEmail, code);
      setMessage("emailAuth", data.message, data.success ? "success" : "error");

      if (data.success) {
        setCheckState((prev) => ({ ...prev, emailVerified: { checked: true, value: fullEmail } }));
      } else {
        setCheckState((prev) => ({ ...prev, emailVerified: { checked: false, value: "" } }));
      }
    } catch (error) {
      console.log("이메일 인증확인 오류", error);
      setMessage("emailAuth", "이메일 인증 확인 중 오류가 발생했습니다.", "error");
    }
  };

  const handlePhoneCheck = async () => {
    if (!form.phone2 && !form.phone3) {
      setMessage("phone", "", "");
      return;
    }

    if (form.phone2.length !== 4 || form.phone3.length !== 4) {
      setMessage("phone", "휴대폰 번호를 정확히 입력하세요.", "error");
      return;
    }

    try {
      const data = await checkPhone(fullPhone);

      if (data.available) {
        setCheckState((prev) => ({ ...prev, phone: { checked: true, value: fullPhone } }));
        setMessage("phone", "사용 가능한 휴대폰 번호입니다.", "success");
      } else {
        resetCheck("phone");
        setMessage("phone", "이미 사용 중인 전화번호입니다.", "error");
      }
    } catch (error) {
      console.log("휴대폰 중복확인 오류", error);
      setMessage("phone", "휴대폰 번호 확인 중 오류가 발생했습니다.", "error");
    }
  };

  const handleProfileChange = (event) => {
    const file = event.target.files?.[0];

    console.log("프로필 이미지 선택 =>", file);

    if (!file) {
      return;
    }

    setProfilePreview(URL.createObjectURL(file));
  };

  const handleSubmit = (event) => {
    console.log("회원가입 submit 검증 시작");

    const currentUserId = form.userId.trim();
    const currentNickname = form.nickname.trim();

    if (!checkState.userId.checked || checkState.userId.value !== currentUserId) {
      alert("아이디 중복확인을 해주세요.");
      event.preventDefault();
      return;
    }

    if (!checkPasswordRule()) {
      alert("비밀번호 형식을 확인해 주세요.");
      event.preventDefault();
      return;
    }

    if (!checkPasswordConfirm()) {
      alert("비밀번호 확인을 다시 입력해 주세요.");
      event.preventDefault();
      return;
    }

    if (!checkState.nickname.checked || checkState.nickname.value !== currentNickname) {
      alert("닉네임 중복확인을 완료해 주세요.");
      event.preventDefault();
      return;
    }

    if (!checkState.email.checked || checkState.email.value !== fullEmail) {
      alert("사용 가능한 이메일인지 확인해 주세요.");
      event.preventDefault();
      return;
    }

    if (!checkState.emailVerified.checked || checkState.emailVerified.value !== fullEmail) {
      alert("이메일 인증을 완료해 주세요.");
      event.preventDefault();
      return;
    }

    if (!checkState.phone.checked || checkState.phone.value !== fullPhone) {
      alert("사용 가능한 휴대폰 번호인지 확인해 주세요.");
      event.preventDefault();
      return;
    }

    console.log("회원가입 submit 통과 => backend /member/join 전송");
  };

  const Message = ({ name }) => {
    const message = messages[name];

    if (!message?.text) {
      return <small />;
    }

    return <small className={message.type}>{message.text}</small>;
  };

  return (
    <main className="join-page">
      <section className="join-card">
        <h1>회원가입</h1>

        {/* 기존 Spring MVC /member/join multipart form 처리와 연결 */}
        <form action="/member/join" method="post" encType="multipart/form-data" id="joinForm" onSubmit={handleSubmit}>
          <div className="form-group">
            <label>아이디</label>
            <div className="input-with-btn">
              <input type="text" name="userId" value={form.userId} onChange={handleChange} placeholder="아이디를 입력하세요." required />
              <button type="button" onClick={handleUserIdCheck}>중복확인</button>
            </div>
            <Message name="userId" />
          </div>

          <div className="form-group">
            <label>비밀번호</label>
            <input type="password" name="password" value={form.password} onChange={handleChange} onBlur={checkPasswordRule} placeholder="영문자+숫자+특수문자 8~20자" required />
            <Message name="password" />
          </div>

          <div className="form-group">
            <label>비밀번호 확인</label>
            <input type="password" value={form.passwordConfirm} name="passwordConfirm" onChange={handleChange} onBlur={checkPasswordConfirm} placeholder="비밀번호를 다시 입력하세요." required />
            <Message name="passwordConfirm" />
          </div>

          <div className="form-group">
            <label>이름</label>
            <input type="text" name="memberName" value={form.memberName} onChange={handleChange} placeholder="이름을 입력하세요." required />
          </div>

          <div className="form-group">
            <label>닉네임</label>
            <input type="text" name="nickname" value={form.nickname} onChange={handleChange} onBlur={handleNicknameCheck} className="join-input" placeholder="닉네임을 입력하세요." required />
            <Message name="nickname" />
          </div>

          <div className="form-group">
            <label>이메일</label>
            <div className="email-row">
              <input type="text" name="emailId" value={form.emailId} onChange={handleChange} onBlur={handleEmailCheck} placeholder="dyddl456" required />
              <span>@</span>
              <select name="emailDomain" value={form.emailDomain} onChange={handleChange} onBlur={handleEmailCheck} required>
                <option value="naver.com">naver.com</option>
                <option value="gmail.com">gmail.com</option>
                <option value="daum.net">daum.net</option>
                <option value="kakao.com">kakao.com</option>
                <option value="direct">직접입력</option>
              </select>
            </div>

            {form.emailDomain === "direct" && (
              <input type="text" name="emailDomainDirect" value={form.emailDomainDirect} onChange={handleChange} onBlur={handleEmailCheck} className="direct-email show" placeholder="도메인만 입력 예: nate.com" />
            )}

            <div className="email-auth-row">
              <button type="button" onClick={handleSendEmailCode} disabled={sendingEmail} className="email-auth-btn">
                {sendingEmail ? "발송 중..." : "인증번호 발송"}
              </button>
            </div>

            <div className="email-code-row">
              <input type="text" name="emailCode" value={form.emailCode} onChange={handleChange} maxLength="6" placeholder="인증번호 6자리" />
              <button type="button" onClick={handleVerifyEmailCode} className="email-auth-btn">인증확인</button>
            </div>

            <Message name="email" />
            <Message name="emailAuth" />
          </div>

          <div className="form-group">
            <label>휴대폰 번호</label>
            <div className="phone-row">
              <input type="text" name="phone1" value={form.phone1} maxLength="3" readOnly />
              <span>-</span>
              <input type="text" name="phone2" value={form.phone2} onChange={handlePhoneChange} onBlur={handlePhoneCheck} maxLength="4" required />
              <span>-</span>
              <input type="text" name="phone3" value={form.phone3} onChange={handlePhoneChange} onBlur={handlePhoneCheck} maxLength="4" ref={phone3Ref} required />
            </div>
            <input type="hidden" name="phone" value={fullPhone} readOnly />
            <Message name="phone" />
          </div>

          <div className="form-group">
            <label>프로필 이미지</label>
            <div className="profile-preview-box">
              <img src={profilePreview} id="profilePreview" className="profile-preview" alt="기본 프로필" />
            </div>

            <label className="profile-upload-btn">
              이미지 선택
              <input type="file" name="profileImage" accept="image/*" ref={fileInputRef} onChange={handleProfileChange} />
            </label>

            <small>선택하지 않으면 기본 프로필 이미지가 적용됩니다.</small>
            <small>지원 형식: jpg, jpeg, png, gif, webp / 최대 5MB</small>
          </div>

          <div className="agree-box">
            <label>
              <input type="checkbox" name="agreeTerms" checked={form.agreeTerms} onChange={handleChange} required />
              개인정보 수집 및 이용에 동의합니다.
            </label>
          </div>

          <button type="submit" className="join-submit-btn">
            회원가입
          </button>
        </form>

        <div className="join-bottom">
          <span>이미 계정이 있으신가요?</span>
          <Link to="/login">로그인</Link>
        </div>
      </section>
    </main>
  );
};

export default JoinPage;
