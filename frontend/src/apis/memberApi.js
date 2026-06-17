import api from "./api";

// 회원가입
export const join = (data) =>
  api.post(`/member/join`, data);

// 아이디 중복확인
export const checkUserId = (userId) =>
  api.get(`/member/check-user-id?userId=${userId}`);

// 닉네임 중복확인
export const checkNickname = (nickname) =>
  api.get(`/member/check-nickname?nickname=${nickname}`);

// 이메일 중복확인
export const checkEmail = (email) =>
  api.get(`/member/check-email?email=${email}`);

// 휴대폰 중복확인
export const checkPhone = (phone) =>
  api.get(`/member/check-phone?phone=${phone}`);

// 이메일 인증코드 발송
export const sendEmailCode = (email) =>
  api.post(`/member/email-code/send`, { email });

// 이메일 인증
export const verifyEmailCode = (email, code) =>
  api.post(`/member/email-code/verify`, {
    email,
    code,
  });

// 회원정보 조회
export const info = () =>
  api.get(`/member/me`);

// 회원정보 수정
export const update = (data) =>
  api.put(`/member/edit`, data);

// 회원탈퇴
export const remove = (memberId) =>
  api.delete(`/member/${memberId}`);