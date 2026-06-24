import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import * as stations from "../../apis/stationApi";
import * as routeApi from "../../apis/routeApi";
import "../../styles/station-map.css";

const KAKAO_MAP_SCRIPT_ID = "kakao-map-sdk";
const DAUM_POSTCODE_SCRIPT_ID = "daum-postcode-sdk";

const CONNECTOR_TYPE_OPTIONS = [
  { label: "전체", value: "" },
  { label: "DC콤보", value: "DC콤보" },
  { label: "AC3상", value: "AC3상" },
  { label: "NACS", value: "NACS" },
];

const MAP_FOCUS_LEVEL = 5;
const NEARBY_STATION_LIMIT = 10;

const loadKakaoMapScript = () => {
  const kakaoJavascriptKey = import.meta.env.VITE_KAKAO_JAVASCRIPT_KEY;

  if (!kakaoJavascriptKey) {
    return Promise.reject(
      new Error("VITE_KAKAO_JAVASCRIPT_KEY가 설정되어 있지 않습니다.")
    );
  }

  if (window.kakao && window.kakao.maps) {
    return new Promise((resolve) => {
      window.kakao.maps.load(() => resolve(window.kakao));
    });
  }

  const existingScript = document.getElementById(KAKAO_MAP_SCRIPT_ID);

  if (existingScript) {
    return new Promise((resolve, reject) => {
      existingScript.addEventListener("load", () => {
        window.kakao.maps.load(() => resolve(window.kakao));
      });
      existingScript.addEventListener("error", reject);
    });
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.id = KAKAO_MAP_SCRIPT_ID;
    script.async = true;
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoJavascriptKey}&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => resolve(window.kakao));
    };

    script.onerror = () => {
      reject(new Error("Kakao 지도 스크립트 로드 실패"));
    };

    document.head.appendChild(script);
  });
};

const loadDaumPostcodeScript = () => {
  if (window.daum && window.daum.Postcode) {
    return Promise.resolve(window.daum);
  }

  const existingScript = document.getElementById(DAUM_POSTCODE_SCRIPT_ID);

  if (existingScript) {
    return new Promise((resolve, reject) => {
      existingScript.addEventListener("load", () => resolve(window.daum));
      existingScript.addEventListener("error", reject);
    });
  }

  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.id = DAUM_POSTCODE_SCRIPT_ID;
    script.async = true;
    script.src = "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";

    script.onload = () => resolve(window.daum);
    script.onerror = () => reject(new Error("Daum 주소검색 스크립트 로드 실패"));

    document.head.appendChild(script);
  });
};

const toNumber = (value) => {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  const numberValue = Number(value);

  if (Number.isNaN(numberValue)) {
    return null;
  }

  return numberValue;
};

const calculateDistanceKm = (origin, station) => {
  if (!origin?.latitude || !origin?.longitude || !station?.latitude || !station?.longitude) {
    return null;
  }

  const toRad = (degree) => (degree * Math.PI) / 180;
  const earthRadiusKm = 6371;
  const dLat = toRad(station.latitude - origin.latitude);
  const dLng = toRad(station.longitude - origin.longitude);
  const lat1 = toRad(origin.latitude);
  const lat2 = toRad(station.latitude);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1) * Math.cos(lat2) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(earthRadiusKm * c * 10) / 10;
};

const normalizeStation = (station) => {
  const latitude = toNumber(station.latitude);
  const longitude = toNumber(station.longitude);
  const distanceKm = toNumber(station.distanceKm);

  return {
    ...station,
    latitude,
    longitude,
    distanceKm,
    stationId: station.stationId,
    stationName: station.stationName || "충전소명 없음",
    address: station.address || "주소 없음",
    operatorName: station.operatorName || "-",
    status: station.stationStatus || station.status || "운영중",
    stationStatus: station.stationStatus || station.status || "운영중",
    chargerCount: station.chargerCount ?? 0,
    availableCount: station.availableChargerCount ?? station.availableCount ?? 0,
    availableChargerCount: station.availableChargerCount ?? station.availableCount ?? 0,
    parkingAvailable: station.parkingFree === "Y" || station.parkingAvailable || false,
  };
};

const normalizeLocation = (location) => ({
  ...location,
  latitude: toNumber(location.latitude),
  longitude: toNumber(location.longitude),
  isDefault: location.isDefault === true || location.isDefault === "true" || location.isDefault === "Y",
});

