import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";
import useAdminPolling from "../../hooks/useAdminPolling";

const today = new Date().toISOString().slice(0, 10);
const initialSearch = { status: "", searchType: "all", keyword: "", startDate: "", endDate: "", startTime: "", endTime: "" };

const statusColor = (status) => {
  if (status === "예약완료") return "warning";
  if (status === "인증완료" || status === "충전중") return "blue";
  if (status === "완료") return "green";
  if (status === "취소" || status === "노쇼") return "danger";
  return "";
};

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");
const moneyText = (value) => `${numberText(value)}원`;
const formatDateTime = (value) => (!value ? "-" : String(value).replace("T", " ").slice(0, 16));

const normalizeReservation = (item) => ({
  reservationId: item.reservationId,
  reservationNo: item.reservationNo || `RSV-${String(item.reservationId).padStart(6, "0")}`,
  memberName: item.memberName || "-",
  userId: item.userId || "-",
  vehicleName: item.vehicleName || "-",
  stationName: item.stationName || "-",
  stationAddress: item.stationAddress || "-",
  chargerName: item.chargerName || "-",
  startTimeText: item.startTimeText || formatDateTime(item.startTime),
  endTimeText: item.endTimeText || formatDateTime(item.endTime),
  currentSoc: item.currentSoc ?? 0,
  targetSoc: item.targetSoc ?? 0,
  estimatedMinutes: item.estimatedMinutes ?? 0,
  estimatedCost: item.estimatedCost ?? 0,
  authCode: item.authCode || "-",
  status: item.status || "예약완료",
});

