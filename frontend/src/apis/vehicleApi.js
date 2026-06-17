import api from "./api";

// 차량 목록
export const list = () =>
  api.get(`/vehicle/list`);

// 차량 상세
export const read = (vehicleId) =>
  api.get(`/vehicle/${vehicleId}`);

// 차량 등록
export const create = (data) =>
  api.post(`/vehicle/register`, data);

// 차량 수정
export const update = (vehicleId, data) =>
  api.put(`/vehicle/${vehicleId}`, data);

// 차량 삭제
export const remove = (vehicleId) =>
  api.delete(`/vehicle/${vehicleId}`);

// 기본 차량 설정
export const setDefault = (vehicleId) =>
  api.patch(`/vehicle/${vehicleId}/default`);

// 차량 모델 목록
export const modelList = () =>
  api.get(`/vehicle/models`);