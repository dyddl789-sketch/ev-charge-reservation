import api from "./api";

// 회원가입
export const join = (data) => {
  console.log("회원가입 요청", data);
  return api.post("/member/join", data);
};

// 아이디 중복확인
export const checkUserId = (userId) => {
  console.log("아이디 중복확인 요청", userId);
  return api.get("/member/check-user-id", { params: { userId } });
};

// 닉네임 중복확인
export const checkNickname = (nickname) => {
  console.log("닉네임 중복확인 요청", nickname);
  return api.get("/member/check-nickname", { params: { nickname } });
};

// 이메일 중복확인
export const checkEmail = (email) => {
  console.log("이메일 중복확인 요청", email);
  return api.get("/member/check-email", { params: { email } });
};

// 휴대폰 중복확인
export const checkPhone = (phone) => {
  console.log("휴대폰 중복확인 요청", phone);
  return api.get("/member/check-phone", { params: { phone } });
};

// 이메일 인증코드 발송
export const sendEmailCode = (email) => {
  console.log("이메일 인증코드 발송 요청", email);
  return api.post("/member/email-code/send", new URLSearchParams({ email }), {
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
  });
};

// 이메일 인증
export const verifyEmailCode = (email, code) => {
  console.log("이메일 인증 요청", email);
  return api.post("/member/email-code/verify", new URLSearchParams({ email, code }), {
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
  });
};

// 회원정보 조회(JSON API 추가 전에는 실패할 수 있음)
export const info = () => {
  console.log("회원정보 조회 요청");
  return api.get("/member/me");
};

// 회원정보 수정: 기존 백엔드 POST /member/mypage/edit 기준
export const update = (data) => {
  console.log("회원정보 수정 요청", data);

  const formData = new FormData();
  formData.append("nickname", data.nickname || "");
  formData.append("currentPassword", data.currentPassword || "");
  formData.append("newPassword", data.newPassword || "");
  formData.append("newPasswordConfirm", data.newPasswordConfirm || "");

  if (data.profileImage) {
    formData.append("profileImage", data.profileImage);
  } else {
    formData.append("profileImage", new Blob([]), "");
  }

  return api.post("/member/mypage/edit", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 회원탈퇴
export const remove = (memberId) => {
  console.log("회원탈퇴 요청", memberId);
  return api.delete(`/member/${memberId}`);
};
