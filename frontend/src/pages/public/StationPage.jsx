import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as stationApi from "../../apis/stationApi";
import "../../styles/station-reservation.css";


const isStationReservable = (station) => {
  const stationOpen = station?.stationStatus === "운영중";
  const availableCount = Number(station?.availableChargerCount || 0);
  return stationOpen && availableCount > 0;
};

const getStationReserveMessage = (station) => {
  if (station?.stationStatus !== "운영중") {
    return "현재 운영중인 충전소가 아닙니다.";
  }

  if (Number(station?.availableChargerCount || 0) <= 0) {
    return "현재 예약 가능한 충전기가 없습니다.";
  }

  return "예약 가능한 충전기가 있습니다.";
};

const mockStations = [
  {
    stationId: 1,
    stationName: "부산시청 공영주차장 충전소",
    address: "부산 연제구 중앙대로 1001",
    operatorName: "부산광역시",
    stationStatus: "운영중",
    chargerCount: 6,
    availableChargerCount: 4,
    imageUrl: "/images/station/station-default.jpg",
  },
  {
    stationId: 2,
    stationName: "해운대 공영주차장 급속충전소",
    address: "부산 해운대구 우동",
    operatorName: "환경부",
    stationStatus: "운영중",
    chargerCount: 4,
    availableChargerCount: 2,
    imageUrl: "/images/station/station-fast-row.jpg",
  },
];

const StationPage = () => {
  console.log("StationPage 렌더링");

  const [keyword, setKeyword] = useState("");
  const [stationList, setStationList] = useState([]);

  const getStationList = async (searchKeyword = "") => {
    console.log("getStationList 실행", searchKeyword);

    try {
      const response = await stationApi.list(searchKeyword);
      console.log("충전소 목록 응답", response.data);
      setStationList(Array.isArray(response.data) ? response.data : mockStations);
    } catch (error) {
      console.log("충전소 목록 조회 실패 - 목업 데이터 사용", error);
      setStationList(mockStations);
    }
  };

  useEffect(() => {
    getStationList();
  }, []);

  const submitSearch = (e) => {
    e.preventDefault();
    getStationList(keyword);
  };

  return (
    <section className="station-page">
      <div className="station-inner">
        <div className="station-title-row">
          <div>
            <p className="page-subtitle">EV Charge Reservation v2.0</p>
            <h1>충전소 찾기</h1>
            <p>충전소 위치, 운영기관, 충전기 상태를 확인하고 예약으로 이동합니다.</p>
          </div>
          <Link to="/stations/map" className="map-link-btn">
            지도에서 보기
          </Link>
        </div>

        <form className="station-search-box" onSubmit={submitSearch}>
          <input
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder="충전소명 또는 주소를 입력하세요."
          />
          <button type="submit">검색</button>
        </form>

        <div className="station-list">
          {stationList.map((station) => (
            <article className="station-card" key={station.stationId}>
              <div className="station-thumb">
                <img src={station.imageUrl || "/images/station/station-default.jpg"} alt={station.stationName} />
              </div>
              <div className="station-info">
                <div className="station-head">
                  <h2>{station.stationName}</h2>
                  <span className={`status-badge ${station.stationStatus === "운영중" ? "ok" : "stop"}`}>
                    {station.stationStatus}
                  </span>
                </div>
                <p>{station.address}</p>
                <p className="station-meta">운영기관: {station.operatorName || "-"}</p>
                <div className="station-counts">
                  <span>전체 {station.chargerCount || 0}기</span>
                  <span>사용가능 {station.availableChargerCount || 0}기</span>
                </div>
                <div className="station-actions">
                  <Link to={`/stations/${station.stationId}`}>상세보기</Link>
                  {isStationReservable(station) ? (
                    <Link to={`/reservation?stationId=${station.stationId}`}>예약하기</Link>
                  ) : (
                    <button
                      type="button"
                      className="station-reserve-disabled"
                      disabled
                      title={getStationReserveMessage(station)}
                    >
                      예약불가
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
};

export default StationPage;
