import api from "./api";

// 지도용 충전소 목록 조회
export const mapData = () => {
  console.log("충전소 지도 데이터 요청");
  return api.get("/station/map-data");
};

// 충전소 상세 조회
export const detail = (stationId) => {
  console.log("충전소 상세 요청", stationId);
  return api.get(`/station/detail/${stationId}`);
};

// 충전소 충전기 목록 조회
export const chargers = (stationId) => {
  console.log("충전기 목록 요청", stationId);
  return api.get("/station/chargers", {
    params: { stationId },
  });
};

// 출발지 목록 조회
export const savedLocations = () => {
  console.log("출발지 목록 요청");
  return api.get("/location/list");
};

// 출발지 등록
export const addLocation = (locationData) => {
  console.log("출발지 등록 요청", locationData);
  return api.post("/location/add", locationData);
};

// 기본 출발지 설정
export const setDefaultLocation = (locationId) => {
  console.log("기본 출발지 설정 요청", locationId);
  return api.post("/location/default", { locationId });
};

// 출발지 삭제
export const deleteLocation = (locationId) => {
  console.log("출발지 삭제 요청", locationId);
  return api.post("/location/delete", { locationId });
};