import { useState } from "react";
import * as adminApi from "../../apis/adminApi";

const initialForm = {
  stationName: "",
  operatorName: "",
  address: "",
  latitude: "",
  longitude: "",
  openTime: "00:00",
  closeTime: "23:59",
  stationStatus: "운영중",
};

const InfrastructurePage = () => {
  console.log("InfrastructurePage 렌더링");

  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("충전소 등록 입력 변경", name, value);

    setForm({
      ...form,
      [name]: value,
    });
  };

  const submitStation = async (e) => {
    e.preventDefault();
    console.log("충전소 등록 요청", form);

    if (!form.stationName.trim()) {
      alert("충전소명을 입력하세요.");
      return;
    }

    if (!form.address.trim()) {
      alert("주소를 입력하세요.");
      return;
    }

    setIsSubmitting(true);

    const requestData = {
      ...form,
      latitude: form.latitude ? Number(form.latitude) : null,
      longitude: form.longitude ? Number(form.longitude) : null,
    };

    try {
      const response = await adminApi.registerStation(requestData);
      console.log("충전소 등록 성공", response.data);
      alert("충전소가 등록되었습니다.");
      setForm(initialForm);
    } catch (error) {
      console.log("충전소 등록 API 미연동 또는 실패", error);
      alert("현재는 화면 확인 단계입니다. 백엔드 REST API 연결 후 실제 등록됩니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>충전소관리</p>
          <h1>충전소 등록</h1>
          <span>신규 충전소 기본 정보와 위치, 운영시간, 초기 운영상태를 등록합니다.</span>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>신규 충전소 정보</strong>
              <p>등록 후 충전소 운영관리 화면에서 충전기 등록과 상태 관리를 진행합니다.</p>
            </div>
          </div>

          <form className="admin-form-grid" onSubmit={submitStation}>
            <label>
              충전소명 <b>*</b>
              <input
                type="text"
                name="stationName"
                value={form.stationName}
                placeholder="예: 부산시청 공공충전소"
                onChange={changeValue}
              />
            </label>
            <label>
              운영기관
              <input
                type="text"
                name="operatorName"
                value={form.operatorName}
                placeholder="예: 부산시"
                onChange={changeValue}
              />
            </label>
            <label className="full">
              주소 <b>*</b>
              <input
                type="text"
                name="address"
                value={form.address}
                placeholder="예: 부산 연제구 중앙대로 1001"
                onChange={changeValue}
              />
            </label>
            <label>
              위도
              <input
                type="number"
                step="0.000001"
                name="latitude"
                value={form.latitude}
                placeholder="예: 35.179554"
                onChange={changeValue}
              />
            </label>
            <label>
              경도
              <input
                type="number"
                step="0.000001"
                name="longitude"
                value={form.longitude}
                placeholder="예: 129.075642"
                onChange={changeValue}
              />
            </label>
            <label>
              운영 시작
              <input type="time" name="openTime" value={form.openTime} onChange={changeValue} />
            </label>
            <label>
              운영 종료
              <input type="time" name="closeTime" value={form.closeTime} onChange={changeValue} />
            </label>
            <label className="full">
              초기 상태
              <select name="stationStatus" value={form.stationStatus} onChange={changeValue}>
                <option>운영중</option>
                <option>점검중</option>
                <option>운영중지</option>
              </select>
            </label>
            <div className="admin-action-row full">
              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "등록 중" : "충전소 등록"}
              </button>
              <button type="button" className="gray" onClick={() => setForm(initialForm)}>
                초기화
              </button>
            </div>
          </form>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>운영 흐름</strong>
              <p>충전소와 충전기를 분리하지 않고 충전소 기준으로 운영합니다.</p>
            </div>
          </div>
          <div className="admin-guide-list">
            <p><b>1. 충전소 등록</b> 신규 충전소의 위치와 운영정보를 저장합니다.</p>
            <p><b>2. 충전소 운영관리</b> 해당 충전소에 충전기를 등록합니다.</p>
            <p><b>3. 상태 관리</b> 충전기 상태를 사용가능, 사용중, 점검중, 고장으로 변경합니다.</p>
            <p><b>4. 장애·점검 연계</b> 점검중 또는 고장 상태는 장애·점검관리에서 담당자 배정과 조치 이력으로 관리합니다.</p>
            <p><b>5. 전자결재 연계</b> 부품 교체나 예산 사용이 필요한 경우 전자결재 상신 후 처리합니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
};

export default InfrastructurePage;
