import axios from "axios";

// axios 공통 객체 생성
const api = axios.create();

// 기본 URL 설정
api.defaults.baseURL = "/api";

// 요청 전 JWT 자동 추가
api.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    console.log("API 요청", config.url);

    return config;
  },
  (error) => {
    console.log("API 요청 오류", error);
    return Promise.reject(error);
  }
);

export default api;