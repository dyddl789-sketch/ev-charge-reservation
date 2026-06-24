import { useEffect, useMemo, useState } from "react";
import * as adminApi from "../../apis/adminApi";

// 날짜 기본값
const DEFAULT_START_DATE = "2026-06-01";
const DEFAULT_END_DATE = "2026-06-30";

// 필터 선택값
const regionOptions = ["전체", "부산진구", "해운대구", "동구", "수영구", "사상구"];
const stationOptions = ["전체", "부산역 공공충전소", "해운대 구청 충전소", "서면 환승센터 충전소", "광안리 해변 충전소", "사상 공영주차장 충전소"];
const chargerTypeOptions = ["전체", "완속", "급속", "초급속"];

// 백엔드 REST API 연결 전 화면 확인용 Mock 데이터
const mockUsageData = {
  summary: {
    totalCount: 1284,
    totalKwh: 42580.6,
    averageMinutes: 46,
    averageAmount: 96962,
  },
  monthlyTrend: [
    { month: "2025-07", count: 720 },
    { month: "2025-08", count: 760 },
    { month: "2025-09", count: 810 },
    { month: "2025-10", count: 850 },
    { month: "2025-11", count: 870 },
    { month: "2025-12", count: 890 },
    { month: "2026-01", count: 820 },
    { month: "2026-02", count: 910 },
    { month: "2026-03", count: 1030 },
    { month: "2026-04", count: 1120 },
    { month: "2026-05", count: 1195 },
    { month: "2026-06", count: 1284 },
  ],
  stationTop: [
    { rank: 1, stationName: "부산역 공공충전소", region: "동구", count: 218, totalKwh: 7140.8, ratio: 17.0 },
    { rank: 2, stationName: "해운대 구청 충전소", region: "해운대구", count: 184, totalKwh: 5960.2, ratio: 14.3 },
    { rank: 3, stationName: "서면 환승센터 충전소", region: "부산진구", count: 162, totalKwh: 5310.5, ratio: 12.6 },
    { rank: 4, stationName: "광안리 해변 충전소", region: "수영구", count: 141, totalKwh: 4612.3, ratio: 11.0 },
    { rank: 5, stationName: "사상 공영주차장 충전소", region: "사상구", count: 117, totalKwh: 3891.4, ratio: 9.1 },
  ],
  regionRanking: [
    { rank: 1, region: "부산진구", count: 342, totalKwh: 11240.4, ratio: 26.6 },
    { rank: 2, region: "해운대구", count: 276, totalKwh: 9260.3, ratio: 21.5 },
    { rank: 3, region: "동구", count: 218, totalKwh: 7140.8, ratio: 17.0 },
    { rank: 4, region: "수영구", count: 184, totalKwh: 6120.1, ratio: 14.3 },
    { rank: 5, region: "사상구", count: 117, totalKwh: 3891.4, ratio: 9.1 },
  ],
  chargerTypeSummary: [
    { chargerType: "완속", count: 462, totalKwh: 15120.7, ratio: 36.0, averageMinutes: 68 },
    { chargerType: "급속", count: 618, totalKwh: 20540.8, ratio: 48.1, averageMinutes: 34 },
    { chargerType: "초급속", count: 204, totalKwh: 6919.1, ratio: 15.9, averageMinutes: 19 },
  ],
  detailRows: [
    { id: 1, date: "2026-06-30 20:12", region: "동구", stationName: "부산역 공공충전소", chargerName: "부산역 급속 01", chargerType: "급속", memberName: "김성민", kwh: 42.1, minutes: 37, amount: 12630, status: "완료" },
    { id: 2, date: "2026-06-30 19:40", region: "해운대구", stationName: "해운대 구청 충전소", chargerName: "해운대 완속 02", chargerType: "완속", memberName: "시연회원12", kwh: 38.4, minutes: 72, amount: 10368, status: "완료" },
    { id: 3, date: "2026-06-30 18:25", region: "부산진구", stationName: "서면 환승센터 충전소", chargerName: "서면 초급속 01", chargerType: "초급속", memberName: "시연회원09", kwh: 51.7, minutes: 18, amount: 19646, status: "완료" },
    { id: 4, date: "2026-06-30 17:05", region: "수영구", stationName: "광안리 해변 충전소", chargerName: "광안리 급속 02", chargerType: "급속", memberName: "시연회원03", kwh: 28.6, minutes: 26, amount: 8580, status: "완료" },
    { id: 5, date: "2026-06-30 16:22", region: "사상구", stationName: "사상 공영주차장 충전소", chargerName: "사상 완속 01", chargerType: "완속", memberName: "시연회원21", kwh: 33.8, minutes: 65, amount: 9126, status: "완료" },
  ],
};

const numberFormat = (value) => Number(value || 0).toLocaleString("ko-KR");
const moneyFormat = (value) => `${numberFormat(value)}원`;
const compactMonth = (value) => String(value || "").replace("2025-", "").replace("2026-", "") + "월";

