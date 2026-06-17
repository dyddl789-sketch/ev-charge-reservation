import api from "./api";

// 충전소 목록
export const list = () =>
  api.get(`/station/list`);

// 충전소 상세
export const read = (stationId) =>
  api.get(`/station/${stationId}`);

// 지도용 충전소 목록
export const mapList = () =>
  api.get(`/station/map`);

// 충전소 검색
export const search = (keyword) =>
  api.get(`/station/search?keyword=${keyword}`);

// 충전기 목록
export const chargerList = (stationId) =>
  api.get(`/station/${stationId}/chargers`);