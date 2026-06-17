import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as stations from "../../apis/stationApi";
import "../../styles/station-map.css";

const StationMapPage = () => {
  console.log("StationMapPage 렌더링");

  const navigate = useNavigate();

  const [keyword, setKeyword] = useState("");
  const [stationList, setStationList] = useState([]);
  const [selectedStation, setSelectedStation] = useState(null);
  const [locationList, setLocationList] = useState([]);

  const mockStations = [
    {
      stationId: 1,
      stationName: "테스트 서면 충전소2",
      address: "부산 부산진구 서면문화로 5",
      operatorName: "환경부",
      status: "운영중",
      chargerCount: 2,
      availableCount: 2,
      parkingAvailable: true,
      latitude: 35.1577,
      longitude: 129.0592,
    },
    {
      stationId: 2,
      stationName: "부산시청 공영주차장 충전소",
      address: "부산 연제구 중앙대로 1001",
      operatorName: "부산광역시",
      status: "운영중",
      chargerCount: 6,
      availableCount: 4,
      parkingAvailable: true,
      latitude: 35.1798,
      longitude: 129.075,
    },
  ];

  const mockLocations = [
    {
      locationId: 1,
      locationName: "회사",
      address: "부산 기장군 정관읍 가동길 8-9",
      isDefault: true,
    },
    {
      locationId: 2,
      locationName: "집",
      address: "부산 영도구 일산봉로 11-14",
      isDefault: false,
    },
  ];

  useEffect(() => {
    getStationMapData();
    getSavedLocations();
  }, []);

  const getStationMapData = async () => {
    console.log("지도 충전소 데이터 조회 실행");

    try {
      const response = await stations.mapData();
      const data = response.data;

      console.log("지도 충전소 데이터 응답", data);

      setStationList(data);
      setSelectedStation(data[0]);
    } catch (error) {
      console.log("지도 충전소 데이터 조회 실패", error);

      setStationList(mockStations);
      setSelectedStation(mockStations[0]);
    }
  };

  const getSavedLocations = async () => {
    console.log("출발지 목록 조회 실행");

    try {
      const response = await stations.savedLocations();
      const data = response.data;

      console.log("출발지 목록 응답", data);

      setLocationList(data);
    } catch (error) {
      console.log("출발지 목록 조회 실패", error);

      setLocationList(mockLocations);
    }
  };

  const searchStation = () => {
    console.log("충전소 검색 실행", keyword);

    if (!keyword.trim()) {
      setStationList(mockStations);
      setSelectedStation(mockStations[0]);
      return;
    }

    const result = mockStations.filter(
      (station) =>
        station.stationName.includes(keyword) ||
        station.address.includes(keyword)
    );

    setStationList(result);
    setSelectedStation(result[0] || null);
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

  return (
    <main className="station-map-page">
      <aside className="location-panel">
        <div className="location-panel-header">
          <h2>출발지 등록</h2>
          <button type="button">+ 위치 추가</button>
        </div>

        <div className="location-list">
          {locationList.map((location) => (
            <div
              className={
                location.isDefault
                  ? "location-card active"
                  : "location-card"
              }
              key={location.locationId}
            >
              <strong>{location.locationName}</strong>
              <p>{location.address}</p>

              <div className="location-actions">
                {location.isDefault ? (
                  <span>기본 위치</span>
                ) : (
                  <button type="button">기본 설정</button>
                )}

                <button type="button">삭제</button>
              </div>
            </div>
          ))}
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

        <div className="map-area">
          <div className="mock-map">
            {stationList.map((station, index) => (
              <button
                type="button"
                className="map-marker"
                key={station.stationId}
                style={{
                  left: `${22 + index * 16}%`,
                  top: `${28 + (index % 3) * 18}%`,
                }}
                onClick={() => {
                  console.log("지도 마커 클릭", station);
                  setSelectedStation(station);
                }}
              >
                📍
              </button>
            ))}

            <div className="map-center-label">부산광역시 지도 영역</div>
          </div>

          <button
            type="button"
            className="map-list-btn"
            onClick={() => navigate("/stations/list")}
          >
            목록 보기
          </button>
        </div>
      </section>

      <aside className="station-detail-panel">
        {selectedStation ? (
          <>
            <div className="station-detail-title">
              <h2>{selectedStation.stationName} ☆</h2>
              <p>{selectedStation.address}</p>
              <strong>출발지: 회사 - 부산 기장군 정관읍 가동길 8-9</strong>
              <span>
                24시간 운영{" "}
                <em>{selectedStation.status}</em>
              </span>
            </div>

            <div className="station-summary-tags">
              <span>충전기 {selectedStation.chargerCount}대</span>
              <span>사용 가능 {selectedStation.availableCount}대</span>
              <span>
                {selectedStation.parkingAvailable ? "주차 가능" : "주차 불가"}
              </span>
            </div>

            <div className="station-action-row">
              <button type="button" className="route-btn">
                길찾기
              </button>
              <button
                type="button"
                className="reserve-btn"
                onClick={moveReservation}
              >
                예약하기
              </button>
            </div>

            <div className="charger-info-box">
              <h3>충전기 정보</h3>

              <div className="charger-info-row">
                <div>
                  <strong>충전기 정보</strong>
                  <p>
                    등록된 충전기 {selectedStation.chargerCount}대 / 사용 가능{" "}
                    {selectedStation.availableCount}대
                  </p>
                </div>

                <span>사용 가능</span>
              </div>

              <button type="button" onClick={moveDetail}>
                상세보기
              </button>
            </div>
          </>
        ) : (
          <div className="empty-station-panel">
            선택된 충전소가 없습니다.
          </div>
        )}
      </aside>
    </main>
  );
};

export default StationMapPage;