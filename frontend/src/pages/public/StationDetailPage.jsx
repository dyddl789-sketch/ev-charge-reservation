import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as stationApi from "../../apis/stationApi";
import "../../styles/station-reservation.css";

const CHARGER_STATUS_POLLING_MS = 30000;

const isChargerReservable = (station, charger) => {
  return station?.stationStatus === "운영중" && charger?.status === "사용가능";
};

const getChargerStatusMessage = (station, charger) => {
  if (station?.stationStatus !== "운영중") {
    return "충전소가 운영중이 아니므로 예약할 수 없습니다.";
  }

  if (charger?.status === "점검중") {
    return "현재 시설관리 담당자가 점검 중인 충전기입니다.";
  }

  if (charger?.status === "고장") {
    return "현재 고장 상태로 예약할 수 없습니다.";
  }

  if (charger?.status !== "사용가능") {
    return "현재 예약할 수 없는 충전기입니다.";
  }

  return "예약 가능한 충전기입니다.";
};

const StationDetailPage = () => {
  console.log("StationDetailPage 렌더링");

  const { stationId } = useParams();
  const [station, setStation] = useState(null);
  const [chargerList, setChargerList] = useState([]);

  useEffect(() => {
    const getDetail = async () => {
      console.log("충전소 상세 조회", stationId);

      try {
        const [stationResponse, chargerResponse] = await Promise.all([
          stationApi.detail(stationId),
          stationApi.chargerList(stationId),
        ]);

        console.log("충전소 상세 응답", stationResponse.data);
        console.log("충전기 목록 응답", chargerResponse.data);

        setStation(stationResponse.data?.station || stationResponse.data);
        setChargerList(Array.isArray(chargerResponse.data) ? chargerResponse.data : []);
      } catch (error) {
        console.log("충전소 상세 조회 실패 - 기본 데이터 사용", error);
        setStation({
          stationId,
          stationName: "부산시청 공영주차장 충전소",
          address: "부산 연제구 중앙대로 1001",
          operatorName: "부산광역시",
          stationStatus: "운영중",
          imageUrl: "/images/station/station-default.jpg",
        });
        setChargerList([
          {
            chargerId: 1,
            chargerName: "급속 1번",
            chargerType: "급속",
            connectorType: "DC콤보",
            chargingSpeedKw: 100,
            pricePerKwh: 320,
            status: "사용가능",
          },
        ]);
      }
    };

    getDetail();

    const timer = window.setInterval(() => {
      console.log("충전소 상세 상태 자동 재조회", stationId);
      getDetail();
    }, CHARGER_STATUS_POLLING_MS);

    return () => {
      console.log("충전소 상세 상태 자동 재조회 종료", stationId);
      window.clearInterval(timer);
    };
  }, [stationId]);

  if (!station) {
    return <section className="station-page"><div className="station-inner">충전소 정보를 불러오는 중입니다.</div></section>;
  }

  const hasReservableCharger = chargerList.some((charger) => isChargerReservable(station, charger));

  return (
    <section className="station-page">
      <div className="station-inner">
        <div className="station-detail-card">
          <div className="station-detail-image">
            <img src={station.imageUrl || "/images/station/station-default.jpg"} alt={station.stationName} />
          </div>
          <div>
            <span className={`status-badge ${station.stationStatus === "운영중" ? "ok" : "stop"}`}>
              {station.stationStatus}
            </span>
            <h1>{station.stationName}</h1>
            <p>{station.address}</p>
            <p>운영기관: {station.operatorName || "-"}</p>
            {hasReservableCharger ? (
              <Link to={`/reservation?stationId=${station.stationId}`} className="reserve-main-btn">
                이 충전소 예약하기
              </Link>
            ) : (
              <button
                type="button"
                className="reserve-main-btn disabled"
                disabled
                title={station.stationStatus !== "운영중" ? "현재 운영중인 충전소가 아닙니다." : "현재 예약 가능한 충전기가 없습니다."}
              >
                예약불가
              </button>
            )}
          </div>
        </div>

        <h2 className="sub-heading">충전기 목록</h2>
        <div className="charger-list">
          {chargerList.map((charger) => (
            <div className="charger-card" key={charger.chargerId}>
              <strong>{charger.chargerName}</strong>
              <p>{charger.chargerType} · {charger.connectorType}</p>
              <p>{charger.chargingSpeedKw}kW · {charger.pricePerKwh}원/kWh</p>
              <span className={`status-badge ${charger.status === "사용가능" ? "ok" : "stop"}`}>
                {charger.status}
              </span>
              <p className="charger-status-desc">{getChargerStatusMessage(station, charger)}</p>
              {isChargerReservable(station, charger) ? (
                <Link to={`/reservation?chargerId=${charger.chargerId}`}>이 충전기로 예약</Link>
              ) : (
                <button type="button" className="charger-reserve-disabled" disabled>
                  예약불가
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default StationDetailPage;
