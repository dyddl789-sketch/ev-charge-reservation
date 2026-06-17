import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as stationApi from "../../apis/stationApi";
import "../../styles/station-reservation.css";

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
  }, [stationId]);

  if (!station) {
    return <section className="station-page"><div className="station-inner">충전소 정보를 불러오는 중입니다.</div></section>;
  }

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
            <Link to={`/reservation?stationId=${station.stationId}`} className="reserve-main-btn">
              이 충전소 예약하기
            </Link>
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
              <Link to={`/reservation?chargerId=${charger.chargerId}`}>이 충전기로 예약</Link>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default StationDetailPage;
