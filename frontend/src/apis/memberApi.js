import api from "./api";

// 회원가입
export const join = (formData) => {
  console.log("회원가입 요청", formData);

  return api.post("/member/join", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
};

// 아이디 중복확인
export const checkUserId = async (userId) => {
  console.log("아이디 중복확인 요청", userId);
  const response = await api.get("/member/check-user-id", { params: { userId } });
  return response.data;
};

// 닉네임 중복확인
export const checkNickname = async (nickname) => {
  console.log("닉네임 중복확인 요청", nickname);
  const response = await api.get("/member/check-nickname", { params: { nickname } });
  return response.data;
};

// 이메일 중복확인
export const checkEmail = async (email) => {
  console.log("이메일 중복확인 요청", email);
  const response = await api.get("/member/check-email", { params: { email } });
  return response.data;
};

// 휴대폰 중복확인
export const checkPhone = async (phone) => {
  console.log("휴대폰 중복확인 요청", phone);
  const response = await api.get("/member/check-phone", { params: { phone } });
  return response.data;
};

// 이메일 인증코드 발송
export const sendEmailCode = async (email) => {
  console.log("이메일 인증코드 발송 요청", email);

  const response = await api.post("/member/email-code/send", new URLSearchParams({ email }), {
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
  });

  return response.data;
};

// 이메일 인증
export const verifyEmailCode = async (email, code) => {
  console.log("이메일 인증 요청", email);

  const response = await api.post("/member/email-code/verify", new URLSearchParams({ email, code }), {
    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
  });

  return response.data;
};

// 회원정보 조회
export const info = () => {
  console.log("회원정보 조회 요청");
  return api.get("/auth/me");
};

// 회원정보 수정
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