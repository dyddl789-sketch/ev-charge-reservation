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
const mockSalesData = {
  summary: {
    totalSales: 124500000,
    paymentCount: 1284,
    averagePayment: 96962,
    totalKwh: 42580.6,
  },
  monthlyTrend: [
    { month: "2025-07", sales: 15400000 },
    { month: "2025-08", sales: 16200000 },
    { month: "2025-09", sales: 17100000 },
    { month: "2025-10", sales: 18400000 },
    { month: "2025-11", sales: 19000000 },
    { month: "2025-12", sales: 19800000 },
    { month: "2026-01", sales: 18100000 },
    { month: "2026-02", sales: 20600000 },
    { month: "2026-03", sales: 22150000 },
    { month: "2026-04", sales: 23800000 },
    { month: "2026-05", sales: 25600000 },
    { month: "2026-06", sales: 28300000 },
  ],
  stationTop: [
    { rank: 1, stationName: "부산역 공공충전소", region: "동구", sales: 28150000, count: 218, ratio: 22.6 },
    { rank: 2, stationName: "해운대 구청 충전소", region: "해운대구", sales: 22400000, count: 184, ratio: 18.0 },
    { rank: 3, stationName: "서면 환승센터 충전소", region: "부산진구", sales: 19880000, count: 162, ratio: 16.0 },
    { rank: 4, stationName: "광안리 해변 충전소", region: "수영구", sales: 17620000, count: 141, ratio: 14.2 },
    { rank: 5, stationName: "사상 공영주차장 충전소", region: "사상구", sales: 14290000, count: 117, ratio: 11.5 },
  ],
  regionRanking: [
    { rank: 1, region: "부산진구", sales: 33600000, count: 342, ratio: 27.0 },
    { rank: 2, region: "동구", sales: 28150000, count: 218, ratio: 22.6 },
    { rank: 3, region: "해운대구", sales: 27200000, count: 276, ratio: 21.8 },
    { rank: 4, region: "수영구", sales: 17620000, count: 184, ratio: 14.2 },
    { rank: 5, region: "사상구", sales: 14290000, count: 117, ratio: 11.5 },
  ],
  chargerTypeSummary: [
    { chargerType: "완속", sales: 35600000, count: 462, ratio: 28.6, averagePayment: 77056 },
    { chargerType: "급속", sales: 64200000, count: 618, ratio: 51.6, averagePayment: 103883 },
    { chargerType: "초급속", sales: 24700000, count: 204, ratio: 19.8, averagePayment: 121078 },
  ],
  detailRows: [
    { id: 1, paidAt: "2026-06-30 20:45", region: "동구", stationName: "부산역 공공충전소", chargerName: "부산역 급속 01", chargerType: "급속", memberName: "김성민", kwh: 42.1, amount: 12630, status: "완료" },
    { id: 2, paidAt: "2026-06-30 20:01", region: "해운대구", stationName: "해운대 구청 충전소", chargerName: "해운대 완속 02", chargerType: "완속", memberName: "시연회원12", kwh: 38.4, amount: 10368, status: "완료" },
    { id: 3, paidAt: "2026-06-30 18:50", region: "부산진구", stationName: "서면 환승센터 충전소", chargerName: "서면 초급속 01", chargerType: "초급속", memberName: "시연회원09", kwh: 51.7, amount: 19646, status: "완료" },
    { id: 4, paidAt: "2026-06-30 17:40", region: "수영구", stationName: "광안리 해변 충전소", chargerName: "광안리 급속 02", chargerType: "급속", memberName: "시연회원03", kwh: 28.6, amount: 8580, status: "완료" },
    { id: 5, paidAt: "2026-06-30 16:55", region: "사상구", stationName: "사상 공영주차장 충전소", chargerName: "사상 완속 01", chargerType: "완속", memberName: "시연회원21", kwh: 33.8, amount: 9126, status: "완료" },
  ],
};

const numberFormat = (value) => Number(value || 0).toLocaleString("ko-KR");
const moneyFormat = (value) => `${numberFormat(value)}원`;
const compactMoney = (value) => `${numberFormat(Math.round(Number(value || 0) / 10000))}만`;
const compactMonth = (value) => String(value || "").replace("2025-", "").replace("2026-", "") + "월";

