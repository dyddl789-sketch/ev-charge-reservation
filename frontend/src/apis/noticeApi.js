import api from "./api";

// 메인 새소식
export const main = () => {
  console.log("메인 공지 조회");
  return api.get("/notice/main");
};
// 공지사항 목록
export const list = (params = {}) => {
  console.log("공지사항 목록 요청", params);
  return api.get("/notice/list", { params });
};

// 공지사항 상세
export const read = (noticeId) => {
  console.log("공지사항 상세 요청", noticeId);
  return api.get(`/notice/${noticeId}`);
};

// 공지사항 검색
export const search = (keyword) => {
  console.log("공지사항 검색 요청", keyword);
  return api.get("/notice/search", {
    params: { keyword },
  });
};
