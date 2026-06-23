import api from "./api";

// 내 차량 목록
export const list = () => {
  console.log("vehicle list 요청");
  return api.get("/vehicle/api/list");
};

// 차량 모델 목록
export const modelList = () => {
  console.log("vehicle modelList 요청");
  return api.get("/vehicle/api/models");
};

// 차량 등록
export const create = (data) => {
  console.log("vehicle create 요청", data);
  return api.post("/vehicle/api/register", data);
};

// 기본 차량 설정
export const setDefault = (vehicleId) => {
  console.log("vehicle setDefault 요청", vehicleId);
  return api.post("/vehicle/api/default", new URLSearchParams({ vehicleId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};

// 차량 삭제
export const remove = (vehicleId) => {
  console.log("vehicle remove 요청", vehicleId);
  return api.post("/vehicle/api/delete", new URLSearchParams({ vehicleId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};
