import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import * as vehicleApi from "../../apis/vehicleApi";
import "../../styles/vehicle.css";

const mockModelList = [
  {
    modelId: 1,
    manufacturer: "현대",
    modelName: "아이오닉5",
    batteryCapacityKwh: 77.4,
    connectorType: "DC콤보",
    maxChargingSpeedKw: 220,
    imageUrl: "/images/vehicle/아이오닉.jpg",
  },
  {
    modelId: 2,
    manufacturer: "기아",
    modelName: "EV6",
    batteryCapacityKwh: 77.4,
    connectorType: "DC콤보",
    maxChargingSpeedKw: 240,
    imageUrl: "/images/vehicle/EV6.jpg",
  },
  {
    modelId: 3,
    manufacturer: "테슬라",
    modelName: "Model 3",
    batteryCapacityKwh: 82,
    connectorType: "NACS",
    maxChargingSpeedKw: 250,
    imageUrl: "/images/vehicle/모델3.jpg",
  },
];

const VehicleRegisterPage = () => {
  console.log("VehicleRegisterPage 렌더링");

  const navigate = useNavigate();

  const [modelList, setModelList] = useState([]);
  const [form, setForm] = useState({
    modelId: "",
    vehicleNickname: "",
    plateNumber: "",
    isDefault: false,
  });

  const getModelList = async () => {
    console.log("getModelList 실행");

    try {
      const response = await vehicleApi.modelList();
      console.log("차량 모델 목록 응답", response.data);
      const data = Array.isArray(response.data) ? response.data : mockModelList;
      setModelList(data);

      if (data.length > 0) {
        setForm((prev) => ({ ...prev, modelId: String(data[0].modelId) }));
      }
    } catch (error) {
      console.log("차량 모델 조회 실패 - 목업 데이터 사용", error);
      setModelList(mockModelList);
      setForm((prev) => ({ ...prev, modelId: String(mockModelList[0].modelId) }));
    }
  };

  useEffect(() => {
    getModelList();
  }, []);

  const selectedModel = useMemo(() => {
    return modelList.find((model) => String(model.modelId) === String(form.modelId));
  }, [modelList, form.modelId]);

  const changeValue = (e) => {
    const { name, value, type, checked } = e.target;
    console.log("차량 등록 입력 변경", name, value);

    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const submitVehicle = async (e) => {
    e.preventDefault();
    console.log("차량 등록 submit", form);

    if (!form.modelId) {
      alert("차량 모델을 선택해 주세요.");
      return;
    }

    if (!form.plateNumber.trim()) {
      alert("차량 번호를 입력해 주세요.");
      return;
    }

    try {
      await vehicleApi.create({
        modelId: form.modelId,
        vehicleNickname: form.vehicleNickname,
        plateNumber: form.plateNumber,
        isDefault: form.isDefault,
      });

      alert("차량 등록이 완료되었습니다.");
      navigate("/vehicles");
    } catch (error) {
      console.log("차량 등록 오류", error);
      alert("차량 등록 중 오류가 발생했습니다. 차량 번호 중복 여부를 확인해 주세요.");
    }
  };

  return (
    <div className="vehicle-page">
      <main className="vehicle-main">
        <section className="page-title">
          <div>
            <h1>차량 등록</h1>
            <p>차량 정보를 등록하면 예약 시 예상 충전 시간과 비용을 계산할 수 있습니다.</p>
          </div>
          <Link to="/vehicles" className="outline-link-btn">
            내 차량으로
          </Link>
        </section>

        <section className="vehicle-register-layout">
          <form className="vehicle-form-card" onSubmit={submitVehicle}>
            <div className="form-section">
              <h3>차량 모델 선택</h3>

              <div className="form-group">
                <label htmlFor="modelId">차량 모델</label>
                <select id="modelId" name="modelId" value={form.modelId} onChange={changeValue}>
                  {modelList.map((model) => (
                    <option key={model.modelId} value={model.modelId}>
                      {model.manufacturer} {model.modelName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-grid">
                <div className="form-group">
                  <label>제조사</label>
                  <input value={selectedModel?.manufacturer || ""} readOnly />
                </div>
                <div className="form-group">
                  <label>배터리 용량</label>
                  <input value={selectedModel ? `${selectedModel.batteryCapacityKwh} kWh` : ""} readOnly />
                </div>
                <div className="form-group">
                  <label>커넥터 타입</label>
                  <input value={selectedModel?.connectorType || ""} readOnly />
                </div>
                <div className="form-group">
                  <label>최대 충전 속도</label>
                  <input value={selectedModel ? `${selectedModel.maxChargingSpeedKw} kW` : ""} readOnly />
                </div>
              </div>
            </div>

            <div className="form-section">
              <h3>내 차량 정보</h3>

              <div className="form-group">
                <label htmlFor="vehicleNickname">차량 별칭</label>
                <input
                  id="vehicleNickname"
                  name="vehicleNickname"
                  value={form.vehicleNickname}
                  onChange={changeValue}
                  placeholder="예: 출퇴근용 차량"
                />
              </div>

              <div className="form-group">
                <label htmlFor="plateNumber">차량 번호</label>
                <input
                  id="plateNumber"
                  name="plateNumber"
                  value={form.plateNumber}
                  onChange={changeValue}
                  placeholder="예: 12가1234"
                />
              </div>

              <label className="check-row">
                <input type="checkbox" name="isDefault" checked={form.isDefault} onChange={changeValue} />
                기본 차량으로 설정
              </label>
            </div>

            <div className="form-buttons">
              <button type="submit" className="submit-btn">
                차량 등록
              </button>
              <Link to="/vehicles" className="cancel-btn">
                취소
              </Link>
            </div>
          </form>

          <aside className="vehicle-preview-card">
            <div className="vehicle-image large">
              <img src={selectedModel?.imageUrl || "/images/vehicle/EV6.jpg"} alt={selectedModel?.modelName || "차량"} />
            </div>
            <h2>{selectedModel ? `${selectedModel.manufacturer} ${selectedModel.modelName}` : "차량 모델"}</h2>
            <p>{selectedModel?.connectorType || "커넥터"} · {selectedModel?.batteryCapacityKwh || "-"}kWh</p>
          </aside>
        </section>
      </main>
    </div>
  );
};

export default VehicleRegisterPage;
