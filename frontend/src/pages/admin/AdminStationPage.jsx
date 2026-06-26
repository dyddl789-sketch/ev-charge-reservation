import { useEffect, useMemo, useState } from "react";
import * as adminApi from "../../apis/adminApi";

const initialSearch = {
  region: "",
  stationStatus: "",
  chargerType: "",
  searchType: "all",
  keyword: "",
};

const emptyChargerForm = {
  chargerName: "",
  chargerType: "급속",
  connectorType: "DC콤보",
  chargingSpeedKw: "100",
  pricePerKwh: "320",
  status: "사용가능",
};

const regionOptions = [
  "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주",
];

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");

const getStatusClass = (status) => {
  if (status === "운영중" || status === "사용가능") return "green";
  if (status === "사용중" || status === "예약중") return "blue";
  if (status === "점검중") return "warning";
  if (status === "고장" || status === "운영중지") return "danger";
  return "";
};

const normalizeStation = (station) => ({
  stationId: station.stationId,
  stationName: station.stationName ?? "-",
  address: station.address ?? "-",
  operatorName: station.operatorName ?? "-",
  openTime: station.openTime ?? "00:00",
  closeTime: station.closeTime ?? "23:59",
  stationStatus: station.stationStatus ?? "운영중",
  chargerCount: station.chargerCount ?? 0,
  availableChargerCount: station.availableChargerCount ?? 0,
  checkChargerCount: station.checkChargerCount ?? 0,
  brokenChargerCount: station.brokenChargerCount ?? 0,
  chargerTypes: station.chargerTypes ?? "",
  chargers: station.chargerList || station.chargers || [],
});

