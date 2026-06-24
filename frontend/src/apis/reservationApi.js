import api from "./api";

// 예약 폼 데이터
export const formData = (chargerId) => {
  console.log("reservation formData 요청", chargerId);
  return api.get("/reservation/api/form", {
    params: { chargerId },
  });
};

// 충전소 기준 예약 폼 데이터
export const formDataByStation = (stationId) => {
  console.log("reservation formDataByStation 요청", stationId);
  return api.get("/reservation/api/form/station", {
    params: { stationId },
  });
};

// 예약 등록
export const create = (data) => {
  console.log("reservation create 요청", data);
  return api.post("/reservation/api/register", data);
};

// 예약 완료 데이터
export const complete = (reservationId) => {
  console.log("reservation complete 요청", reservationId);
  return api.get("/reservation/api/complete", {
    params: { reservationId },
  });
};

// 충전기 임시 점유 변경
export const changeLock = (oldChargerId, newChargerId) => {
  console.log("reservation changeLock 요청", oldChargerId, newChargerId);
  return api.post(
    "/reservation/lock/change",
    new URLSearchParams({ oldChargerId, newChargerId }),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
    }
  );
};

// 충전기 임시 점유 유지
export const keepAliveLock = (chargerId) => {
  console.log("reservation keepAliveLock 요청", chargerId);
  return api.post("/reservation/lock/keep-alive", new URLSearchParams({ chargerId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};

// 충전기 임시 점유 해제
export const releaseLock = (chargerId) => {
  console.log("reservation releaseLock 요청", chargerId);
  return api.post("/reservation/lock/release", new URLSearchParams({ chargerId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};

// 선택 시간 기준 충전기 상태 조회
export const chargerStatus = ({ stationId, reservationDate, startTime, estimatedMinutes }) => {
  console.log("reservation chargerStatus 요청", {
    stationId,
    reservationDate,
    startTime,
    estimatedMinutes,
  });

  return api.get("/reservation/charger-status", {
    params: {
      stationId,
      reservationDate,
      startTime,
      estimatedMinutes,
    },
  });
};

// 내 예약 목록 조회
export const myList = (month = "") => {
  console.log("reservation myList 요청", month);
  return api.get("/reservation/api/my", {
    params: month ? { month } : {},
  });
};

// 내 예약 상세 조회
export const myDetail = (reservationId) => {
  console.log("reservation myDetail 요청", reservationId);
  return api.get(`/reservation/api/my/${reservationId}`);
};

// 충전 이용 내역 조회
export const historyList = (month = "") => {
  console.log("reservation historyList 요청", month);
  return api.get("/reservation/api/history", {
    params: month ? { month } : {},
  });
};

// 예약 취소
export const cancel = (reservationId) => {
  console.log("reservation cancel 요청", reservationId);
  return api.post("/reservation/api/cancel", { reservationId });
};

// 예약 인증코드 발급
export const issueAuthCode = (reservationId) => {
  console.log("reservation issueAuthCode 요청", reservationId);
  return api.post("/reservation/api/auth-code/issue", { reservationId });
};

// 예약 인증 처리
export const verify = (reservationId, authCode) => {
  console.log("reservation verify 요청", reservationId);
  return api.post("/reservation/api/verify", { reservationId, authCode });
};

// 충전 영수증 이메일 발송
export const sendReceiptEmail = (reservationId) => {
  console.log("reservation sendReceiptEmail 요청", reservationId);
  return api.post("/reservation/api/receipt/email", { reservationId });
};

// 충전 시작 시뮬레이션 완료 처리
export const completeChargingSimulation = (reservationId) => {
  console.log("reservation completeChargingSimulation 요청", reservationId);
  return api.post("/reservation/api/charging/simulation/complete", { reservationId });
};
