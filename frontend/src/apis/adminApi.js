import api from "./api";

// 대시보드
export const dashboard = () =>
  api.get(`/admin/dashboard`);

// 회원 목록
export const members = () =>
  api.get(`/admin/members`);

// 회원 상세
export const member = (memberId) =>
  api.get(`/admin/members/${memberId}`);

// 충전소 목록
export const stations = () =>
  api.get(`/admin/stations`);

// 예약 목록
export const reservations = () =>
  api.get(`/admin/reservations`);

// 민원 목록
export const complaints = () =>
  api.get(`/admin/complaints`);

// 장애 목록
export const faults = () =>
  api.get(`/admin/faults`);

// 점검 목록
export const inspections = () =>
  api.get(`/admin/inspections`);

// 결재 목록
export const approvals = () =>
  api.get(`/admin/approvals`);

// 이용 통계
export const usageStatistics = () =>
  api.get(`/admin/statistics/usage`);

// 매출 통계
export const salesStatistics = () =>
  api.get(`/admin/statistics/sales`);