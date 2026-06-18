import { useEffect, useMemo, useState } from "react";
import * as adminApi from "../../apis/adminApi";

const mockStations = [
  {
    stationId: 1,
    stationName: "부산시청 공공충전소",
    address: "부산 연제구 중앙대로 1001",
    operatorName: "부산시",
    openTime: "00:00",
    closeTime: "23:59",
    stationStatus: "운영중",
    chargers: [
      {
        chargerId: 101,
        chargerName: "급속 01",
        chargerType: "급속",
        connectorType: "DC콤보",
        chargingSpeedKw: 100,
        pricePerKwh: 320,
        status: "사용가능",
      },
      {
        chargerId: 102,
        chargerName: "급속 02",
        chargerType: "급속",
        connectorType: "DC콤보",
        chargingSpeedKw: 100,
        pricePerKwh: 320,
        status: "사용중",
      },
      {
        chargerId: 103,
        chargerName: "완속 01",
        chargerType: "완속",
        connectorType: "AC3상",
        chargingSpeedKw: 7,
        pricePerKwh: 260,
        status: "점검중",
      },
    ],
  },
  {
    stationId: 2,
    stationName: "해운대 공영주차장 충전소",
    address: "부산 해운대구 해운대로 620",
    operatorName: "환경부",
    openTime: "06:00",
    closeTime: "23:00",
    stationStatus: "운영중",
    chargers: [
      {
        chargerId: 201,
        chargerName: "초급속 01",
        chargerType: "초급속",
        connectorType: "DC콤보",
        chargingSpeedKw: 200,
        pricePerKwh: 360,
        status: "사용가능",
      },
      {
        chargerId: 202,
        chargerName: "급속 01",
        chargerType: "급속",
        connectorType: "NACS",
        chargingSpeedKw: 100,
        pricePerKwh: 340,
        status: "고장",
      },
    ],
  },
  {
    stationId: 3,
    stationName: "센텀시티 공영주차장 충전소",
    address: "부산 해운대구 센텀남대로 35",
    operatorName: "부산시",
    openTime: "00:00",
    closeTime: "23:59",
    stationStatus: "점검중",
    chargers: [
      {
        chargerId: 301,
        chargerName: "완속 01",
        chargerType: "완속",
        connectorType: "AC3상",
        chargingSpeedKw: 7,
        pricePerKwh: 250,
        status: "점검중",
      },
    ],
  },
];

const emptyChargerForm = {
  chargerName: "",
  chargerType: "급속",
  connectorType: "DC콤보",
  chargingSpeedKw: "100",
  pricePerKwh: "320",
  status: "사용가능",
};

const getStatusClass = (status) => {
  if (status === "운영중" || status === "사용가능") {
    return "green";
  }

  if (status === "사용중" || status === "예약중") {
    return "blue";
  }

  if (status === "점검중") {
    return "warning";
  }

  if (status === "고장" || status === "운영중지") {
    return "danger";
  }

  return "";
};

const normalizeStations = (data) => {
  const list = Array.isArray(data) ? data : data?.stations || data?.list || data?.content || [];

  return list.map((station) => ({
    stationId: station.stationId || station.id,
    stationName: station.stationName || station.name,
    address: station.address,
    operatorName: station.operatorName || station.operator,
    openTime: station.openTime || station.open_time || "00:00",
    closeTime: station.closeTime || station.close_time || "23:59",
    stationStatus: station.stationStatus || station.status || "운영중",
    chargers: station.chargers || [],
  }));
};