const sortLocationList = (locations, selectedLocationId = null) => {
  const selectedKey = selectedLocationId !== null && selectedLocationId !== undefined
    ? String(selectedLocationId)
    : null;

  return [...locations].sort((a, b) => {
    const aSelected = selectedKey && String(a.locationId) === selectedKey;
    const bSelected = selectedKey && String(b.locationId) === selectedKey;

    if (aSelected !== bSelected) {
      return aSelected ? -1 : 1;
    }

    if (a.isDefault !== b.isDefault) {
      return a.isDefault ? -1 : 1;
    }

    const aId = Number(a.locationId);
    const bId = Number(b.locationId);

    if (!Number.isNaN(aId) && !Number.isNaN(bId)) {
      return aId - bId;
    }

    return String(a.locationName || "").localeCompare(String(b.locationName || ""), "ko-KR");
  });
};

const StationMapPage = () => {
  console.log("StationMapPage 렌더링");

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const mapElementRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const markerListRef = useRef([]);
  const infoWindowRef = useRef(null);
  const originMarkerRef = useRef(null);
  const routeLineRef = useRef(null);

  const [keyword, setKeyword] = useState("");
  const [connectorType, setConnectorType] = useState("");
  const [stationList, setStationList] = useState([]);
  const [selectedStation, setSelectedStation] = useState(null);
  const [locationList, setLocationList] = useState([]);
  const [currentOrigin, setCurrentOrigin] = useState(null);
  const [routeInfo, setRouteInfo] = useState(null);
  const [isRouteLoading, setIsRouteLoading] = useState(false);
  const [isCurrentLocationLoading, setIsCurrentLocationLoading] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [useKakaoMap, setUseKakaoMap] = useState(true);
  const [mapMessage, setMapMessage] = useState("");
  const [isLocationFormOpen, setIsLocationFormOpen] = useState(false);
  const [locationForm, setLocationForm] = useState({
    locationName: "",
    address: "",
    isDefault: true,
  });

  const mockLocations = [
    {
      locationId: "guide",
      locationName: "출발지 안내",
      address: "주소검색 또는 내 위치 버튼으로 출발지를 설정하면 주변 충전소 10곳을 볼 수 있습니다.",
      isDefault: true,
      latitude: null,
      longitude: null,
    },
  ];

  const getQueryMapFocus = () => {
    const focusType = searchParams.get("focusType") || "";
    const stationId = searchParams.get("stationId");
    const latitude = toNumber(searchParams.get("lat"));
    const longitude = toNumber(searchParams.get("lng"));
    const name = searchParams.get("name") || "";
    const address = searchParams.get("address") || "";

    if (!focusType && !stationId && latitude === null && longitude === null) {
      return null;
    }

    return {
      focusType: focusType || (stationId ? "station" : "origin"),
      stationId,
      latitude,
      longitude,
      name,
      address,
    };
  };

  const validStationList = useMemo(
    () =>
      stationList.filter(
        (station) => station.latitude !== null && station.longitude !== null
      ),
    [stationList]
  );

  const nearbyStationList = useMemo(() => {
    if (!currentOrigin?.latitude || !currentOrigin?.longitude) {
      return stationList.slice(0, NEARBY_STATION_LIMIT);
    }

    return stationList
      .map((station) => ({
        ...station,
        distanceKm:
          station.distanceKm ?? calculateDistanceKm(currentOrigin, station),
      }))
      .sort((a, b) => (a.distanceKm ?? 999999) - (b.distanceKm ?? 999999))
      .slice(0, NEARBY_STATION_LIMIT);
  }, [stationList, currentOrigin]);

  useEffect(() => {
    initStationMapPage();
  }, []);

  useEffect(() => {
    drawKakaoMap();
  }, [validStationList, currentOrigin]);

  const initStationMapPage = async () => {
    const queryFocus = getQueryMapFocus();
    const savedLocations = await getSavedLocations();

    // AI 답변의 “지도에서 내 위치 보기”로 넘어온 경우에는 DB 기본 출발지 좌표를 지도 중심으로 사용한다.
    if (queryFocus?.focusType === "origin" && queryFocus.latitude !== null && queryFocus.longitude !== null) {
      const queryOrigin = {
        locationId: "ai-default-location",
        locationName: queryFocus.name || "AI 기본 출발지",
        address: queryFocus.address || "AI 답변에서 이동한 위치",
        latitude: queryFocus.latitude,
        longitude: queryFocus.longitude,
        isDefault: true,
        isAiFocus: true,
      };

      setCurrentOrigin(queryOrigin);
      await getStationMapData("", queryOrigin, connectorType, queryFocus);
      return;
    }

    const defaultLocation = savedLocations.find(
      (location) => location.isDefault && location.latitude && location.longitude
    );

    // 저장한 출발지가 있으면 그 위치를 우선 사용한다.
    // 저장한 출발지가 없을 때만 브라우저 현재 위치를 기본값으로 사용한다.
    if (defaultLocation) {
      setCurrentOrigin(defaultLocation);
      await getStationMapData("", defaultLocation, connectorType, queryFocus);
      return;
    }

    const browserOrigin = await getBrowserCurrentOrigin(false);

    if (browserOrigin) {
      setCurrentOrigin(browserOrigin);
      await getStationMapData("", browserOrigin, connectorType, queryFocus);
      return;
    }

    await getStationMapData("", currentOrigin, connectorType, queryFocus);
  };

  const getStationMapData = async (searchKeyword = "", origin = currentOrigin, selectedConnectorType = connectorType, focusOptions = null) => {
    console.log("지도 충전소 데이터 조회 실행", searchKeyword, origin, selectedConnectorType);

    setIsLoading(true);

    try {
      const params = {
        keyword: searchKeyword,
        connectorType: selectedConnectorType || "",
      };

      // 지도 마커는 타입 필터가 적용된 전체 충전소를 표시한다.
      // 주변 충전소 목록 10개 제한은 프론트에서 거리순으로 따로 처리한다.
      const response = await stations.mapData(params);
      const data = Array.isArray(response.data) ? response.data : [];
      let normalizedData = data.map(normalizeStation);

      if (origin?.latitude && origin?.longitude) {
        normalizedData = normalizedData
          .map((station) => ({
            ...station,
            distanceKm:
              station.distanceKm ?? calculateDistanceKm(origin, station),
          }))
          .sort((a, b) => (a.distanceKm ?? 999999) - (b.distanceKm ?? 999999));
      }

      console.log("지도 충전소 데이터 응답", normalizedData);

      const focusStationId = focusOptions?.stationId || searchParams.get("stationId");
      const focusStation = focusStationId
        ? normalizedData.find((item) => String(item.stationId) === String(focusStationId))
        : null;
      const nextSelectedStation = focusStation || normalizedData[0] || null;

      setStationList(normalizedData);
      setSelectedStation((prev) => {
        if (focusStation) {
          return focusStation;
        }

        if (!prev) {
          return nextSelectedStation;
        }

        return (
          normalizedData.find((item) => item.stationId === prev.stationId) ||
          nextSelectedStation
        );
      });
    } catch (error) {
      console.log("지도 충전소 데이터 조회 실패", error);
      setStationList([]);
      setSelectedStation(null);
      setMapMessage("충전소 데이터를 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const getSavedLocations = async () => {
    console.log("출발지 목록 조회 실행");

    try {
      const response = await stations.savedLocations();
      const data = Array.isArray(response.data) ? response.data.map(normalizeLocation) : [];
      const displayData = data.length > 0
        ? sortLocationList(data, currentOrigin?.locationId)
        : mockLocations;

      console.log("출발지 목록 응답", displayData);

      setLocationList(displayData);
      return data;
    } catch (error) {
      console.log("출발지 목록 조회 실패", error);
      setLocationList(mockLocations);
      return [];
    }
  };

  const clearRouteLine = () => {
    if (routeLineRef.current) {
      routeLineRef.current.setMap(null);
      routeLineRef.current = null;
    }

    setRouteInfo(null);
  };

  const moveMapTo = (latitude, longitude, level = MAP_FOCUS_LEVEL) => {
    const lat = toNumber(latitude);
    const lng = toNumber(longitude);

    if (lat === null || lng === null || !mapInstanceRef.current || !window.kakao?.maps) {
      return;
    }

    const movePosition = new window.kakao.maps.LatLng(lat, lng);
    const map = mapInstanceRef.current;

    // 가까운 거리의 충전소를 클릭하면 panTo 애니메이션만으로는
    // 화면이 이동하지 않은 것처럼 보일 수 있다.
    // 그래서 카드/마커 클릭 이동은 중심 좌표와 확대 레벨을 강제로 맞춘다.
    map.setLevel(level);
    map.setCenter(movePosition);

    if (typeof map.relayout === "function") {
      map.relayout();
    }
  };

  const getBrowserCurrentOrigin = (showAlert = true) => {
    if (!navigator.geolocation) {
      if (showAlert) {
        alert("현재 브라우저에서 현재 위치 기능을 사용할 수 없습니다.");
      }
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const accuracy = position.coords.accuracy
            ? Math.round(position.coords.accuracy)
            : null;

          resolve({
            locationId: `current-location-${Date.now()}`,
            locationName: "현재 위치",
            address: accuracy
              ? `브라우저에서 가져온 현재 위치 · 오차 약 ${accuracy}m`
              : "브라우저에서 가져온 현재 위치",
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            isDefault: false,
            isCurrent: true,
            accuracy,
          });
        },
        (error) => {
          console.log("현재 위치 조회 실패", error);
          if (showAlert) {
            alert("현재 위치를 가져오지 못했습니다. 브라우저 위치 권한을 허용해 주세요.");
          }
          resolve(null);
        },
        {
          enableHighAccuracy: true,
          timeout: 8000,
          maximumAge: 0,
        }
      );
    });
  };

  const drawOriginMarker = async () => {
    if (!mapInstanceRef.current || !currentOrigin?.latitude || !currentOrigin?.longitude) {
      if (originMarkerRef.current) {
        originMarkerRef.current.setMap(null);
        originMarkerRef.current = null;
      }
      return;
    }

    const kakao = await loadKakaoMapScript();
    const map = mapInstanceRef.current;

    if (originMarkerRef.current) {
      originMarkerRef.current.setMap(null);
      originMarkerRef.current = null;
    }

    const originPosition = new kakao.maps.LatLng(currentOrigin.latitude, currentOrigin.longitude);
    const markerTitle = currentOrigin.isCurrent ? "내 위치" : "출발지";
    const accuracyText = currentOrigin.accuracy ? `<span>오차 약 ${currentOrigin.accuracy}m</span>` : "";

    originMarkerRef.current = new kakao.maps.CustomOverlay({
      map,
      position: originPosition,
      yAnchor: 1,
      zIndex: 20,
      content: `
        <div class="origin-marker-wrap">
          <div class="origin-marker-pulse ${currentOrigin.isCurrent ? "current" : ""}"></div>
          <div class="origin-marker-label">
            <strong>${markerTitle}</strong>
            <span>${currentOrigin.locationName || markerTitle}</span>
            ${accuracyText}
          </div>
        </div>
      `,
    });
  };

  const drawKakaoMap = async () => {
    console.log("drawKakaoMap 실행", validStationList, currentOrigin);

    if (!mapElementRef.current) {
      return;
    }

    if (validStationList.length === 0 && !currentOrigin) {
      setMapMessage("지도에 표시할 충전소가 없습니다.");
      return;
    }

    try {
      const kakao = await loadKakaoMapScript();
      const firstStation = validStationList[0];
      const center = currentOrigin?.latitude && currentOrigin?.longitude
        ? new kakao.maps.LatLng(currentOrigin.latitude, currentOrigin.longitude)
        : new kakao.maps.LatLng(firstStation.latitude, firstStation.longitude);

      if (!mapInstanceRef.current) {
        mapInstanceRef.current = new kakao.maps.Map(mapElementRef.current, {
          center,
          level: MAP_FOCUS_LEVEL,
        });
      }

      const map = mapInstanceRef.current;

      markerListRef.current.forEach((marker) => marker.setMap(null));
      markerListRef.current = [];

      const bounds = new kakao.maps.LatLngBounds();

      if (currentOrigin?.latitude && currentOrigin?.longitude) {
        bounds.extend(new kakao.maps.LatLng(currentOrigin.latitude, currentOrigin.longitude));
      }

      validStationList.forEach((station) => {
        const position = new kakao.maps.LatLng(
          station.latitude,
          station.longitude
        );

        const marker = new kakao.maps.Marker({
          map,
          position,
          title: station.stationName,
        });

        kakao.maps.event.addListener(marker, "click", () => {
          console.log("카카오 지도 마커 클릭", station);
          setSelectedStation(station);
          clearRouteLine();
          map.setLevel(MAP_FOCUS_LEVEL);
          map.setCenter(position);

          if (typeof map.relayout === "function") {
            map.relayout();
          }

          if (infoWindowRef.current) {
            infoWindowRef.current.close();
          }

          infoWindowRef.current = new kakao.maps.InfoWindow({
            content: `
              <div style="padding:10px 12px;font-size:13px;line-height:1.4;">
                <strong>${station.stationName}</strong><br />
                <span>${station.address}</span><br />
                <span>사용가능 ${station.availableChargerCount} / 전체 ${station.chargerCount}</span>
                ${station.distanceKm ? `<br /><span>거리 ${station.distanceKm}km</span>` : ""}
              </div>
            `,
          });

          infoWindowRef.current.open(map, marker);
        });

        markerListRef.current.push(marker);
        bounds.extend(position);
      });

      await drawOriginMarker();

      const queryFocus = getQueryMapFocus();
      if (queryFocus?.focusType === "station") {
        const focusStation = queryFocus.stationId
          ? validStationList.find((station) => String(station.stationId) === String(queryFocus.stationId))
          : null;
        const focusLat = focusStation?.latitude ?? queryFocus.latitude;
        const focusLng = focusStation?.longitude ?? queryFocus.longitude;

        if (focusStation) {
          setSelectedStation(focusStation);
        }

        if (focusLat !== null && focusLng !== null) {
          map.setLevel(MAP_FOCUS_LEVEL);
          map.setCenter(new kakao.maps.LatLng(focusLat, focusLng));
          setMapMessage(queryFocus.name ? `${queryFocus.name} 위치를 지도에서 표시했습니다.` : "선택한 충전소 위치를 지도에서 표시했습니다.");
          setUseKakaoMap(true);
          return;
        }
      }

      if (queryFocus?.focusType === "origin" && queryFocus.latitude !== null && queryFocus.longitude !== null) {
        map.setCenter(new kakao.maps.LatLng(queryFocus.latitude, queryFocus.longitude));
        map.setLevel(MAP_FOCUS_LEVEL);
        setUseKakaoMap(true);
        setMapMessage(queryFocus.name ? `${queryFocus.name} 기준 위치를 지도에서 표시했습니다.` : "AI 기본 출발지 위치를 지도에서 표시했습니다.");
        return;
      }

      if (currentOrigin?.latitude && currentOrigin?.longitude) {
        map.setCenter(center);
        map.setLevel(MAP_FOCUS_LEVEL);
      } else if (validStationList.length === 1) {
        map.setCenter(center);
        map.setLevel(MAP_FOCUS_LEVEL);
      } else if (validStationList.length > 1) {
        map.setBounds(bounds);
      }

      setUseKakaoMap(true);
      setMapMessage("");
    } catch (error) {
      console.log("Kakao 지도 로드 실패 - 대체 지도 사용", error);
      setUseKakaoMap(false);
      setMapMessage(
        "Kakao JavaScript 키가 없거나 지도 스크립트를 불러오지 못했습니다. 목록은 정상 조회됩니다."
      );
    }
  };

  const drawRouteLine = async (route, origin, destination) => {
    if (!mapInstanceRef.current || !window.kakao?.maps) {
      return;
    }

    const kakao = window.kakao;
    const map = mapInstanceRef.current;

    if (routeLineRef.current) {
      routeLineRef.current.setMap(null);
    }

    const path = Array.isArray(route?.path)
      ? route.path
          .filter((point) => point.lat && point.lng)
          .map((point) => new kakao.maps.LatLng(point.lat, point.lng))
      : [];

    if (path.length === 0) {
      const fallbackPath = [
        new kakao.maps.LatLng(origin.latitude, origin.longitude),
        new kakao.maps.LatLng(destination.latitude, destination.longitude),
      ];

      routeLineRef.current = new kakao.maps.Polyline({
        path: fallbackPath,
        strokeWeight: 5,
        strokeColor: "#2563eb",
        strokeOpacity: 0.85,
        strokeStyle: "dashed",
      });
    } else {
      routeLineRef.current = new kakao.maps.Polyline({
        path,
        strokeWeight: 6,
        strokeColor: "#f05a00",
        strokeOpacity: 0.9,
        strokeStyle: "solid",
      });
    }

    routeLineRef.current.setMap(map);

    const bounds = new kakao.maps.LatLngBounds();
    bounds.extend(new kakao.maps.LatLng(origin.latitude, origin.longitude));
    bounds.extend(new kakao.maps.LatLng(destination.latitude, destination.longitude));
    path.forEach((point) => bounds.extend(point));
    map.setBounds(bounds);
  };

  const searchStation = () => {
    console.log("충전소 검색 실행", keyword);
    clearRouteLine();
    getStationMapData(keyword.trim(), currentOrigin, connectorType);
  };

  const changeConnectorType = (e) => {
    const nextConnectorType = e.target.value;
    console.log("커넥터 타입 변경", nextConnectorType);

    setConnectorType(nextConnectorType);
    clearRouteLine();
    getStationMapData(keyword.trim(), currentOrigin, nextConnectorType);
  };

  const selectStation = (station) => {
    console.log("충전소 선택", station);

    if (!station?.latitude || !station?.longitude) {
      setSelectedStation(station);
      alert("좌표가 없는 충전소입니다.");
      return;
    }

    setSelectedStation(station);
    clearRouteLine();
    moveMapTo(station.latitude, station.longitude, MAP_FOCUS_LEVEL);
  };

  const moveReservation = () => {
    console.log("예약하기 클릭", selectedStation);

    if (!selectedStation) {
      alert("충전소를 선택해 주세요.");
      return;
    }

    navigate(`/reservation?stationId=${selectedStation.stationId}`);
  };

  const moveDetail = () => {
    console.log("상세보기 클릭", selectedStation);

    if (!selectedStation) {
      alert("충전소를 선택해 주세요.");
      return;
    }

    navigate(`/stations/${selectedStation.stationId}`);
  };

  const openRoute = async () => {
    console.log("길찾기 클릭", selectedStation, currentOrigin);

    if (!selectedStation) {
      alert("충전소를 선택해 주세요.");
      return;
    }

    if (!currentOrigin?.latitude || !currentOrigin?.longitude) {
      alert("출발지를 먼저 선택하거나 현재 위치 버튼을 눌러 주세요.");
      return;
    }

    setIsRouteLoading(true);

    try {
      const response = await routeApi.simulation({
        startLat: currentOrigin.latitude,
        startLng: currentOrigin.longitude,
        endLat: selectedStation.latitude,
        endLng: selectedStation.longitude,
      });

      const route = response.data || {};
      setRouteInfo({
        ...route,
        originName: currentOrigin.locationName || "출발지",
        destinationName: selectedStation.stationName,
      });

      await drawRouteLine(route, currentOrigin, selectedStation);
    } catch (error) {
      console.log("길찾기 시뮬레이션 실패", error);
      alert("길찾기 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.");
    } finally {
      setIsRouteLoading(false);
    }
  };

  const changeLocationForm = (e) => {
    const { name, value, checked, type } = e.target;
    setLocationForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const openAddressSearch = async () => {
    try {
      await loadDaumPostcodeScript();

      new window.daum.Postcode({
        oncomplete: (data) => {
          const roadAddress = data.roadAddress || data.jibunAddress || data.address;
          const locationName = data.buildingName || data.bname || locationForm.locationName;

          setLocationForm((prev) => ({
            ...prev,
            locationName: prev.locationName || locationName || "출발지",
            address: roadAddress,
          }));
        },
      }).open();
    } catch (error) {
      console.log("주소 검색 실행 실패", error);
      alert("주소 검색을 불러오지 못했습니다. 주소를 직접 입력해 주세요.");
    }
  };

  const submitLocationForm = async (e) => {
    e.preventDefault();

    const locationName = locationForm.locationName.trim();
    const address = locationForm.address.trim();

    if (!locationName) {
      alert("출발지 이름을 입력해 주세요.");
      return;
    }

    if (!address) {
      alert("주소를 입력해 주세요.");
      return;
    }

    try {
      const response = await stations.createSavedLocation({
        locationName,
        address,
        isDefault: locationForm.isDefault,
      });

      if (response.data === "login_required") {
        alert("로그인 후 출발지를 등록할 수 있습니다.");
        navigate("/login");
        return;
      }

      if (typeof response.data === "string" && response.data !== "success") {
        alert(response.data);
        return;
      }

      alert("출발지가 등록되었습니다.");
      setLocationForm({ locationName: "", address: "", isDefault: true });
      setIsLocationFormOpen(false);
      const savedLocations = await getSavedLocations();
      const savedLocation = savedLocations.find(
        (location) =>
          location.locationName === locationName &&
          location.address === address &&
          location.latitude &&
          location.longitude
      ) || savedLocations.find(
        (location) => location.isDefault && location.latitude && location.longitude
      );

      if (savedLocation) {
        const sortedLocations = sortLocationList(
          savedLocations.map((item) => ({
            ...item,
            isDefault: String(item.locationId) === String(savedLocation.locationId),
          })),
          savedLocation.locationId
        );

        setLocationList(sortedLocations);
        setCurrentOrigin({ ...savedLocation, isDefault: true });
        clearRouteLine();
        getStationMapData(keyword.trim(), savedLocation, connectorType);
        moveMapTo(savedLocation.latitude, savedLocation.longitude, MAP_FOCUS_LEVEL);
      }
    } catch (error) {
      console.log("출발지 등록 실패", error);
      alert(error.response?.data?.message || "출발지 등록 중 오류가 발생했습니다. 주소를 다시 확인해 주세요.");
    }
  };

  const setDefaultLocation = async (location) => {
    if (!location.locationId || location.locationId === "guide") {
      alert("먼저 출발지를 등록해 주세요.");
      return;
    }

    if (!location.latitude || !location.longitude) {
      alert("좌표가 없는 출발지입니다. 다시 등록해 주세요.");
      return;
    }

    try {
      const response = await stations.setDefaultLocation(location.locationId);

      if (response.data === "login_required") {
        alert("로그인 후 기본 출발지를 설정할 수 있습니다.");
        navigate("/login");
        return;
      }

      if (typeof response.data === "string" && response.data !== "success") {
        alert(response.data);
        return;
      }

      const savedLocations = await getSavedLocations();
      const updatedLocations = savedLocations.map((item) => ({
        ...item,
        isDefault: String(item.locationId) === String(location.locationId),
      }));

      const selectedDefault = updatedLocations.find(
        (item) => String(item.locationId) === String(location.locationId)
      ) || { ...location, isDefault: true };

      setLocationList(sortLocationList(updatedLocations, selectedDefault.locationId));
      setCurrentOrigin(selectedDefault);
      clearRouteLine();
      getStationMapData(keyword.trim(), selectedDefault, connectorType);
      moveMapTo(selectedDefault.latitude, selectedDefault.longitude, MAP_FOCUS_LEVEL);
    } catch (error) {
      console.log("기본 출발지 설정 실패", error);
      alert("기본 출발지 설정 중 오류가 발생했습니다.");
    }
  };

  const deleteLocation = async (location) => {
    if (!location.locationId || location.locationId === "guide") {
      alert("삭제할 수 없는 안내 항목입니다.");
      return;
    }

    if (!window.confirm(`${location.locationName} 출발지를 삭제할까요?`)) {
      return;
    }

    try {
      const response = await stations.deleteSavedLocation(location.locationId);

      if (response.data === "login_required") {
        alert("로그인 후 출발지를 삭제할 수 있습니다.");
        navigate("/login");
        return;
      }

      const savedLocations = await getSavedLocations();
      const defaultLocation = savedLocations.find(
        (item) => item.isDefault && item.latitude && item.longitude
      );

      setCurrentOrigin(defaultLocation || null);
      clearRouteLine();
      getStationMapData(keyword.trim(), defaultLocation || null, connectorType);
    } catch (error) {
      console.log("출발지 삭제 실패", error);
      alert("출발지 삭제 중 오류가 발생했습니다.");
    }
  };

  const useSavedLocationAsOrigin = (location) => {
    if (!location.latitude || !location.longitude) {
      alert("좌표가 없는 출발지입니다. 다시 등록해 주세요.");
      return;
    }

    setDefaultLocation(location);
  };

  const moveToCurrentLocation = async () => {
    // 버튼은 현재 선택된 출발지로 이동한다.
    // 저장된 출발지가 없을 때만 브라우저 현재 위치를 새 출발지로 사용한다.
    if (currentOrigin?.latitude && currentOrigin?.longitude) {
      clearRouteLine();
      getStationMapData(keyword.trim(), currentOrigin, connectorType);
      moveMapTo(currentOrigin.latitude, currentOrigin.longitude, MAP_FOCUS_LEVEL);
      return;
    }

    setIsCurrentLocationLoading(true);

    const origin = await getBrowserCurrentOrigin(true);

    if (origin) {
      setCurrentOrigin(origin);
      clearRouteLine();
      getStationMapData(keyword.trim(), origin, connectorType);
      moveMapTo(origin.latitude, origin.longitude, MAP_FOCUS_LEVEL);
    }

    setIsCurrentLocationLoading(false);
  };

  return (
    <main className="station-map-page">
      <aside className="location-panel">
        <div className="location-section">
          <div className="location-panel-header">
            <h2>출발지</h2>
            <button type="button" onClick={() => setIsLocationFormOpen((prev) => !prev)}>
              {isLocationFormOpen ? "닫기" : "+ 위치 추가"}
            </button>
          </div>

          <button
            type="button"
            className="current-location-btn"
            onClick={moveToCurrentLocation}
            disabled={isCurrentLocationLoading}
          >
            {isCurrentLocationLoading ? "위치 확인 중..." : "📍 출발지로 이동"}
          </button>

          {currentOrigin && (
            <div className="current-origin-box">
              <strong>현재 출발지</strong>
              <span>{currentOrigin.locationName}</span>
              <p>{currentOrigin.address}</p>
            </div>
          )}

          {isLocationFormOpen && (
            <form className="location-form-card" onSubmit={submitLocationForm}>
              <label>
                이름
                <input
                  type="text"
                  name="locationName"
                  value={locationForm.locationName}
                  onChange={changeLocationForm}
                  placeholder="예: 집, 회사"
                />
              </label>
              <label>
                주소
                <div className="address-search-row">
                  <input
                    type="text"
                    name="address"
                    value={locationForm.address}
                    onChange={changeLocationForm}
                    placeholder="주소 검색 버튼을 눌러 주세요"
                  />
                  <button type="button" onClick={openAddressSearch}>
                    검색
                  </button>
                </div>
              </label>
              <label className="location-check-row">
                <input
                  type="checkbox"
                  name="isDefault"
                  checked={locationForm.isDefault}
                  onChange={changeLocationForm}
                />
                기본 출발지로 설정
              </label>
              <button type="submit">출발지 등록</button>
            </form>
          )}

          <div className="location-list">
            {locationList.map((location) => (
              <div
                className={
                  currentOrigin?.locationId === location.locationId
                    ? "location-card active"
                    : location.isDefault
                      ? "location-card default"
                      : "location-card"
                }
                key={location.locationId}
                role="button"
                tabIndex={0}
                onClick={() => useSavedLocationAsOrigin(location)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    useSavedLocationAsOrigin(location);
                  }
                }}
              >
                <strong>
                  {location.locationName}
                  {location.isDefault && <span className="location-default-badge">기본</span>}
                </strong>
                <p>{location.address}</p>

                <div className="location-actions">
                  {location.latitude && location.longitude && (
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        useSavedLocationAsOrigin(location);
                      }}
                    >
                      출발지 사용
                    </button>
                  )}

                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      deleteLocation(location);
                    }}
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="station-list-panel">
          <div className="station-list-title-row">
            <h2>{currentOrigin ? "주변 충전소" : "충전소 목록"}</h2>
            <span>{currentOrigin ? `가까운 ${nearbyStationList.length}곳` : `${nearbyStationList.length}곳`}</span>
          </div>

          {isLoading ? (
            <div className="station-list-empty">충전소를 불러오는 중입니다.</div>
          ) : nearbyStationList.length === 0 ? (
            <div className="station-list-empty">조회된 충전소가 없습니다.</div>
          ) : (
            <div className="map-station-list">
              {nearbyStationList.map((station) => (
                <button
                  type="button"
                  key={station.stationId}
                  className={
                    selectedStation?.stationId === station.stationId
                      ? "map-station-card active"
                      : "map-station-card"
                  }
                  onClick={() => selectStation(station)}
                >
                  <strong>{station.stationName}</strong>
                  <p>{station.address}</p>
                  <div>
                    <span>{station.stationStatus}</span>
                    <em>
                      사용가능 {station.availableChargerCount} / 전체 {station.chargerCount}
                    </em>
                  </div>
                  {station.distanceKm !== null && station.distanceKm !== undefined && (
                    <small>출발지 기준 {station.distanceKm}km</small>
                  )}
                </button>
              ))}
            </div>
          )}
        </div>
      </aside>

      <section className="map-content">
        <div className="map-search-bar">
          <input
            type="text"
            value={keyword}
            placeholder="지역, 충전소명 검색"
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                searchStation();
              }
            }}
          />

          <button type="button" onClick={searchStation}>
            🔍
          </button>
        </div>

        <div className="map-filter-bar">
          <label htmlFor="connectorType">충전기 타입</label>
          <select
            id="connectorType"
            value={connectorType}
            onChange={changeConnectorType}
          >
            {CONNECTOR_TYPE_OPTIONS.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>


        {routeInfo && (
          <div className="route-info-box">
            <strong>길찾기 결과</strong>
            <p>
              {routeInfo.originName} → {routeInfo.destinationName}
            </p>
            <span>
              예상 거리 {routeInfo.distanceText || "확인 불가"} · 예상 시간 {routeInfo.durationText || "확인 불가"}
            </span>
          </div>
        )}

        <div className="map-area">
          <div
            ref={mapElementRef}
            className={useKakaoMap ? "kakao-map" : "kakao-map hidden"}
          />

          {!useKakaoMap && (
            <div className="mock-map">
              {validStationList.map((station, index) => (
                <button
                  type="button"
                  className="map-marker"
                  key={station.stationId}
                  style={{
                    left: `${18 + (index % 4) * 20}%`,
                    top: `${28 + Math.floor(index / 4) * 18}%`,
                  }}
                  onClick={() => selectStation(station)}
                >
                  📍
                </button>
              ))}

              <div className="map-center-label">지도 대체 표시 영역</div>
            </div>
          )}

          {mapMessage && <div className="map-message-box">{mapMessage}</div>}
        </div>
      </section>

      <aside className="station-detail-panel">
        {selectedStation ? (
          <>
            <div className="station-detail-title">
              <h2>{selectedStation.stationName}</h2>
              <p>{selectedStation.address}</p>
              <strong>운영기관: {selectedStation.operatorName || "-"}</strong>
              <span>
                충전소 상태 <em>{selectedStation.stationStatus}</em>
              </span>
              {selectedStation.distanceKm !== null && selectedStation.distanceKm !== undefined && (
                <b>출발지 기준 {selectedStation.distanceKm}km</b>
              )}
            </div>

            <div className="station-summary-tags">
              <span>충전기 {selectedStation.chargerCount}대</span>
              <span>사용 가능 {selectedStation.availableChargerCount}대</span>
            </div>

            <div className="station-action-row">
              <button type="button" className="route-btn" onClick={openRoute} disabled={isRouteLoading}>
                {isRouteLoading ? "길찾기 중" : "길찾기"}
              </button>
              <button type="button" className="reserve-btn" onClick={moveReservation}>
                예약하기
              </button>
            </div>

            <div className="charger-info-box">
              <h3>충전기 정보</h3>

              <div className="charger-info-row">
                <div>
                  <strong>충전기 현황</strong>
                  <p>
                    등록된 충전기 {selectedStation.chargerCount}대 / 사용 가능 {" "}
                    {selectedStation.availableChargerCount}대
                  </p>
                </div>

                <span>{selectedStation.stationStatus}</span>
              </div>

              <button type="button" onClick={moveDetail}>
                상세보기
              </button>
            </div>
          </>
        ) : (
          <div className="empty-station-panel">선택된 충전소가 없습니다.</div>
        )}
      </aside>
    </main>
  );
};

export default StationMapPage;