const normalizeUsageData = (data) => {
  console.log("이용 통계 데이터 정규화", data);

  return {
    ...mockUsageData,
    ...(data || {}),
    summary: {
      ...mockUsageData.summary,
      ...(data?.summary || {}),
    },
    monthlyTrend: data?.monthlyTrend || data?.monthlyUsage || mockUsageData.monthlyTrend,
    stationTop: data?.stationTop || data?.stationRanking || mockUsageData.stationTop,
    regionRanking: data?.regionRanking || data?.regionUsage || mockUsageData.regionRanking,
    chargerTypeSummary: data?.chargerTypeSummary || data?.chargerTypeUsage || mockUsageData.chargerTypeSummary,
    detailRows: data?.detailRows || data?.usageDetails || data?.recentUsage || mockUsageData.detailRows,
  };
};

const pickData = (responseData) => {
  console.log("이용 통계 응답 데이터 정규화", responseData);

  if (responseData?.summary || responseData?.monthlyTrend) {
    return normalizeUsageData(responseData);
  }

  if (responseData?.data?.summary || responseData?.data?.monthlyTrend) {
    return normalizeUsageData(responseData.data);
  }

  return null;
};

const filterRows = (rows, filter) => {
  console.log("이용 상세 목록 필터 적용", filter);

  return rows.filter((row) => {
    const matchRegion = filter.region === "전체" || row.region === filter.region;
    const matchStation = filter.stationName === "전체" || row.stationName === filter.stationName;
    const matchType = filter.chargerType === "전체" || row.chargerType === filter.chargerType;

    return matchRegion && matchStation && matchType;
  });
};

const TrendBarChart = ({ rows, valueKey, suffix = "건", color = "blue" }) => {
  console.log("월별 이용 추이 차트 렌더링", rows);

  const safeRows = rows || [];
  const maxValue = Math.max(...safeRows.map((item) => Number(item[valueKey] || 0)), 1);

  return (
    <div className="mis-trend-chart">
      {safeRows.map((item) => {
        const value = Number(item[valueKey] || 0);
        const height = Math.max((value / maxValue) * 190, 14);

        return (
          <div className="mis-trend-item" key={item.month}>
            <div className="mis-trend-bar-wrap">
              <span
                className={`mis-trend-bar ${color}`}
                style={{ height: `${height}px` }}
                title={`${item.month} ${numberFormat(value)}${suffix}`}
              />
            </div>
            <b>{compactMonth(item.month)}</b>
            <em>{numberFormat(value)}{suffix}</em>
          </div>
        );
      })}
    </div>
  );
};

