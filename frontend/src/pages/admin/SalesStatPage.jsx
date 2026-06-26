import { useEffect, useMemo, useRef, useState } from "react";
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LineElement,
  LinearScale,
  PointElement,
  Tooltip,
} from "chart.js";
import { Bar, Doughnut, Line } from "react-chartjs-2";
import * as adminApi from "../../apis/adminApi";
import useAdminPolling from "../../hooks/useAdminPolling";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Tooltip, Legend, Filler);

const today = new Date();
const defaultEnd = today.toISOString().slice(0, 10);
const defaultStart = new Date(today.getFullYear(), today.getMonth() - 4, today.getDate()).toISOString().slice(0, 10);

const regionOptions = ["전체", "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"];
const chargerTypeOptions = ["전체", "완속", "급속", "초급속"];
const historyPageSize = 10;

const salesColors = {
  line: "#f97316",
  lineFill: "rgba(249, 115, 22, 0.16)",
  bar: "rgba(251, 146, 60, 0.78)",
  barBorder: "#ea580c",
  doughnut: ["#f97316", "#f59e0b", "#ef4444", "#fb7185"],
};

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");
const decimalText = (value) => Number(value || 0).toLocaleString("ko-KR", { maximumFractionDigits: 2 });
const moneyText = (value) => `${numberText(value)}원`;
const rateText = (value) => `${Number(value || 0).toLocaleString("ko-KR")}%`;

const doughnutPercentPlugin = {
  id: "doughnutPercentLabel",
  afterDatasetsDraw(chart) {
    const dataset = chart.data.datasets?.[0];
    const meta = chart.getDatasetMeta(0);

    if (!dataset || !meta?.data?.length) {
      return;
    }

    const total = (dataset.data || []).reduce((sum, value) => sum + Number(value || 0), 0);

    if (!total) {
      return;
    }

    const { ctx } = chart;
    ctx.save();
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.font = "800 12px sans-serif";

    meta.data.forEach((arc, index) => {
      const value = Number(dataset.data[index] || 0);

      if (!value) {
        return;
      }

      const percent = value * 100 / total;

      if (percent < 3) {
        return;
      }

      const position = arc.tooltipPosition();
      ctx.fillStyle = percent >= 8 ? "#ffffff" : "#0f172a";
      ctx.fillText(`${percent.toFixed(1)}%`, position.x, position.y);
    });

    ctx.restore();
  },
};


const buildMonthRows = (dailyList = []) => {
  const map = new Map();
  dailyList.forEach((row) => {
    const month = String(row.salesDate || "").slice(0, 7);
    if (!month) return;
    const current = map.get(month) || { label: month, salesAmount: 0, paymentCount: 0 };
    current.salesAmount += Number(row.salesAmount || 0);
    current.paymentCount += Number(row.paymentCount || 0);
    map.set(month, current);
  });

  return Array.from(map.values()).map((row) => ({
    ...row,
    avgPaymentAmount: row.paymentCount > 0 ? Math.round(row.salesAmount / row.paymentCount) : 0,
  }));
};

const buildDailyRows = (dailyList = []) => dailyList.map((row) => ({
  label: row.salesDate,
  salesAmount: Number(row.salesAmount || 0),
  paymentCount: Number(row.paymentCount || 0),
  avgPaymentAmount: Number(row.avgPaymentAmount || 0),
}));