const normalizeSalesData = (data) => {
  console.log("매출 통계 데이터 정규화", data);

  return {
    ...mockSalesData,
    ...(data || {}),
    summary: {
      ...mockSalesData.summary,
      ...(data?.summary || {}),
    },
    monthlyTrend: data?.monthlyTrend || data?.monthlySales || mockSalesData.monthlyTrend,
    stationTop: data?.stationTop || data?.stationRanking || mockSalesData.stationTop,
    regionRanking: data?.regionRanking || data?.regionSales || mockSalesData.regionRanking,
    chargerTypeSummary: data?.chargerTypeSummary || data?.chargerTypeSales || mockSalesData.chargerTypeSummary,
    detailRows: data?.detailRows || data?.salesDetails || data?.recentPayments || mockSalesData.detailRows,
  };
};

const pickData = (responseData) => {
  console.log("매출 통계 응답 데이터 정규화", responseData);

  if (responseData?.summary || responseData?.monthlyTrend) {
    return normalizeSalesData(responseData);
  }

  if (responseData?.data?.summary || responseData?.data?.monthlyTrend) {
    return normalizeSalesData(responseData.data);
  }

  return null;
};

const filterRows = (rows, filter) => {
  console.log("매출 상세 목록 필터 적용", filter);

  return rows.filter((row) => {
    const matchRegion = filter.region === "전체" || row.region === filter.region;
    const matchStation = filter.stationName === "전체" || row.stationName === filter.stationName;
    const matchType = filter.chargerType === "전체" || row.chargerType === filter.chargerType;

    return matchRegion && matchStation && matchType;
  });
};

const TrendBarChart = ({ rows, valueKey, color = "orange" }) => {
  console.log("월별 매출 추이 차트 렌더링", rows);

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
                title={`${item.month} ${moneyFormat(value)}`}
              />
            </div>
            <b>{compactMonth(item.month)}</b>
            <em>{compactMoney(value)}</em>
          </div>
        );
      })}
    </div>
  );
};

