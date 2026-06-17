import api from "./api";

// 내 민원 목록
export const myList = () =>
  api.get(`/complaint/my`);

// 민원 목록
export const list = () =>
  api.get(`/complaint/list`);

// 민원 상세
export const read = (complaintId) =>
  api.get(`/complaint/${complaintId}`);

// 민원 등록
export const create = (data) =>
  api.post(`/complaint/create`, data);

// 민원 수정
export const update = (complaintId, data) =>
  api.put(`/complaint/${complaintId}`, data);