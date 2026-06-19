import api from "./api";

// 로그인
export const login = (loginData) => {
  console.log("login 요청", loginData);

  // 백엔드 연결 후 사용
  return api.post("/auth/login", loginData);
};

// 로그아웃
export const logout = () => {
  console.log("logout 요청");

  return api.post("/auth/logout");
};

// 내 로그인 정보 조회
export const getMyInfo = () => {
  console.log("getMyInfo 요청");

  return api.get("/auth/me");
};

// 토큰 재발급
export const refreshToken = () => {
  console.log("refreshToken 요청");

  return api.post("/auth/refresh");
};