const SalesStatPage = () => {
  console.log("SalesStatPage 렌더링");

  const [filter, setFilter] = useState({
    startDate: DEFAULT_START_DATE,
    endDate: DEFAULT_END_DATE,
    region: "전체",
    stationName: "전체",
    chargerType: "전체",
  });
  const [salesData, setSalesData] = useState(mockSalesData);
  const [loading, setLoading] = useState(false);

  const filteredDetails = useMemo(() => {
    return filterRows(salesData.detailRows, filter);
  }, [salesData, filter]);

  const showStationTop = filter.stationName === "전체";
  const showRegionRanking = filter.region === "전체";

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("매출 통계 필터 변경", name, value);

    setFilter({
      ...filter,
      [name]: value,
    });
  };

  const loadSalesStatistics = async () => {
    console.log("매출 통계 조회 실행", filter);

    setLoading(true);

    try {
      const response = await adminApi.salesStatistics(filter);
      const nextData = pickData(response.data);

      if (nextData) {
        setSalesData(nextData);
      } else {
        console.log("매출 통계 API가 JSON이 아니므로 Mock 데이터를 유지합니다.");
        setSalesData(mockSalesData);
      }
    } catch (error) {
      console.log("매출 통계 조회 실패 - Mock 데이터 사용", error);
      setSalesData(mockSalesData);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log("SalesStatPage 최초 데이터 조회");
    loadSalesStatistics();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <section className="admin-page stat-dashboard-page">
      <div className="admin-page-header">
        <div>
          <p>통계/분석</p>
          <h1>매출 통계</h1>
          <span>필터 조건으로 결제 현황을 조회하고, 월별 추이는 최근 12개월 기준으로 확인합니다.</span>
        </div>
      </div>

      <section className="mis-filter-box sales">
        <div className="mis-filter-title">
          <strong>조회 조건</strong>
          <span>기간, 지역, 충전소, 충전기 유형을 기준으로 KPI와 상세 매출을 조회합니다.</span>
        </div>

        <div className="mis-filter-grid">
          <label>시작일<input type="date" name="startDate" value={filter.startDate} onChange={changeValue} /></label>
          <label>종료일<input type="date" name="endDate" value={filter.endDate} onChange={changeValue} /></label>
          <label>지역<select name="region" value={filter.region} onChange={changeValue}>{regionOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <label>충전소<select name="stationName" value={filter.stationName} onChange={changeValue}>{stationOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <label>충전기 유형<select name="chargerType" value={filter.chargerType} onChange={changeValue}>{chargerTypeOptions.map((item) => <option key={item}>{item}</option>)}</select></label>
          <button type="button" onClick={loadSalesStatistics} disabled={loading}>{loading ? "조회중" : "조회"}</button>
        </div>
      </section>

      <div className="admin-kpi-grid four stat-kpi-grid">
        <article className="admin-kpi-card stat-kpi-card"><span>총 매출</span><strong>{moneyFormat(salesData.summary.totalSales)}</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>결제 건수</span><strong>{numberFormat(salesData.summary.paymentCount)}건</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>평균 결제금액</span><strong>{moneyFormat(salesData.summary.averagePayment)}</strong></article>
        <article className="admin-kpi-card stat-kpi-card"><span>총 충전량</span><strong>{numberFormat(salesData.summary.totalKwh)}kWh</strong></article>
      </div>

      <article className="admin-panel stat-card">
        <div className="admin-panel-title"><div><strong>월별 매출 추이</strong><p>필터 조건과 별개로 최근 12개월 매출 흐름을 고정 표시합니다.</p></div></div>
        <TrendBarChart rows={salesData.monthlyTrend} valueKey="sales" color="orange" />
      </article>

      {(showStationTop || showRegionRanking) && (
        <div className="admin-grid admin-grid-2-1">
          {showStationTop && (
            <article className="admin-panel stat-card">
              <div className="admin-panel-title"><div><strong>충전소 매출 TOP 5</strong><p>충전소가 전체일 때만 상위 충전소를 표시합니다.</p></div></div>
              <table className="mis-rank-table sales-table">
                <thead><tr><th>순위</th><th>충전소</th><th>지역</th><th>매출</th><th>결제건수</th><th>비율</th></tr></thead>
                <tbody>
                  {salesData.stationTop.map((item) => (
                    <tr key={item.rank}>
                      <td><em>{item.rank}</em></td>
                      <td><b>{item.stationName}</b></td>
                      <td>{item.region}</td>
                      <td>{moneyFormat(item.sales)}</td>
                      <td>{numberFormat(item.count)}건</td>
                      <td>{item.ratio}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </article>
          )}

          {showRegionRanking && (
            <article className="admin-panel stat-card">
              <div className="admin-panel-title"><div><strong>지역별 매출 순위</strong><p>지역이 전체일 때만 지역별 순위를 표시합니다.</p></div></div>
              <table className="mis-rank-table compact sales-table">
                <thead><tr><th>순위</th><th>지역</th><th>매출</th><th>비율</th></tr></thead>
                <tbody>
                  {salesData.regionRanking.map((item) => (
                    <tr key={item.rank}>
                      <td><em>{item.rank}</em></td>
                      <td><b>{item.region}</b></td>
                      <td>{moneyFormat(item.sales)}</td>
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
        <div className="admin-panel-title"><div><strong>충전기 유형별 매출 현황</strong><p>필터 조건 기준 충전기 유형별 매출 비율입니다.</p></div></div>
        <table className="mis-rank-table sales-table">
          <thead><tr><th>충전기 유형</th><th>매출</th><th>결제건수</th><th>평균 결제금액</th><th>비율</th></tr></thead>
          <tbody>
            {salesData.chargerTypeSummary.map((item) => (
              <tr key={item.chargerType}>
                <td><b>{item.chargerType}</b></td>
                <td>{moneyFormat(item.sales)}</td>
                <td>{numberFormat(item.count)}건</td>
                <td>{moneyFormat(item.averagePayment)}</td>
                <td>{item.ratio}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </article>

      <article className="admin-panel stat-card">
        <div className="admin-panel-title"><div><strong>상세 매출 내역</strong><p>상단 조회 조건이 적용된 결제 완료 내역입니다.</p></div></div>
        <div className="admin-table-wrap">
          <table className="admin-table recent-payment-table">
            <thead><tr><th>결제일시</th><th>회원명</th><th>지역</th><th>충전소</th><th>충전기</th><th>유형</th><th>충전량</th><th>결제금액</th><th>상태</th></tr></thead>
            <tbody>
              {filteredDetails.map((item) => (
                <tr key={item.id}>
                  <td>{item.paidAt}</td>
                  <td>{item.memberName}</td>
                  <td>{item.region}</td>
                  <td>{item.stationName}</td>
                  <td>{item.chargerName}</td>
                  <td>{item.chargerType}</td>
                  <td>{numberFormat(item.kwh)}kWh</td>
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

export default SalesStatPage;
