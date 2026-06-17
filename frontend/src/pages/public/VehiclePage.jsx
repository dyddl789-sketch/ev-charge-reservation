import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import * as vehicleApi from "../../apis/vehicleApi";
import "../../styles/vehicle.css";

const mockVehicles = [
  {
    vehicleId: 1,
    vehicleNickname: "출퇴근용 아이오닉",
    manufacturer: "현대",
    modelName: "아이오닉5",
    batteryCapacityKwh: 77.4,
    connectorType: "DC콤보",
    maxChargingSpeedKw: 220,
    plateNumber: "12가1234",
    imageUrl: "/images/vehicle/아이오닉.jpg",
    isDefault: true,
  },
  {
    vehicleId: 2,
    vehicleNickname: "주말용 EV6",
    manufacturer: "기아",
    modelName: "EV6",
    batteryCapacityKwh: 77.4,
    connectorType: "DC콤보",
    maxChargingSpeedKw: 240,
    plateNumber: "34나5678",
    imageUrl: "/images/vehicle/EV6.jpg",
    isDefault: false,
  },
];

const VehiclePage = () => {
  console.log("VehiclePage 렌더링");

  const [vehicleList, setVehicleList] = useState([]);
  const [loading, setLoading] = useState(true);

  const getVehicleList = async () => {
    console.log("getVehicleList 실행");

    try {
      const response = await vehicleApi.list();
      console.log("차량 목록 응답", response.data);
      setVehicleList(Array.isArray(response.data) ? response.data : mockVehicles);
    } catch (error) {
      console.log("차량 목록 조회 실패 - 목업 데이터 사용", error);
      setVehicleList(mockVehicles);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getVehicleList();
  }, []);

  const handleSetDefault = async (vehicleId) => {
    console.log("기본 차량 설정 클릭", vehicleId);

    try {
      const response = await vehicleApi.setDefault(vehicleId);
      console.log("기본 차량 설정 응답", response.data);

      if (response.data !== "success") {
        alert("기본 차량 설정에 실패했습니다.");
        return;
      }

      setVehicleList((prev) =>
        prev
          .map((vehicle) => ({
            ...vehicle,
            isDefault: vehicle.vehicleId === vehicleId,
          }))
          .sort((a, b) => Number(b.isDefault) - Number(a.isDefault))
      );
    } catch (error) {
      console.log("기본 차량 설정 오류", error);
      alert("서버 오류가 발생했습니다.");
    }
  };

  const handleDelete = async (vehicleId) => {
    console.log("차량 삭제 클릭", vehicleId);

    if (!window.confirm("해당 차량을 삭제하시겠습니까?")) {
      return;
    }

    try {
      const response = await vehicleApi.remove(vehicleId);
      console.log("차량 삭제 응답", response.data);

      if (response.data !== "success") {
        alert("차량 삭제에 실패했습니다.");
        return;
      }

      setVehicleList((prev) => prev.filter((vehicle) => vehicle.vehicleId !== vehicleId));
      alert("차량이 삭제되었습니다.");
    } catch (error) {
      console.log("차량 삭제 오류", error);
      alert("서버 오류가 발생했습니다.");
    }
  };

  const defaultVehicle = vehicleList.find((vehicle) => vehicle.isDefault);

  return (
    <div className="vehicle-page">
      <main className="vehicle-main">
        <section className="page-title">
          <div>
            <h1>내 차량</h1>
            <p>등록된 차량 정보를 기준으로 충전소 추천과 예약 정보를 제공합니다.</p>
          </div>

          <Link to="/vehicles/register" className="add-btn">
            차량 등록
          </Link>
        </section>

        <section className="vehicle-summary">
          <div className="summary-card total-card">
            <span>등록 차량</span>
            <strong>{vehicleList.length}대</strong>
          </div>

          <div className="summary-card default-card">
            <span>기본 차량</span>
            <strong>{defaultVehicle?.vehicleNickname || defaultVehicle?.modelName || "-"}</strong>
          </div>
        </section>

        {loading ? (
          <div className="empty-box">차량 정보를 불러오는 중입니다.</div>
        ) : (
          <section className="vehicle-list">
            {vehicleList.length === 0 && (
              <div className="empty-box">
                <p>등록된 차량이 없습니다.</p>
                <Link to="/vehicles/register" className="add-btn">
                  차량 등록하기
                </Link>
              </div>
            )}

            {vehicleList.map((vehicle) => (
              <article
                className={`vehicle-card ${vehicle.isDefault ? "main-vehicle" : ""}`}
                key={vehicle.vehicleId}
              >
                <div className="vehicle-image">
                  <img src={vehicle.imageUrl || "/images/vehicle/EV6.jpg"} alt={vehicle.modelName} />
                </div>

                <div className="vehicle-info">
                  <div className="vehicle-title-row">
                    <div>
                      {vehicle.isDefault && <span className="badge">기본 차량</span>}
                      <h2>{vehicle.vehicleNickname || vehicle.modelName}</h2>
                    </div>
                    <span className="vehicle-status">{vehicle.isDefault ? "사용중" : "대기"}</span>
                  </div>

                  <p className="vehicle-desc">
                    {vehicle.manufacturer} · {vehicle.connectorType} · {vehicle.modelName}
                  </p>

                  <div className="vehicle-data">
                    <div>
                      <span>배터리 용량</span>
                      <strong>{vehicle.batteryCapacityKwh}kWh</strong>
                    </div>
                    <div>
                      <span>최대 충전 속도</span>
                      <strong>{vehicle.maxChargingSpeedKw}kW</strong>
                    </div>
                    <div>
                      <span>충전 타입</span>
                      <strong>{vehicle.connectorType}</strong>
                    </div>
                    <div>
                      <span>차량 번호</span>
                      <strong>{vehicle.plateNumber}</strong>
                    </div>
                  </div>

                  <div className="card-buttons">
                    {vehicle.isDefault ? (
                      <button type="button" className="outline-btn active-btn" disabled>
                        설정됨
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="outline-btn default-btn"
                        onClick={() => handleSetDefault(vehicle.vehicleId)}
                      >
                        기본 차량 설정
                      </button>
                    )}

                    <button
                      type="button"
                      className="danger-btn delete-btn"
                      onClick={() => handleDelete(vehicle.vehicleId)}
                    >
                      삭제
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </section>
        )}
      </main>
    </div>
  );
};

export default VehiclePage;