const SalesStatPage = () => {
  console.log("SalesStatPage 렌더링");

  const [filter, setFilter] = useState({ startDate: defaultStart, endDate: defaultEnd, region: "", chargerType: "" });
  const [data, setData] = useState({ summary: {}, dailyList: [], hourlyList: [], typeList: [], stationRankList: [], historyList: [] });
  const [loading, setLoading] = useState(false);
  const [periodMode, setPeriodMode] = useState("daily");
  const [trendViewMode, setTrendViewMode] = useState("chart");
  const [hourlyViewMode, setHourlyViewMode] = useState("chart");
  const [hourlyTitle, setHourlyTitle] = useState("필터 기간 전체");
  const [selectedDate, setSelectedDate] = useState("");
  const [historyPage, setHistoryPage] = useState(1);
  const isFirstFilterChangeRef = useRef(true);
  const filterKey = useMemo(() => JSON.stringify(filter), [filter]);

  const loadStats = async (nextFilter = filter) => {
    console.log("매출 통계 실제 조회", nextFilter);
    setLoading(true);
    try {
      const response = await adminApi.salesStatistics(nextFilter);
      console.log("매출 통계 응답", response.data);
      setData(response.data || {});
      setHourlyTitle("필터 기간 전체");
      setSelectedDate("");
      setHistoryPage(1);
    } catch (error) {
      console.log("매출 통계 조회 실패", error);
      alert(error.response?.data?.message || "매출 통계를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (isFirstFilterChangeRef.current) {
      isFirstFilterChangeRef.current = false;
      return undefined;
    }

    const timer = setTimeout(() => {
      console.log('매출 통계 필터 변경 즉시 재조회', filter);
      loadStats(filter);
    }, 400);

    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterKey]);

  useAdminPolling(() => loadStats(filter), { label: '매출통계' });

  const changeFilter = (e) => {
    const { name, value } = e.target;
    console.log("매출 통계 필터 변경", name, value);
    setFilter((prev) => ({ ...prev, [name]: value }));
  };

  const submitFilter = (e) => {
    e.preventDefault();
    loadStats(filter);
  };

  const createSampleData = async () => {
    console.log("매출 통계 샘플 생성");
    if (!window.confirm("기존 통계회원(stat_user_%) 샘플 예약/충전 세션을 정리하고 최근 4개월 기준으로 다시 생성할까요?")) {
      return;
    }

    setLoading(true);
    try {
      const response = await adminApi.generateStatisticsSampleData({ days: 120, count: 15000 });
      console.log("통계 샘플 생성 응답", response.data);
      alert(response.data?.message || "통계 샘플 데이터가 생성되었습니다.");
      await loadStats(filter);
    } catch (error) {
      console.log("통계 샘플 생성 실패", error);
      alert(error.response?.data?.message || "통계 샘플 생성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const loadHourlyForDate = async (salesDate) => {
    if (!salesDate || periodMode !== "daily") return;
    console.log("선택 일자 시간대별 매출 조회", salesDate);

    try {
      const response = await adminApi.salesStatistics({ ...filter, startDate: salesDate, endDate: salesDate });
      console.log("선택 일자 시간대별 매출 응답", response.data);
      setData((prev) => ({ ...prev, hourlyList: response.data?.hourlyList || [] }));
      setHourlyTitle(`${salesDate} 기준`);
      setSelectedDate(salesDate);
    } catch (error) {
      console.log("선택 일자 시간대별 매출 조회 실패", error);
      alert(error.response?.data?.message || "선택한 날짜의 시간대별 매출 통계를 불러오지 못했습니다.");
    }
  };

  const loadWholeHourly = async () => {
    console.log("필터 기간 전체 시간대별 매출 보기");
    await loadStats(filter);
  };

  const summary = data.summary || {};
  const trendRows = periodMode === "monthly" ? buildMonthRows(data.dailyList || []) : buildDailyRows(data.dailyList || []);
  const historyList = data.historyList || [];
  const historyTotalPages = Math.max(1, Math.ceil(historyList.length / historyPageSize));
  const pagedHistoryList = historyList.slice((historyPage - 1) * historyPageSize, historyPage * historyPageSize);

  const trendChartData = useMemo(() => ({
    labels: trendRows.map((row) => row.label),
    datasets: [
      {
        label: periodMode === "monthly" ? "월별 매출" : "일별 매출",
        data: trendRows.map((row) => row.salesAmount),
        borderColor: salesColors.line,
        backgroundColor: salesColors.lineFill,
        pointBackgroundColor: salesColors.line,
        pointBorderColor: "#ffffff",
        borderWidth: 3,
        tension: 0.35,
        fill: true,
      },
    ],
  }), [periodMode, trendRows]);

  const hourlyChartData = useMemo(() => ({
    labels: (data.hourlyList || []).map((row) => `${row.salesHour}시`),
    datasets: [
      {
        label: "시간대별 매출",
        data: (data.hourlyList || []).map((row) => Number(row.salesAmount || 0)),
        backgroundColor: salesColors.bar,
        borderColor: salesColors.barBorder,
        borderWidth: 1,
        borderRadius: 8,
      },
    ],
  }), [data.hourlyList]);

  const typeChartData = useMemo(() => ({
    labels: (data.typeList || []).map((row) => row.chargerType || "기타"),
    datasets: [
      {
        label: "매출 비율",
        data: (data.typeList || []).map((row) => Number(row.salesAmount || 0)),
        backgroundColor: salesColors.doughnut,
        borderColor: "#ffffff",
        borderWidth: 3,
      },
    ],
  }), [data.typeList]);

  const lineOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: true }, tooltip: { mode: "index", intersect: false } },
    scales: { y: { beginAtZero: true } },
    onClick: (_, elements) => {
      if (periodMode !== "daily" || !elements?.length) return;
      const index = elements[0].index;
      const salesDate = trendRows[index]?.label;
      loadHourlyForDate(salesDate);
    },
  };

  const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true } },
  };

  const doughnutOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: "bottom" } },
    cutout: "62%",
  };

  const moveHistoryPage = (nextPage) => {
    console.log("상세 매출 페이지 이동", nextPage);
    if (nextPage < 1 || nextPage > historyTotalPages) return;
    setHistoryPage(nextPage);
  };

  return (
    <section className="admin-page statistics-modern-page sales-stat-page">
      <div className="admin-page-header">
        <div>
          <p>통계/분석</p>
          <h1>매출 통계</h1>
          <span>완료된 charging_session의 actual_cost를 기준으로 매출 흐름을 분석합니다.</span>
        </div>
        <button type="button" onClick={createSampleData} disabled={loading}>{loading ? "처리 중" : "이용/매출 통계 샘플 생성"}</button>
      </div>

      <form className="admin-filter-panel polished statistics-filter" onSubmit={submitFilter}>
        <label><span>시작일</span><input type="date" name="startDate" value={filter.startDate} onChange={changeFilter} /></label>
        <label><span>종료일</span><input type="date" name="endDate" value={filter.endDate} onChange={changeFilter} /></label>
        <label>
          <span>지역</span>
          <select name="region" value={filter.region} onChange={changeFilter}>
            {regionOptions.map((region) => <option key={region} value={region === "전체" ? "" : region}>{region}</option>)}
          </select>
        </label>
        <label>
          <span>충전기 유형</span>
          <select name="chargerType" value={filter.chargerType} onChange={changeFilter}>
            {chargerTypeOptions.map((type) => <option key={type} value={type === "전체" ? "" : type}>{type}</option>)}
          </select>
        </label>
        <div className="filter-actions"><button type="submit" disabled={loading}>{loading ? "조회 중" : "조회"}</button></div>
      </form>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>총 매출</span><strong>{moneyText(summary.totalSalesAmount)}</strong><p>필터 기간 합계</p></article>
        <article className="admin-kpi-card"><span>오늘 완료 매출</span><strong>{moneyText(summary.todaySalesAmount)}</strong><p>오늘 완료 세션 기준</p></article>
        <article className="admin-kpi-card"><span>평균 결제금액</span><strong>{moneyText(summary.avgPaymentAmount)}</strong><p>세션당 평균</p></article>
        <article className="admin-kpi-card"><span>총 충전량</span><strong>{decimalText(summary.totalKwh)}kWh</strong><p>매출 연동 충전량</p></article>
      </div>

      <div className="admin-grid statistics-chart-grid">
        <article className="admin-panel chart-panel wide-chart-panel">
          <div className="admin-panel-title">
            <div><strong>일별 / 월별 매출 추이</strong><p>그래프 또는 표의 일자를 클릭하면 시간대별 매출을 확인할 수 있습니다.</p></div>
            <div className="stat-toggle-group multi-toggle">
              <button type="button" className={periodMode === "daily" ? "active" : ""} onClick={() => setPeriodMode("daily")}>일별</button>
              <button type="button" className={periodMode === "monthly" ? "active" : ""} onClick={() => setPeriodMode("monthly")}>월별</button>
              <button type="button" className={trendViewMode === "chart" ? "active" : ""} onClick={() => setTrendViewMode("chart")}>그래프</button>
              <button type="button" className={trendViewMode === "table" ? "active" : ""} onClick={() => setTrendViewMode("table")}>표 보기</button>
            </div>
          </div>
          {trendViewMode === "chart" ? (
            <div className="chart-canvas-box line-chart-box">
              {trendRows.length > 0 ? <Line data={trendChartData} options={lineOptions} /> : <div className="empty-box">매출 데이터가 없습니다.</div>}
            </div>
          ) : (
            <div className="stat-detail-table-wrap">
              <table className="admin-table compact stat-detail-table">
                <thead><tr><th>{periodMode === "daily" ? "일자" : "월"}</th><th>매출</th><th>결제 건수</th><th>평균 결제금액</th></tr></thead>
                <tbody>
                  {trendRows.map((row) => (
                    <tr key={row.label} className={selectedDate === row.label ? "selected-row" : "clickable-row"} onClick={() => loadHourlyForDate(row.label)}>
                      <td>{row.label}</td><td>{moneyText(row.salesAmount)}</td><td>{numberText(row.paymentCount)}건</td><td>{moneyText(row.avgPaymentAmount)}</td>
                    </tr>
                  ))}
                  {trendRows.length === 0 && <tr><td colSpan="4">데이터가 없습니다.</td></tr>}
                </tbody>
              </table>
            </div>
          )}
        </article>

        <article className="admin-panel chart-panel">
          <div className="admin-panel-title">
            <div><strong>시간대별 매출 현황</strong><p>{hourlyTitle}</p></div>
            <div className="stat-toggle-group multi-toggle">
              <button type="button" className={hourlyViewMode === "chart" ? "active" : ""} onClick={() => setHourlyViewMode("chart")}>그래프</button>
              <button type="button" className={hourlyViewMode === "table" ? "active" : ""} onClick={() => setHourlyViewMode("table")}>표 보기</button>
              <button type="button" onClick={loadWholeHourly}>전체 보기</button>
            </div>
          </div>
          {hourlyViewMode === "chart" ? (
            <div className="chart-canvas-box bar-chart-box">
              {(data.hourlyList || []).length > 0 ? <Bar data={hourlyChartData} options={barOptions} /> : <div className="empty-box">시간대별 데이터가 없습니다.</div>}
            </div>
          ) : (
            <div className="stat-detail-table-wrap">
              <table className="admin-table compact stat-detail-table">
                <thead><tr><th>시간대</th><th>결제 건수</th><th>매출</th><th>평균 결제금액</th><th>충전량</th></tr></thead>
                <tbody>
                  {(data.hourlyList || []).map((row) => <tr key={row.salesHour}><td>{row.salesHour}시</td><td>{numberText(row.paymentCount)}건</td><td>{moneyText(row.salesAmount)}</td><td>{moneyText(row.avgPaymentAmount)}</td><td>{decimalText(row.totalKwh)}kWh</td></tr>)}
                  {(data.hourlyList || []).length === 0 && <tr><td colSpan="5">데이터가 없습니다.</td></tr>}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </div>

      <div className="admin-grid statistics-chart-grid">
        <article className="admin-panel chart-panel">
          <div className="admin-panel-title"><div><strong>충전기 유형별 매출</strong><p>완속/급속/초급속 매출 비율입니다.</p></div></div>
          <div className="chart-canvas-box doughnut-chart-box">
            {(data.typeList || []).length > 0 ? <Doughnut data={typeChartData} options={doughnutOptions} plugins={[doughnutPercentPlugin]} /> : <div className="empty-box">유형별 매출 데이터가 없습니다.</div>}
          </div>
          <div className="stat-mini-list">
            {(data.typeList || []).map((row) => <span key={row.chargerType}>{row.chargerType} {moneyText(row.salesAmount)} · {rateText(row.salesRate)}</span>)}
          </div>
        </article>

        <article className="admin-panel rank-panel">
          <div className="admin-panel-title"><div><strong>충전소 매출 순위</strong><p>필터 조건 기준 상위 10개 충전소입니다.</p></div></div>
          <div className="rank-table-wrap">
            <table className="admin-table compact rank-table">
              <thead><tr><th>순위</th><th>충전소</th><th>매출</th><th>비율</th></tr></thead>
              <tbody>
                {(data.stationRankList || []).map((row) => (
                  <tr key={row.stationId} className={row.rankNo <= 4 ? "rank-highlight" : ""}>
                    <td><b>{row.rankNo}</b></td>
                    <td className="rank-station-name" title={row.stationName}>{row.stationName}</td>
                    <td>{moneyText(row.salesAmount)}</td>
                    <td>{rateText(row.salesRate)}</td>
                  </tr>
                ))}
                {(data.stationRankList || []).length === 0 && <tr><td colSpan="4">데이터가 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        </article>
      </div>

      <article className="admin-panel">
        <div className="admin-panel-title">
          <div><strong>상세 매출 내역</strong><p>필터 조건에 해당하는 충전 완료 세션의 결제 근거 데이터입니다.</p></div>
          <span className="admin-sub-count">10개 단위 · {historyPage}/{historyTotalPages}페이지 · 총 {numberText(historyList.length)}건</span>
        </div>
        <div className="admin-table-wrap">
          <table className="admin-table elegant-table">
            <thead><tr><th>결제일시</th><th>회원명</th><th>충전소</th><th>충전기</th><th>충전량</th><th>충전시간</th><th>결제금액</th></tr></thead>
            <tbody>
              {pagedHistoryList.map((row) => (
                <tr key={row.sessionId}>
                  <td>{row.paymentDate}</td>
                  <td>{row.memberName}</td>
                  <td>{row.stationName}</td>
                  <td>{row.chargerName}</td>
                  <td>{decimalText(row.actualKwh)}kWh</td>
                  <td>{numberText(row.actualMinutes)}분</td>
                  <td>{moneyText(row.actualCost)}</td>
                </tr>
              ))}
              {pagedHistoryList.length === 0 && <tr><td colSpan="7">데이터가 없습니다.</td></tr>}
            </tbody>
          </table>
        </div>
        <div className="admin-pagination">
          <button type="button" onClick={() => moveHistoryPage(historyPage - 1)} disabled={historyPage <= 1}>이전</button>
          <span>{historyPage} / {historyTotalPages}</span>
          <button type="button" onClick={() => moveHistoryPage(historyPage + 1)} disabled={historyPage >= historyTotalPages}>다음</button>
        </div>
      </article>
    </section>
  );
};

export default SalesStatPage;
