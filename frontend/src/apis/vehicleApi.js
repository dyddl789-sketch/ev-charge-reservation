import api from "./api";

// 내 차량 목록(JSON REST 필요: GET /vehicle/api/list)
export const list = () => {
  console.log("vehicle list 요청");
  return api.get("/vehicle/api/list");
};

// 차량 모델 목록(JSON REST 필요: GET /vehicle/api/models)
export const modelList = () => {
  console.log("vehicle modelList 요청");
  return api.get("/vehicle/api/models");
};

// 차량 등록(기존 백엔드 POST /vehicle/register DTO 필드명에 맞춤)
export const create = (data) => {
  console.log("vehicle create 요청", data);
  return api.post("/vehicle/register", new URLSearchParams(data), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};

// 기본 차량 설정(기존 백엔드 POST /vehicle/default)
export const setDefault = (vehicleId) => {
  console.log("vehicle setDefault 요청", vehicleId);
  return api.post("/vehicle/default", new URLSearchParams({ vehicleId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};

// 차량 삭제(기존 백엔드 POST /vehicle/delete)
export const remove = (vehicleId) => {
  console.log("vehicle remove 요청", vehicleId);
  return api.post("/vehicle/delete", new URLSearchParams({ vehicleId }), {
    headers: {
      "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
    },
  });
};
