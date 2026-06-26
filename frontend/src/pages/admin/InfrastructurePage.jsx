import { useState } from "react";
import * as adminApi from "../../apis/adminApi";

const DAUM_POSTCODE_SCRIPT_ID = "daum-postcode-sdk-admin";

const initialForm = {
  stationName: "",
  operatorName: "",
  address: "",
  latitude: "",
  longitude: "",
  openTime: "00:00",
  closeTime: "23:59",
  stationStatus: "운영중",
  chargerList: [
    {
      chargerName: "급속 01",
      chargerType: "급속",
      connectorType: "DC콤보",
      chargingSpeedKw: 100,
      pricePerKwh: 320,
      status: "사용가능",
    },
  ],
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
    script.src = "https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js";
    script.async = true;
    script.onload = () => resolve(window.daum);
    script.onerror = reject;
    document.body.appendChild(script);
  });
};

const InfrastructurePage = () => {
  console.log("InfrastructurePage 렌더링");

  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("충전소 등록 입력 변경", name, value);
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const changeCharger = (index, name, value) => {
    console.log("설치 충전기 입력 변경", index, name, value);
    setForm((prev) => ({
      ...prev,
      chargerList: prev.chargerList.map((charger, chargerIndex) => (
        chargerIndex === index ? { ...charger, [name]: value } : charger
      )),
    }));
  };

  const addCharger = () => {
    console.log("설치 충전기 추가");
    setForm((prev) => ({
      ...prev,
      chargerList: [
        ...prev.chargerList,
        {
          chargerName: `급속 ${String(prev.chargerList.length + 1).padStart(2, "0")}`,
          chargerType: "급속",
          connectorType: "DC콤보",
          chargingSpeedKw: 100,
          pricePerKwh: 320,
          status: "사용가능",
        },
      ],
    }));
  };

  const removeCharger = (index) => {
    console.log("설치 충전기 제거", index);
    setForm((prev) => ({
      ...prev,
      chargerList: prev.chargerList.filter((_, chargerIndex) => chargerIndex !== index),
    }));
  };

  const openAddressSearch = async () => {
    console.log("다음 주소검색 열기");

    try {
      await loadDaumPostcodeScript();
      new window.daum.Postcode({
        oncomplete: (data) => {
          console.log("다음 주소검색 선택", data);
          setForm((prev) => ({
            ...prev,
            address: data.roadAddress || data.jibunAddress || prev.address,
            latitude: "",
            longitude: "",
          }));
          alert("주소가 입력되었습니다. 위도/경도는 백엔드에서 주소 기반으로 자동 저장됩니다.");
        },
      }).open();
    } catch (error) {
      console.log("다음 주소검색 로드 실패", error);
      alert("주소검색을 불러오지 못했습니다. 주소를 직접 입력해 주세요.");
    }
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

    const validChargers = form.chargerList.filter((charger) => charger.chargerName?.trim());
    if (validChargers.length === 0) {
      alert("충전소 등록 시 최소 1대 이상의 충전기 정보를 입력하세요.");
      return;
    }

    setIsSubmitting(true);

    const requestData = {
      ...form,
      latitude: form.latitude ? Number(form.latitude) : null,
      longitude: form.longitude ? Number(form.longitude) : null,
      chargerList: validChargers.map((charger) => ({
        ...charger,
        chargingSpeedKw: Number(charger.chargingSpeedKw),
        pricePerKwh: Number(charger.pricePerKwh),
      })),
    };

    try {
      const response = await adminApi.registerStation(requestData);
      console.log("충전소 등록 성공", response.data);
      alert("충전소가 등록되었습니다.");
      setForm(initialForm);
    } catch (error) {
      console.log("충전소 등록 실패", error);
      alert(error.response?.data?.message || "충전소 등록에 실패했습니다. 입력값과 백엔드 로그를 확인해 주세요.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="admin-page station-register-page">
      <div className="admin-page-header">
        <div>
          <p>충전소관리</p>
          <h1>충전소 등록</h1>
          <span>신규 충전소 위치와 운영정보, 설치 충전기 정보를 실제 DB에 등록합니다.</span>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel station-register-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전소 기본 정보</strong>
              <p>주소검색으로 위치를 입력하면 위도와 경도는 내부에서 자동 저장됩니다.</p>
            </div>
          </div>

          <form className="admin-form-grid refined-form station-register-form" onSubmit={submitStation}>
            <label>
              <span>충전소명 <em>필수</em></span>
              <input type="text" name="stationName" value={form.stationName} placeholder="예: 부산시청 공공충전소" onChange={changeValue} />
            </label>
            <label>
              <span>운영기관</span>
              <input type="text" name="operatorName" value={form.operatorName} placeholder="예: 부산시" onChange={changeValue} />
            </label>
            <label className="full address-field">
              <span>주소 <em>필수</em></span>
              <div>
                <input type="text" name="address" value={form.address} placeholder="다음 주소검색 또는 직접 입력" onChange={changeValue} />
                <button type="button" onClick={openAddressSearch}>다음 주소검색</button>
              </div>
            </label>
            <label>
              <span>운영 시작</span>
              <input type="time" name="openTime" value={form.openTime} onChange={changeValue} />
            </label>
            <label>
              <span>운영 종료</span>
              <input type="time" name="closeTime" value={form.closeTime} onChange={changeValue} />
            </label>
            <label className="full">
              <span>초기 상태</span>
              <select name="stationStatus" value={form.stationStatus} onChange={changeValue}>
                <option>운영중</option>
                <option>점검중</option>
                <option>운영중지</option>
              </select>
            </label>

            <div className="full charger-seed-box modern-charger-box">
              <div className="admin-panel-title compact-title">
                <div><strong>설치 충전기 정보</strong><p>충전소 등록 시 최소 1대 이상의 충전기 정보를 함께 저장합니다.</p></div>
                <button type="button" onClick={addCharger}>충전기 추가</button>
              </div>

              {form.chargerList.map((charger, index) => (
                <div className="charger-seed-card" key={`${charger.chargerName}-${index}`}>
                  <div className="charger-seed-head">
                    <strong>충전기 {index + 1}</strong>
                    <button type="button" className="gray" onClick={() => removeCharger(index)} disabled={form.chargerList.length <= 1}>삭제</button>
                  </div>
                  <div className="charger-seed-grid">
                    <label><span>충전기명</span><input value={charger.chargerName} placeholder="예: 급속 01" onChange={(e) => changeCharger(index, "chargerName", e.target.value)} /></label>
                    <label><span>충전기 유형</span><select value={charger.chargerType} onChange={(e) => changeCharger(index, "chargerType", e.target.value)}><option>완속</option><option>급속</option><option>초급속</option></select></label>
                    <label><span>커넥터 타입</span><select value={charger.connectorType} onChange={(e) => changeCharger(index, "connectorType", e.target.value)}><option>DC콤보</option><option>AC3상</option><option>NACS</option></select></label>
                    <label><span>출력(kW)</span><input type="number" value={charger.chargingSpeedKw} onChange={(e) => changeCharger(index, "chargingSpeedKw", e.target.value)} /></label>
                    <label><span>요금(원/kWh)</span><input type="number" value={charger.pricePerKwh} onChange={(e) => changeCharger(index, "pricePerKwh", e.target.value)} /></label>
                  </div>
                </div>
              ))}
            </div>

            <div className="admin-action-row full">
              <button type="submit" disabled={isSubmitting}>{isSubmitting ? "등록 중" : "충전소 등록"}</button>
              <button type="button" className="gray" onClick={() => setForm(initialForm)}>초기화</button>
            </div>
          </form>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div><strong>등록 후 반영 위치</strong><p>직접 등록 데이터도 공공데이터 적재 데이터와 같은 테이블을 사용합니다.</p></div>
          </div>
          <div className="admin-guide-list polished-guide">
            <p><b>충전소 운영관리</b> 등록 즉시 목록과 지역 검색에 표시됩니다.</p>
            <p><b>사용자 지도</b> 주소 기반 좌표가 저장되면 지도 마커와 예약 화면에서 사용할 수 있습니다.</p>
            <p><b>예약관리</b> 사용자가 해당 충전기를 예약하면 예약 현황에 반영됩니다.</p>
            <p><b>통계분석</b> 충전 완료 세션이 생기면 이용통계와 매출통계에 바로 반영됩니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
};

export default InfrastructurePage;