const AdminStationPage = () => {
  console.log("AdminStationPage 렌더링");

  const [stations, setStations] = useState(mockStations);
  const [selectedStationId, setSelectedStationId] = useState(mockStations[0].stationId);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("전체 상태");
  const [chargerForm, setChargerForm] = useState(emptyChargerForm);
  const [isLoading, setIsLoading] = useState(false);

  const selectedStation = useMemo(() => {
    return stations.find((station) => station.stationId === selectedStationId) || stations[0];
  }, [stations, selectedStationId]);

  const filteredStations = useMemo(() => {
    return stations.filter((station) => {
      const matchStatus = statusFilter === "전체 상태" || station.stationStatus === statusFilter;
      const keyword = searchKeyword.trim().toLowerCase();
      const matchKeyword =
        !keyword ||
        station.stationName.toLowerCase().includes(keyword) ||
        station.address.toLowerCase().includes(keyword) ||
        station.operatorName.toLowerCase().includes(keyword);

      return matchStatus && matchKeyword;
    });
  }, [stations, statusFilter, searchKeyword]);

  const stationSummary = useMemo(() => {
    const chargerList = stations.flatMap((station) => station.chargers || []);

    return {
      stationCount: stations.length,
      chargerCount: chargerList.length,
      availableCount: chargerList.filter((charger) => charger.status === "사용가능").length,
      issueCount: chargerList.filter((charger) => charger.status === "점검중" || charger.status === "고장").length,
    };
  }, [stations]);

  const loadStations = async () => {
    console.log("충전소 운영관리 목록 조회 시작");
    setIsLoading(true);

    try {
      const response = await adminApi.stations();
      const nextStations = normalizeStations(response.data);

      if (nextStations.length > 0) {
        setStations(nextStations);
        setSelectedStationId(nextStations[0].stationId);
      }
    } catch (error) {
      console.log("충전소 운영관리 목록 조회 실패 - Mock 데이터 사용", error);
      setStations(mockStations);
      setSelectedStationId(mockStations[0].stationId);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadStations();
  }, []);

  const changeStationStatus = async (stationId, stationStatus) => {
    console.log("충전소 상태 변경", stationId, stationStatus);

    try {
      await adminApi.updateStationStatus(stationId, { stationStatus });
    } catch (error) {
      console.log("충전소 상태 변경 API 미연동 - 화면 상태만 변경", error);
    }

    setStations((prevStations) =>
      prevStations.map((station) =>
        station.stationId === stationId ? { ...station, stationStatus } : station
      )
    );
  };

  const changeChargerStatus = async (chargerId, status) => {
    console.log("충전기 상태 변경", chargerId, status);

    try {
      await adminApi.updateChargerStatus(chargerId, { status });
    } catch (error) {
      console.log("충전기 상태 변경 API 미연동 - 화면 상태만 변경", error);
    }

    setStations((prevStations) =>
      prevStations.map((station) => ({
        ...station,
        chargers: (station.chargers || []).map((charger) =>
          charger.chargerId === chargerId ? { ...charger, status } : charger
        ),
      }))
    );
  };

  const changeChargerForm = (e) => {
    const { name, value } = e.target;
    console.log("충전기 등록 입력 변경", name, value);

    setChargerForm({
      ...chargerForm,
      [name]: value,
    });
  };

  const registerCharger = async (e) => {
    e.preventDefault();
    console.log("충전기 등록 처리", selectedStation?.stationId, chargerForm);

    if (!selectedStation) {
      alert("충전소를 먼저 선택하세요.");
      return;
    }

    if (!chargerForm.chargerName.trim()) {
      alert("충전기명을 입력하세요.");
      return;
    }

    const requestData = {
      stationId: selectedStation.stationId,
      ...chargerForm,
      chargingSpeedKw: Number(chargerForm.chargingSpeedKw),
      pricePerKwh: Number(chargerForm.pricePerKwh),
    };

    try {
      const response = await adminApi.registerCharger(requestData);
      console.log("충전기 등록 응답", response.data);
    } catch (error) {
      console.log("충전기 등록 API 미연동 - Mock 데이터 추가", error);
    }

    const newCharger = {
      chargerId: Date.now(),
      ...requestData,
    };

    setStations((prevStations) =>
      prevStations.map((station) =>
        station.stationId === selectedStation.stationId
          ? { ...station, chargers: [...(station.chargers || []), newCharger] }
          : station
      )
    );

    setChargerForm(emptyChargerForm);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>충전소관리</p>
          <h1>충전소 운영관리</h1>
          <span>충전소 목록, 상세 정보, 충전기 등록과 상태 변경을 한 화면에서 관리합니다.</span>
        </div>
        <button type="button" onClick={loadStations}>
          새로고침
        </button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card">
          <span>운영 충전소</span>
          <strong>{stationSummary.stationCount}개소</strong>
          <p>전체 등록 충전소</p>
        </article>
        <article className="admin-kpi-card">
          <span>등록 충전기</span>
          <strong>{stationSummary.chargerCount}기</strong>
          <p>충전소 소속 충전기</p>
        </article>
        <article className="admin-kpi-card">
          <span>사용가능</span>
          <strong>{stationSummary.availableCount}기</strong>
          <p>즉시 이용 가능</p>
        </article>
        <article className="admin-kpi-card">
          <span>점검/고장</span>
          <strong>{stationSummary.issueCount}기</strong>
          <p>장애·점검관리 연계 대상</p>
        </article>
      </div>

      <div className="admin-filter-panel">
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option>전체 상태</option>
          <option>운영중</option>
          <option>점검중</option>
          <option>운영중지</option>
        </select>
        <input
          type="text"
          value={searchKeyword}
          placeholder="충전소명, 주소, 운영기관 검색"
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <button type="button" onClick={() => console.log("충전소 검색", { statusFilter, searchKeyword })}>
          검색
        </button>
      </div>

      <div className="admin-grid admin-grid-2-1 station-operation-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전소 목록</strong>
              <p>충전소를 선택하면 우측에서 충전기 목록과 상태를 관리할 수 있습니다.</p>
            </div>
            <span>{isLoading ? "조회 중" : `총 ${filteredStations.length}개소`}</span>
          </div>

          <div className="admin-list selectable">
            {filteredStations.map((station) => (
              <button
                className={`admin-list-row ${selectedStation?.stationId === station.stationId ? "active" : ""}`}
                key={station.stationId}
                type="button"
                onClick={() => {
                  console.log("충전소 선택", station.stationId);
                  setSelectedStationId(station.stationId);
                }}
              >
                <div>
                  <b>{station.stationName}</b>
                  <span>{station.address}</span>
                </div>
                <div className="station-list-right">
                  <em className={`admin-badge ${getStatusClass(station.stationStatus)}`}>
                    {station.stationStatus}
                  </em>
                  <span>{station.chargers?.length || 0}기</span>
                </div>
              </button>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전소 상세</strong>
              <p>운영 상태를 변경하고 장애·점검관리 흐름과 연결합니다.</p>
            </div>
          </div>

          {selectedStation && (
            <div className="admin-detail-box">
              <h3>{selectedStation.stationName}</h3>
              <p>{selectedStation.address}</p>
              <dl>
                <div>
                  <dt>운영기관</dt>
                  <dd>{selectedStation.operatorName}</dd>
                </div>
                <div>
                  <dt>운영시간</dt>
                  <dd>{selectedStation.openTime} ~ {selectedStation.closeTime}</dd>
                </div>
                <div>
                  <dt>충전기 수</dt>
                  <dd>{selectedStation.chargers?.length || 0}기</dd>
                </div>
                <div>
                  <dt>충전소 상태</dt>
                  <dd>
                    <select
                      value={selectedStation.stationStatus}
                      onChange={(e) => changeStationStatus(selectedStation.stationId, e.target.value)}
                    >
                      <option>운영중</option>
                      <option>점검중</option>
                      <option>운영중지</option>
                    </select>
                  </dd>
                </div>
              </dl>
            </div>
          )}
        </article>
      </div>

      <div className="admin-grid admin-grid-2-1 station-operation-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전기 목록</strong>
              <p>선택한 충전소에 설치된 충전기의 타입, 커넥터, 출력, 상태를 관리합니다.</p>
            </div>
            <span>{selectedStation?.chargers?.length || 0}기</span>
          </div>

          <div className="admin-table-wrap">
            <table className="admin-table charger-table">
              <thead>
                <tr>
                  <th>충전기명</th>
                  <th>유형</th>
                  <th>커넥터</th>
                  <th>출력</th>
                  <th>요금</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {(selectedStation?.chargers || []).map((charger) => (
                  <tr key={charger.chargerId}>
                    <td>{charger.chargerName}</td>
                    <td>{charger.chargerType}</td>
                    <td>{charger.connectorType}</td>
                    <td>{charger.chargingSpeedKw}kW</td>
                    <td>{Number(charger.pricePerKwh).toLocaleString()}원/kWh</td>
                    <td>
                      <em className={`admin-badge ${getStatusClass(charger.status)}`}>
                        {charger.status}
                      </em>
                    </td>
                    <td>
                      <select value={charger.status} onChange={(e) => changeChargerStatus(charger.chargerId, e.target.value)}>
                        <option>사용가능</option>
                        <option>예약중</option>
                        <option>사용중</option>
                        <option>점검중</option>
                        <option>고장</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전기 등록</strong>
              <p>선택한 충전소에 신규 충전기를 추가합니다.</p>
            </div>
          </div>

          <form className="admin-form-grid charger-register-form" onSubmit={registerCharger}>
            <label className="full">
              충전기명
              <input
                type="text"
                name="chargerName"
                value={chargerForm.chargerName}
                placeholder="예: 급속 03"
                onChange={changeChargerForm}
              />
            </label>
            <label>
              충전기 유형
              <select name="chargerType" value={chargerForm.chargerType} onChange={changeChargerForm}>
                <option>완속</option>
                <option>급속</option>
                <option>초급속</option>
              </select>
            </label>
            <label>
              커넥터
              <select name="connectorType" value={chargerForm.connectorType} onChange={changeChargerForm}>
                <option>AC3상</option>
                <option>DC콤보</option>
                <option>NACS</option>
              </select>
            </label>
            <label>
              출력(kW)
              <input
                type="number"
                name="chargingSpeedKw"
                value={chargerForm.chargingSpeedKw}
                onChange={changeChargerForm}
              />
            </label>
            <label>
              요금(원/kWh)
              <input
                type="number"
                name="pricePerKwh"
                value={chargerForm.pricePerKwh}
                onChange={changeChargerForm}
              />
            </label>
            <label className="full">
              초기 상태
              <select name="status" value={chargerForm.status} onChange={changeChargerForm}>
                <option>사용가능</option>
                <option>예약중</option>
                <option>사용중</option>
                <option>점검중</option>
                <option>고장</option>
              </select>
            </label>
            <div className="admin-action-row full">
              <button type="submit">충전기 등록</button>
            </div>
          </form>

          <div className="admin-guide-list mt">
            <p><b>점검중/고장</b> 상태는 장애·점검관리에서 담당자 배정과 점검 이력으로 이어집니다.</p>
            <p><b>부품 교체</b>가 필요하면 전자결재 상신 후 승인된 작업만 조치 완료 처리합니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
};

export default AdminStationPage;