const AdminStationPage = () => {
  console.log("AdminStationPage 렌더링");

  const [stations, setStations] = useState([]);
  const [selectedStation, setSelectedStation] = useState(null);
  const [pageInfo, setPageInfo] = useState({ page: 1, size: 10, totalPage: 1, searchCount: 0 });
  const [summary, setSummary] = useState({ totalCount: 0, activeCount: 0, checkCount: 0 });
  const [search, setSearch] = useState(initialSearch);
  const [page, setPage] = useState(1);
  const [chargerForm, setChargerForm] = useState(emptyChargerForm);
  const [loading, setLoading] = useState(false);

  const params = useMemo(() => ({
    region: search.region || undefined,
    stationStatus: search.stationStatus || undefined,
    chargerType: search.chargerType || undefined,
    searchType: search.searchType || undefined,
    keyword: search.keyword || undefined,
    page,
    size: 10,
  }), [search, page]);

  const loadStations = async () => {
    console.log("충전소 운영관리 실제 조회", params);
    setLoading(true);

    try {
      const response = await adminApi.stations(params);
      console.log("충전소 목록 응답", response.data);

      const data = response.data || {};
      const rows = (data.stationList || []).map(normalizeStation);

      setStations(rows);
      setSummary({
        totalCount: data.totalCount ?? 0,
        activeCount: data.activeCount ?? 0,
        checkCount: data.checkCount ?? 0,
      });
      setPageInfo({
        page: data.page ?? page,
        size: data.size ?? 10,
        totalPage: data.totalPage ?? 1,
        searchCount: data.searchCount ?? rows.length,
      });

      if (rows.length > 0) {
        await selectStation(rows[0].stationId, rows[0]);
      } else {
        setSelectedStation(null);
      }
    } catch (error) {
      console.log("충전소 목록 조회 실패", error);
      alert("충전소 목록을 불러오지 못했습니다. 권한과 백엔드 실행 상태를 확인해 주세요.");
      setStations([]);
      setSelectedStation(null);
    } finally {
      setLoading(false);
    }
  };

  const selectStation = async (stationId, fallbackStation = null) => {
    console.log("충전소 상세 선택", stationId);

    try {
      const response = await adminApi.stationDetail(stationId);
      console.log("충전소 상세 응답", response.data);
      setSelectedStation(normalizeStation(response.data));
    } catch (error) {
      console.log("충전소 상세 조회 실패", error);
      setSelectedStation(fallbackStation);
    }
  };

  useEffect(() => {
    loadStations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const changeSearch = (e) => {
    const { name, value } = e.target;
    console.log("충전소 검색 조건 변경", name, value);
    setSearch((prev) => ({ ...prev, [name]: value }));
  };

  const submitSearch = (e) => {
    e.preventDefault();
    console.log("충전소 검색 실행", search);
    if (page !== 1) {
      setPage(1);
      return;
    }
    loadStations();
  };

  const resetSearch = () => {
    console.log("충전소 검색 초기화");
    setSearch(initialSearch);
    setPage(1);
  };

  const changeStationStatus = async (stationId, stationStatus) => {
    console.log("충전소 상태 변경", stationId, stationStatus);

    try {
      await adminApi.updateStationStatus(stationId, stationStatus);
      await loadStations();
    } catch (error) {
      console.log("충전소 상태 변경 실패", error);
      alert("충전소 상태 변경에 실패했습니다.");
    }
  };

  const changeChargerStatus = async (chargerId, status) => {
    console.log("충전기 상태 변경", chargerId, status);

    try {
      await adminApi.updateChargerStatus(chargerId, status);
      if (selectedStation) await selectStation(selectedStation.stationId, selectedStation);
      await loadStations();
    } catch (error) {
      console.log("충전기 상태 변경 실패", error);
      alert("충전기 상태 변경에 실패했습니다.");
    }
  };

  const changeChargerForm = (e) => {
    const { name, value } = e.target;
    console.log("충전기 등록 입력 변경", name, value);
    setChargerForm((prev) => ({ ...prev, [name]: value }));
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
      await adminApi.registerCharger(requestData);
      alert("충전기가 등록되었습니다.");
      setChargerForm(emptyChargerForm);
      await selectStation(selectedStation.stationId, selectedStation);
      await loadStations();
    } catch (error) {
      console.log("충전기 등록 실패", error);
      alert("충전기 등록에 실패했습니다.");
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>충전소관리</p>
          <h1>충전소 운영관리</h1>
          <span>공공데이터 또는 직접 등록된 실제 충전소·충전기 데이터를 지역별로 조회하고 상태를 관리합니다.</span>
        </div>
        <button type="button" onClick={loadStations} disabled={loading}>{loading ? "조회 중" : "새로고침"}</button>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 충전소</span><strong>{numberText(summary.totalCount)}개소</strong><p>charging_station 기준</p></article>
        <article className="admin-kpi-card"><span>운영중</span><strong>{numberText(summary.activeCount)}개소</strong><p>정상 운영 충전소</p></article>
        <article className="admin-kpi-card"><span>점검중</span><strong>{numberText(summary.checkCount)}개소</strong><p>운영 확인 필요</p></article>
        <article className="admin-kpi-card"><span>검색 결과</span><strong>{numberText(pageInfo.searchCount)}개소</strong><p>현재 조건 기준</p></article>
      </div>

      <form className="admin-filter-panel polished" onSubmit={submitSearch}>
        <label>
          <span>지역</span>
          <select name="region" value={search.region} onChange={changeSearch}>
            <option value="">전체 지역</option>
            {regionOptions.map((region) => <option key={region} value={region}>{region}</option>)}
          </select>
        </label>
        <label>
          <span>충전소 상태</span>
          <select name="stationStatus" value={search.stationStatus} onChange={changeSearch}>
            <option value="">전체 상태</option>
            <option value="운영중">운영중</option>
            <option value="점검중">점검중</option>
            <option value="운영중지">운영중지</option>
          </select>
        </label>
        <label>
          <span>충전기 유형</span>
          <select name="chargerType" value={search.chargerType} onChange={changeSearch}>
            <option value="">전체 유형</option>
            <option value="완속">완속</option>
            <option value="급속">급속</option>
            <option value="초급속">초급속</option>
          </select>
        </label>
        <label>
          <span>검색 기준</span>
          <select name="searchType" value={search.searchType} onChange={changeSearch}>
            <option value="all">전체</option>
            <option value="name">충전소명</option>
            <option value="address">주소</option>
            <option value="operator">운영기관</option>
          </select>
        </label>
        <label className="wide">
          <span>검색어</span>
          <input type="text" name="keyword" value={search.keyword} placeholder="충전소명, 주소, 운영기관 검색" onChange={changeSearch} />
        </label>
        <div className="filter-actions">
          <button type="submit">검색</button>
          <button type="button" className="gray" onClick={resetSearch}>초기화</button>
        </div>
      </form>

      <div className="admin-grid admin-grid-2-1 station-operation-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>충전소 리스트</strong>
              <p>10개 단위 페이지네이션 · 현재 {pageInfo.page} / {pageInfo.totalPage} 페이지</p>
            </div>
          </div>

          <div className="admin-list selectable station-card-list">
            {stations.map((station) => (
              <button
                type="button"
                className={`admin-list-row station-card-row ${selectedStation?.stationId === station.stationId ? "active" : ""}`}
                key={station.stationId}
                onClick={() => selectStation(station.stationId, station)}
              >
                <div>
                  <b>{station.stationName}</b>
                  <span>{station.address}</span>
                  <small>{station.operatorName} · {station.openTime}~{station.closeTime}</small>
                </div>
                <div className="station-card-metrics">
                  <em className={`admin-badge ${getStatusClass(station.stationStatus)}`}>{station.stationStatus}</em>
                  <span>{numberText(station.availableChargerCount)} / {numberText(station.chargerCount)}기 사용가능</span>
                </div>
              </button>
            ))}
            {!loading && stations.length === 0 && <p className="admin-empty-text">조회된 충전소가 없습니다.</p>}
            {loading && <p className="admin-empty-text">충전소 데이터를 불러오는 중입니다.</p>}
          </div>

          <div className="admin-pagination">
            <button type="button" disabled={pageInfo.page <= 1 || loading} onClick={() => setPage((prev) => Math.max(1, prev - 1))}>이전</button>
            <span>{pageInfo.page} / {pageInfo.totalPage}</span>
            <button type="button" disabled={pageInfo.page >= pageInfo.totalPage || loading} onClick={() => setPage((prev) => prev + 1)}>다음</button>
          </div>
        </article>

        <article className="admin-panel station-detail-panel">
          {selectedStation ? (
            <>
              <div className="admin-panel-title">
                <div>
                  <strong>{selectedStation.stationName}</strong>
                  <p>{selectedStation.address}</p>
                </div>
                <em className={`admin-badge ${getStatusClass(selectedStation.stationStatus)}`}>{selectedStation.stationStatus}</em>
              </div>

              <div className="admin-info-grid compact">
                <article><span>운영기관</span><strong>{selectedStation.operatorName}</strong></article>
                <article><span>운영시간</span><strong>{selectedStation.openTime}~{selectedStation.closeTime}</strong></article>
                <article><span>전체 충전기</span><strong>{numberText(selectedStation.chargerList?.length || selectedStation.chargers?.length || selectedStation.chargerCount)}기</strong></article>
                <article><span>사용가능</span><strong>{numberText(selectedStation.availableChargerCount)}기</strong></article>
              </div>

              <div className="admin-action-row wrap">
                {['운영중', '점검중', '운영중지'].map((status) => (
                  <button key={status} type="button" className={selectedStation.stationStatus === status ? "active" : "gray"} onClick={() => changeStationStatus(selectedStation.stationId, status)}>{status}</button>
                ))}
              </div>

              <h3 className="admin-small-title">충전기 목록</h3>
              <div className="charger-management-list">
                {(selectedStation.chargerList || selectedStation.chargers || []).map((charger) => (
                  <div className="charger-management-card" key={charger.chargerId}>
                    <div>
                      <b>{charger.chargerName}</b>
                      <span>{charger.chargerType} · {charger.connectorType} · {charger.chargingSpeedKw}kW · {numberText(charger.pricePerKwh)}원/kWh</span>
                    </div>
                    <select value={charger.status} onChange={(e) => changeChargerStatus(charger.chargerId, e.target.value)}>
                      <option>사용가능</option>
                      <option>예약중</option>
                      <option>사용중</option>
                      <option>점검중</option>
                      <option>고장</option>
                    </select>
                  </div>
                ))}
                {(selectedStation.chargerList || selectedStation.chargers || []).length === 0 && <p className="admin-empty-text">등록된 충전기가 없습니다.</p>}
              </div>

              <form className="admin-form-grid compact-form" onSubmit={registerCharger}>
                <label className="full">충전기명<input name="chargerName" value={chargerForm.chargerName} onChange={changeChargerForm} placeholder="예: 급속 01" /></label>
                <label>유형<select name="chargerType" value={chargerForm.chargerType} onChange={changeChargerForm}><option>완속</option><option>급속</option><option>초급속</option></select></label>
                <label>커넥터<select name="connectorType" value={chargerForm.connectorType} onChange={changeChargerForm}><option>DC콤보</option><option>AC3상</option><option>NACS</option></select></label>
                <label>출력(kW)<input type="number" name="chargingSpeedKw" value={chargerForm.chargingSpeedKw} onChange={changeChargerForm} /></label>
                <label>요금<input type="number" name="pricePerKwh" value={chargerForm.pricePerKwh} onChange={changeChargerForm} /></label>
                <div className="admin-action-row full"><button type="submit">충전기 등록</button></div>
              </form>
            </>
          ) : (
            <p className="admin-empty-text">충전소를 선택하면 상세 정보와 충전기 목록이 표시됩니다.</p>
          )}
        </article>
      </div>
    </section>
  );
};

export default AdminStationPage;
