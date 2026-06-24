import api from "./api";

// 충전소 목록
export const list = (keyword = "") => {
  console.log("충전소 목록 요청", keyword);
  return api.get("/station/api/list", {
    params: { keyword },
  });
};

// 지도용 충전소 목록
// 사용 예
// mapData("부산")
// mapData({ keyword: "부산", latitude: 35.1, longitude: 129.0, limit: 10 })
export const mapData = (options = "") => {
  const params =
    typeof options === "string"
      ? { keyword: options }
      : {
          keyword: options.keyword || "",
          latitude: options.latitude ?? undefined,
          longitude: options.longitude ?? undefined,
          limit: options.limit ?? undefined,
          connectorType: options.connectorType || undefined,
        };

  console.log("충전소 지도 데이터 요청", params);

  return api.get("/station/map-data", {
    params,
  });
};

// 충전소 상세
export const detail = (stationId) => {
  console.log("충전소 상세 요청", stationId);
  return api.get(`/station/api/detail/${stationId}`);
};

// 충전기 목록
export const chargerList = (stationId) => {
  console.log("충전기 목록 요청", stationId);
  return api.get("/station/chargers", {
    params: { stationId },
  });
};

// 출발지 목록
export const savedLocations = () => {
  console.log("출발지 목록 요청");
  return api.get("/station/saved-locations");
};

// 출발지 등록
export const createSavedLocation = ({ locationName, locationType = "CUSTOM", address, isDefault = false }) => {
  console.log("출발지 등록 요청", { locationName, locationType, address, isDefault });

  return api.post(
    "/station/saved-locations",
    new URLSearchParams({
      locationName,
      locationType,
      address,
      isDefault: String(isDefault),
    }),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
    }
  );
};

// 기본 출발지 설정
export const setDefaultLocation = (locationId) => {
  console.log("기본 출발지 설정 요청", locationId);

  return api.post(
    "/station/saved-locations/default",
    new URLSearchParams({ locationId }),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
    }
  );
};

// 출발지 삭제
export const deleteSavedLocation = (locationId) => {
  console.log("출발지 삭제 요청", locationId);

  return api.post(
    "/station/saved-locations/delete",
    new URLSearchParams({ locationId }),
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
      },
    }
  );
};