const UsageStatPage = () => {
  console.log("UsageStatPage 렌더링");

  const [filter, setFilter] = useState({
    startDate: DEFAULT_START_DATE,
    endDate: DEFAULT_END_DATE,
    region: "전체",
    stationName: "전체",
    chargerType: "전체",
  });
  const [usageData, setUsageData] = useState(mockUsageData);
  const [loading, setLoading] = useState(false);

  const filteredDetails = useMemo(() => {
    return filterRows(usageData.detailRows, filter);
  }, [usageData, filter]);

  const showStationTop = filter.stationName === "전체";
  const showRegionRanking = filter.region === "전체";

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("이용 통계 필터 변경", name, value);

    setFilter({
      ...filter,
      [name]: value,
    });
  };

  const loadUsageStatistics = async () => {
    console.log("이용 통계 조회 실행", filter);

    setLoading(true);

    try {
      const response = await adminApi.usageStatistics(filter);
      const nextData = pickData(response.data);

      if (nextData) {
        setUsageData(nextData);
      } else {
        console.log("이용 통계 API가 JSON이 아니므로 Mock 데이터를 유지합니다.");
        setUsageData(mockUsageData);
      }
    } catch (error) {
      console.log("이용 통계 조회 실패 - Mock 데이터 사용", error);
      setUsageData(mockUsageData);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log("UsageStatPage 최초 데이터 조회");
    loadUsageStatistics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <section className="admin-page stat-dashboard-page">
      <div className="admin-page-header">
        <div>
          <p>통계/분석</p>
          <h1>이용 통계</h1>
          <span>필터 조건으로 운영 현황을 조회하고, 월별 추이는 최근 12개월 기준으로 확인합니다.</span>
        </div>
      </div>

      <section className="mis-filter-box">
        <div className="mis-filter-title">
          <strong>조회 조건</strong>
          <span>기간, 지역, 충전소, 충전기 유형을 기준으로 KPI와 상세 내역을 조회합니다.</span>
        </div>

        <div className="mis-filter-grid">
          <label>시작일<input type="date" name="startDate" value={filter.startDate} onChange={changeValue} /></label>
          <label>종료일<input type="date" name="endDate" value={filter.endDate} onChange={changeValue} /></label>
          <label>지역<select name="region" value={filter.region} onChange={changeValue}>{regionOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <label>충전소<select name="stationName" value={filter.stationName} onChange={changeValue}>{stationOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <label>충전기 유형<select name="chargerType" value={filter.chargerType} onChange={changeValue}>{chargerTypeOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <button type="button" onClick={loadUsageStatistics} disabled={loading}>{loading ? "조회중" : "조회"}</button>
        </div>
      </section>

      <div className="admin-kpi-grid four stat-kpi-grid">
        <article className="admin-kpi-card stat-kpi-card"><span>총 이용 건수</span><strong>{numberFormat(usageData.summary.totalCount)}건</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>총 충전량</span><strong>{numberFormat(usageData.summary.totalKwh)}kWh</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>평균 충전 시간</span><strong>{numberFormat(usageData.summary.averageMinutes)}분</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>평균 이용금액</span><strong>{moneyFormat(usageData.summary.averageAmount)}</strong></article>
      </div>

      <article className="admin-panel stat-card">
        <div className="admin-panel-title"><div><strong>월별 이용 추이</strong><p>필터 조건과 별개로 최근 12개월 이용 흐름을 고정 표시합니다.</p></div></div>
        <TrendBarChart rows={usageData.monthlyTrend} valueKey="count" suffix="건" />
      </article>

      {(showStationTop || showRegionRanking) && (
        <div className="admin-grid admin-grid-2-1">
          {showStationTop && (
            <article className="admin-panel stat-card">
              <div className="admin-panel-title"><div><strong>충전소 이용 TOP 5</strong><p>충전소가 전체일 때만 상위 충전소를 표시합니다.</p></div></div>
              <table className="mis-rank-table">
                <thead><tr><th>순위</th><th>충전소</th><th>지역</th><th>이용건수</th><th>총 충전량</th><th>비율</th></tr></thead>
                <tbody>
                  {usageData.stationTop.map((item) => (
                    <tr key={item.rank}>
                      <td><em>{item.rank}</em></td>
                      <td><b>{item.stationName}</b></td>
                      <td>{item.region}</td>
                      <td>{numberFormat(item.count)}건</td>
                      <td>{numberFormat(item.totalKwh)}kWh</td>
                      <td>{item.ratio}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </article>
          )}

          {showRegionRanking && (
            <article className="admin-panel stat-card">
              <div className="admin-panel-title"><div><strong>지역별 이용 순위</strong><p>지역이 전체일 때만 지역별 순위를 표시합니다.</p></div></div>
              <table className="mis-rank-table compact">
                <thead><tr><th>순위</th><th>지역</th><th>이용건수</th><th>비율</th></tr></thead>
                <tbody>
                  {usageData.regionRanking.map((item) => (
                    <tr key={item.rank}>
                      <td><em>{item.rank}</em></td>
                      <td><b>{item.region}</b></td>
                      <td>{numberFormat(item.count)}건</td>
                      <td>{item.ratio}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </article>
          )}
        </div>
      )}

      <article className="admin-panel stat-card">
        <div className="admin-panel-title"><div><strong>충전기 유형별 이용 현황</strong><p>필터 조건 기준 충전기 유형별 이용 비율입니다.</p></div></div>
        <table className="mis-rank-table">
          <thead><tr><th>충전기 유형</th><th>이용건수</th><th>총 충전량</th><th>평균 시간</th><th>비율</th></tr></thead>
          <tbody>
            {usageData.chargerTypeSummary.map((item) => (
              <tr key={item.chargerType}>
                <td><b>{item.chargerType}</b></td>
                <td>{numberFormat(item.count)}건</td>
                <td>{numberFormat(item.totalKwh)}kWh</td>
                <td>{numberFormat(item.averageMinutes)}분</td>
                <td>{item.ratio}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </article>

      <article className="admin-panel stat-card">
        <div className="admin-panel-title"><div><strong>상세 이용 내역</strong><p>상단 조회 조건이 적용된 충전 완료 내역입니다.</p></div></div>
        <div className="admin-table-wrap">
          <table className="admin-table recent-payment-table">
            <thead><tr><th>이용일시</th><th>회원명</th><th>지역</th><th>충전소</th><th>충전기</th><th>유형</th><th>충전량</th><th>충전시간</th><th>이용금액</th><th>상태</th></tr></thead>
            <tbody>
              {filteredDetails.map((item) => (
                <tr key={item.id}>
                  <td>{item.date}</td>
                  <td>{item.memberName}</td>
                  <td>{item.region}</td>
                  <td>{item.stationName}</td>
                  <td>{item.chargerName}</td>
                  <td>{item.chargerType}</td>
                  <td>{numberFormat(item.kwh)}kWh</td>
                  <td>{numberFormat(item.minutes)}분</td>
                  <td>{moneyFormat(item.amount)}</td>
                  <td>{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  );
};

export default UsageStatPage;
