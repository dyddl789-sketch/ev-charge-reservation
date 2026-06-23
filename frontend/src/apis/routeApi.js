import api from "./api";

// Kakao Mobility 기반 길찾기 시뮬레이션
export const simulation = ({ startLat, startLng, endLat, endLng }) => {
  console.log("route simulation 요청", { startLat, startLng, endLat, endLng });
  return api.get("/route/simulation", {
    params: { startLat, startLng, endLat, endLng },
  });
};