const AdminReservationPage = () => {
  console.log("AdminReservationPage 렌더링");

  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [summary, setSummary] = useState({});
  const [issue, setIssue] = useState({});
  const [pageInfo, setPageInfo] = useState({ page: 1, size: 10, totalPage: 1, totalCount: 0 });
  const [search, setSearch] = useState(initialSearch);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const isFirstSearchChangeRef = useRef(true);

  const params = useMemo(() => ({
    status: search.status || undefined,
    searchType: search.searchType || undefined,
    keyword: search.keyword || undefined,
    startDate: search.startDate || undefined,
    endDate: search.endDate || undefined,
    startTime: search.startTime || undefined,
    endTime: search.endTime || undefined,
    page,
    size: 10,
  }), [search, page]);
  const searchKey = useMemo(() => JSON.stringify(search), [search]);

  const loadReservations = async () => {
    console.log("예약관리 실제 조회", params);
    setLoading(true);
    try {
      const response = await adminApi.reservations(params);
      console.log("예약관리 응답", response.data);
      const data = response.data || {};
      const rows = (data.reservationList || []).map(normalizeReservation);
      setReservations(rows);
      setSummary(data.summary || {});
      setIssue(data.issue || {});
      setPageInfo({ page: data.page ?? page, size: data.size ?? 10, totalPage: data.totalPage ?? 1, totalCount: data.totalCount ?? rows.length });
    } catch (error) {
      console.log("예약관리 조회 실패", error);
      alert("예약 목록을 불러오지 못했습니다.");
      setReservations([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadReservations(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [page]);

  useEffect(() => {
    if (isFirstSearchChangeRef.current) {
      isFirstSearchChangeRef.current = false;
      return undefined;
    }

    const timer = setTimeout(() => {
      console.log('예약 검색 조건 변경 즉시 재조회', search);
      if (page !== 1) {
        setPage(1);
        return;
      }
      loadReservations();
    }, 400);

    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchKey]);

  useAdminPolling(loadReservations, { label: '예약관리' });

  const changeSearch = (e) => {
    const { name, value } = e.target;
    console.log("예약 검색 조건 변경", name, value);
    setSearch((prev) => ({ ...prev, [name]: value }));
  };

  const submitSearch = (e) => {
    e.preventDefault();
    console.log("예약 검색 실행", search);
    if (page !== 1) { setPage(1); return; }
    loadReservations();
  };

  const resetSearch = () => {
    console.log("예약 검색 초기화");
    setSearch(initialSearch);
    setPage(1);
  };

  const goDetail = (reservationId) => {
    console.log("예약 상세 이동", reservationId);
    navigate(`/admin/reservations/${reservationId}`);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div><p>예약관리</p><h1>예약 현황</h1><span>실제 예약 데이터를 10개 단위로 조회하고 회원·예약시간 기준으로 검색합니다.</span></div>
        <button type="button" onClick={loadReservations} disabled={loading}>{loading ? "조회 중" : "새로고침"}</button>
      </div>

      <div className="admin-kpi-grid five">
        <article className="admin-kpi-card"><span>전체 예약</span><strong>{numberText(summary.totalCount)}</strong><p>현재 조건 기준</p></article>
        <article className="admin-kpi-card"><span>예약완료</span><strong>{numberText(summary.reservedCount)}</strong><p>인증 대기</p></article>
        <article className="admin-kpi-card"><span>충전중</span><strong>{numberText(summary.chargingCount)}</strong><p>현재 진행</p></article>
        <article className="admin-kpi-card"><span>완료</span><strong>{numberText(summary.completedCount)}</strong><p>통계 반영</p></article>
        <article className="admin-kpi-card"><span>취소/노쇼</span><strong>{numberText((summary.canceledCount || 0) + (summary.noShowCount || 0))}</strong><p>관리 필요</p></article>
      </div>

      <div className="admin-mini-kpi-row">
        <span>오늘 인증대기 {numberText(issue.waitingAuthCount)}건</span>
        <span>오늘 취소 {numberText(issue.cancelRequestCount)}건</span>
        <span>노쇼 예상 {numberText(issue.expectedNoShowCount)}건</span>
        <span>시작 지연 {numberText(issue.delayedStartCount)}건</span>
      </div>

      <form className="admin-filter-panel polished" onSubmit={submitSearch}>
        <label><span>상태</span><select name="status" value={search.status} onChange={changeSearch}><option value="">전체 상태</option><option>예약완료</option><option>인증완료</option><option>충전중</option><option>완료</option><option>취소</option><option>노쇼</option></select></label>
        <label><span>시작일</span><input type="date" name="startDate" value={search.startDate} onChange={changeSearch} /></label>
        <label><span>종료일</span><input type="date" name="endDate" value={search.endDate} onChange={changeSearch} max={today} /></label>
        <label><span>시작시간</span><input type="time" name="startTime" value={search.startTime} onChange={changeSearch} /></label>
        <label><span>종료시간</span><input type="time" name="endTime" value={search.endTime} onChange={changeSearch} /></label>
        <label><span>검색 기준</span><select name="searchType" value={search.searchType} onChange={changeSearch}><option value="all">전체</option><option value="memberName">회원명</option><option value="userId">회원 아이디</option><option value="stationName">충전소명</option><option value="vehicleName">차량명</option></select></label>
        <label className="wide"><span>검색어</span><input name="keyword" value={search.keyword} placeholder="회원명, 아이디, 충전소명, 차량명 검색" onChange={changeSearch} /></label>
        <div className="filter-actions"><button type="submit">검색</button><button type="button" className="gray" onClick={resetSearch}>초기화</button></div>
      </form>

      <div className="admin-panel">
        <div className="admin-panel-title"><div><strong>예약 리스트</strong><p>10개 초과 시 다음 페이지로 이동합니다. 현재 {pageInfo.page} / {pageInfo.totalPage} 페이지</p></div></div>
        <div className="admin-table-wrap elegant-table-wrap">
          <table className="admin-table elegant-table">
            <thead><tr><th>예약번호</th><th>회원</th><th>충전소/충전기</th><th>예약시간</th><th>SOC</th><th>예상</th><th>인증코드</th><th>상태</th><th>관리</th></tr></thead>
            <tbody>
              {reservations.map((item) => (
                <tr key={item.reservationId} className="admin-clickable-row" onClick={() => goDetail(item.reservationId)}>
                  <td>{item.reservationNo}</td>
                  <td><b>{item.memberName}</b><span>{item.userId}</span></td>
                  <td><b>{item.stationName}</b><span>{item.chargerName}</span></td>
                  <td><b>{item.startTimeText}</b><span>{item.endTimeText}</span></td>
                  <td>{item.currentSoc}% → {item.targetSoc}%</td>
                  <td><b>{numberText(item.estimatedMinutes)}분</b><span>{moneyText(item.estimatedCost)}</span></td>
                  <td>{item.authCode}</td>
                  <td><em className={`admin-badge ${statusColor(item.status)}`}>{item.status}</em></td>
                  <td><button type="button" onClick={(e) => { e.stopPropagation(); goDetail(item.reservationId); }}>상세</button></td>
                </tr>
              ))}
              {!loading && reservations.length === 0 && <tr><td colSpan="9">조회된 예약이 없습니다.</td></tr>}
              {loading && <tr><td colSpan="9">예약 데이터를 불러오는 중입니다.</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="admin-pagination"><button type="button" disabled={pageInfo.page <= 1 || loading} onClick={() => setPage((prev) => Math.max(1, prev - 1))}>이전</button><span>{pageInfo.page} / {pageInfo.totalPage}</span><button type="button" disabled={pageInfo.page >= pageInfo.totalPage || loading} onClick={() => setPage((prev) => prev + 1)}>다음</button></div>
      </div>
    </section>
  );
};

export default AdminReservationPage;
