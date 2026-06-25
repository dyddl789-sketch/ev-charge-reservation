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
    console.log("초기 충전기 입력 변경", index, name, value);
    setForm((prev) => ({
      ...prev,
      chargerList: prev.chargerList.map((charger, chargerIndex) => (
        chargerIndex === index ? { ...charger, [name]: value } : charger
      )),
    }));
  };

  const addCharger = () => {
    console.log("초기 충전기 추가");
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
    console.log("초기 충전기 제거", index);
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
          }));
          alert("주소가 입력되었습니다. 위도/경도는 카카오맵 또는 DBeaver 기준 좌표를 확인해서 입력해 주세요.");
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

    if (!form.latitude || !form.longitude) {
      alert("위도와 경도를 입력하세요. 지도 표시와 거리 계산에 필요합니다.");
      return;
    }

    setIsSubmitting(true);

    const requestData = {
      ...form,
      latitude: Number(form.latitude),
      longitude: Number(form.longitude),
      chargerList: form.chargerList
        .filter((charger) => charger.chargerName?.trim())
        .map((charger) => ({
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
      alert("충전소 등록에 실패했습니다. 입력값과 백엔드 로그를 확인해 주세요.");
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
          <span>신규 충전소 위치와 운영정보, 초기 충전기 정보를 실제 DB에 등록합니다.</span>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>신규 충전소 정보</strong>
              <p>다음 주소검색으로 주소를 깔끔하게 입력하고, 위도/경도까지 저장하면 지도와 통계에 바로 반영됩니다.</p>
            </div>
          </div>

          <form className="admin-form-grid refined-form" onSubmit={submitStation}>
            <label>
              충전소명 <b>*</b>
              <input type="text" name="stationName" value={form.stationName} placeholder="예: 부산시청 공공충전소" onChange={changeValue} />
            </label>
            <label>
              운영기관
              <input type="text" name="operatorName" value={form.operatorName} placeholder="예: 부산시" onChange={changeValue} />
            </label>
            <label className="full address-field">
              주소 <b>*</b>
              <div>
                <input type="text" name="address" value={form.address} placeholder="다음 주소검색 또는 직접 입력" onChange={changeValue} />
                <button type="button" onClick={openAddressSearch}>다음 주소검색</button>
              </div>
            </label>
            <label>
              위도 <b>*</b>
              <input type="number" step="0.000001" name="latitude" value={form.latitude} placeholder="예: 35.179554" onChange={changeValue} />
            </label>
            <label>
              경도 <b>*</b>
              <input type="number" step="0.000001" name="longitude" value={form.longitude} placeholder="예: 129.075642" onChange={changeValue} />
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

            <div className="full charger-seed-box">
              <div className="admin-panel-title compact-title">
                <div><strong>초기 충전기</strong><p>등록 시 충전소와 함께 충전기도 바로 저장됩니다.</p></div>
                <button type="button" onClick={addCharger}>충전기 추가</button>
              </div>

              {form.chargerList.map((charger, index) => (
                <div className="charger-seed-row" key={`${charger.chargerName}-${index}`}>
                  <input value={charger.chargerName} placeholder="충전기명" onChange={(e) => changeCharger(index, "chargerName", e.target.value)} />
                  <select value={charger.chargerType} onChange={(e) => changeCharger(index, "chargerType", e.target.value)}><option>완속</option><option>급속</option><option>초급속</option></select>
                  <select value={charger.connectorType} onChange={(e) => changeCharger(index, "connectorType", e.target.value)}><option>DC콤보</option><option>AC3상</option><option>NACS</option></select>
                  <input type="number" value={charger.chargingSpeedKw} onChange={(e) => changeCharger(index, "chargingSpeedKw", e.target.value)} />
                  <input type="number" value={charger.pricePerKwh} onChange={(e) => changeCharger(index, "pricePerKwh", e.target.value)} />
                  <button type="button" className="gray" onClick={() => removeCharger(index)} disabled={form.chargerList.length <= 1}>삭제</button>
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
            <p><b>사용자 지도</b> 위도/경도가 저장되면 지도 마커와 예약 화면에서 사용할 수 있습니다.</p>
            <p><b>예약관리</b> 사용자가 해당 충전기를 예약하면 예약 현황에 반영됩니다.</p>
            <p><b>통계분석</b> 충전 완료 세션이 생기면 이용통계와 매출통계에 바로 반영됩니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
};

export default InfrastructurePage;
