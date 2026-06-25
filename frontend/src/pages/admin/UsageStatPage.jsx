import { useEffect, useState } from "react";
import * as adminApi from "../../apis/adminApi";

const today = new Date();
const defaultEnd = today.toISOString().slice(0, 10);
const defaultStart = new Date(today.getFullYear(), today.getMonth(), 1).toISOString().slice(0, 10);

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");

const UsageStatPage = () => {
  console.log("UsageStatPage 렌더링");

  const [filter, setFilter] = useState({ startDate: defaultStart, endDate: defaultEnd, region: "", chargerType: "" });
  const [data, setData] = useState({ summary: {}, dailyList: [], hourlyList: [], typeList: [], stationRankList: [] });
  const [loading, setLoading] = useState(false);

  const loadStats = async () => {
    console.log("이용 통계 실제 조회", filter);
    setLoading(true);
    try {
      const response = await adminApi.usageStatistics(filter);
      console.log("이용 통계 응답", response.data);
      setData(response.data || {});
    } catch (error) {
      console.log("이용 통계 조회 실패", error);
      alert("이용 통계를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadStats(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, []);

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log("이용 통계 필터 변경", name, value);
    setFilter((prev) => ({ ...prev, [name]: value }));
  };

  const submitFilter = (e) => { e.preventDefault(); loadStats(); };

  const createSampleData = async () => {
    console.log("이용 통계 샘플 생성");
    setLoading(true);
    try {
      const response = await adminApi.generateStatisticsSampleData({ days: 90, count: 500 });
      console.log("통계 샘플 생성 응답", response.data);
      alert(response.data?.message || "통계 샘플 데이터가 생성되었습니다.");
      await loadStats();
    } catch (error) {
      console.log("통계 샘플 생성 실패", error);
      alert("통계 샘플 생성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const summary = data.summary || {};

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div><p>통계/분석</p><h1>이용 통계</h1><span>실제 charging_session 완료 데이터를 기준으로 이용 현황을 집계합니다.</span></div>
        <button type="button" onClick={createSampleData} disabled={loading}>{loading ? "처리 중" : "이용/매출 통계 샘플 생성"}</button>
      </div>

      <form className="admin-filter-panel polished" onSubmit={submitFilter}>
        <label><span>시작일</span><input type="date" name="startDate" value={filter.startDate} onChange={changeFilter} /></label>
        <label><span>종료일</span><input type="date" name="endDate" value={filter.endDate} onChange={changeFilter} /></label>
        <label><span>지역</span><input name="region" value={filter.region} placeholder="예: 부산" onChange={changeFilter} /></label>
        <label><span>충전기 유형</span><select name="chargerType" value={filter.chargerType} onChange={changeFilter}><option value="">전체</option><option>완속</option><option>급속</option><option>초급속</option></select></label>
        <div className="filter-actions"><button type="submit">조회</button></div>
      </form>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>총 이용 건수</span><strong>{numberText(summary.totalUsageCount)}건</strong><p>완료 세션 기준</p></article>
        <article className="admin-kpi-card"><span>오늘 이용</span><strong>{numberText(summary.todayUsageCount)}건</strong><p>current_date 기준</p></article>
        <article className="admin-kpi-card"><span>평균 충전시간</span><strong>{numberText(summary.avgChargingMinutes)}분</strong><p>actual_minutes 평균</p></article>
        <article className="admin-kpi-card"><span>총 충전량</span><strong>{numberText(summary.totalKwh)}kWh</strong><p>actual_kwh 합계</p></article>
      </div>

      <div className="admin-grid">
        <article className="admin-panel"><div className="admin-panel-title"><div><strong>일별 이용 현황</strong><p>기간 내 날짜별 충전 완료 건수입니다.</p></div></div><table className="admin-table compact"><thead><tr><th>일자</th><th>이용 건수</th></tr></thead><tbody>{(data.dailyList || []).map((row) => <tr key={row.usageDate}><td>{row.usageDate}</td><td>{numberText(row.usageCount)}건</td></tr>)}{(data.dailyList || []).length === 0 && <tr><td colSpan="2">데이터가 없습니다.</td></tr>}</tbody></table></article>
        <article className="admin-panel"><div className="admin-panel-title"><div><strong>시간대별 이용 현황</strong><p>충전 시작 시간 기준입니다.</p></div></div><table className="admin-table compact"><thead><tr><th>시간대</th><th>이용 건수</th></tr></thead><tbody>{(data.hourlyList || []).map((row) => <tr key={row.usageHour}><td>{row.usageHour}시</td><td>{numberText(row.usageCount)}건</td></tr>)}{(data.hourlyList || []).length === 0 && <tr><td colSpan="2">데이터가 없습니다.</td></tr>}</tbody></table></article>
      </div>

      <div className="admin-grid">
        <article className="admin-panel"><div className="admin-panel-title"><div><strong>충전기 유형별 이용</strong><p>완속/급속/초급속 기준 이용률입니다.</p></div></div><table className="admin-table compact"><thead><tr><th>유형</th><th>이용</th><th>비율</th><th>평균시간</th></tr></thead><tbody>{(data.typeList || []).map((row) => <tr key={row.chargerType}><td>{row.chargerType}</td><td>{numberText(row.usageCount)}건</td><td>{row.usageRate || 0}%</td><td>{numberText(row.avgMinutes)}분</td></tr>)}{(data.typeList || []).length === 0 && <tr><td colSpan="4">데이터가 없습니다.</td></tr>}</tbody></table></article>
        <article className="admin-panel"><div className="admin-panel-title"><div><strong>충전소 이용 순위</strong><p>상위 10개 충전소입니다.</p></div></div><table className="admin-table compact"><thead><tr><th>순위</th><th>충전소</th><th>이용</th><th>비율</th></tr></thead><tbody>{(data.stationRankList || []).map((row) => <tr key={row.stationId}><td>{row.rankNo}</td><td>{row.stationName}</td><td>{numberText(row.usageCount)}건</td><td>{row.operationRate || 0}%</td></tr>)}{(data.stationRankList || []).length === 0 && <tr><td colSpan="4">데이터가 없습니다.</td></tr>}</tbody></table></article>
      </div>
    </section>
  );
};

export default UsageStatPage;
