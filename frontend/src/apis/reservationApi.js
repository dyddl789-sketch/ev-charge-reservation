import api from "./api";

// 예약 목록
export const list = () =>
  api.get(`/reservation/list`);

// 예약 상세
export const read = (reservationId) =>
  api.get(`/reservation/${reservationId}`);

// 예약 생성
export const create = (data) =>
  api.post(`/reservation/create`, data);

// 예약 취소
export const cancel = (reservationId) =>
  api.patch(`/reservation/${reservationId}/cancel`);

// 예약 인증
export const verify = (reservationId, authCode) =>
  api.post(`/reservation/${reservationId}/verify`, {
    authCode,
  });

// 충전 이력
export const history = () =>
  api.get(`/reservation/history`);