import { Link } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";
import { useState } from "react";

const StatisticsPage = () => {
  console.log("StatisticsPage 렌더링");

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const createSampleData = async () => {
    console.log("통계 샘플 생성 버튼 클릭");
    if (!window.confirm("공공데이터로 적재된 충전소/충전기를 기준으로 예약완료·충전완료 샘플 데이터를 생성할까요?")) return;

    setLoading(true);
    try {
      const response = await adminApi.generateStatisticsSampleData({ days: 90, count: 500 });
      console.log("통계 샘플 생성 응답", response.data);
      setResult(response.data);
      alert(response.data?.message || "통계 샘플 데이터가 생성되었습니다.");
    } catch (error) {
      console.log("통계 샘플 생성 실패", error);
      alert("통계 샘플 생성에 실패했습니다. 먼저 공공데이터 샘플 적재 여부를 확인해 주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>통계/분석</p>
          <h1>통계분석 홈</h1>
          <span>charging_session 완료 데이터를 기준으로 이용통계와 매출통계를 집계합니다.</span>
        </div>
        <button type="button" onClick={createSampleData} disabled={loading}>{loading ? "생성 중" : "이용/매출 통계 샘플 생성"}</button>
      </div>

      <div className="admin-grid">
        <article className="admin-panel stat-home-card">
          <div className="admin-panel-title"><div><strong>이용 통계</strong><p>충전 완료 건수, 시간대별 이용량, 충전소별 이용 순위를 확인합니다.</p></div></div>
          <Link className="admin-primary-link" to="/admin/statistics/usage">이용 통계 보기</Link>
        </article>
        <article className="admin-panel stat-home-card">
          <div className="admin-panel-title"><div><strong>매출 통계</strong><p>실제 충전 완료 세션의 actual_cost를 기준으로 매출을 집계합니다.</p></div></div>
          <Link className="admin-primary-link" to="/admin/statistics/sales">매출 통계 보기</Link>
        </article>
      </div>

      <article className="admin-panel">
        <div className="admin-panel-title"><div><strong>통계 데이터 생성 방식</strong><p>대시보드의 공공데이터 적재로 들어온 충전소/충전기 목록을 사용합니다.</p></div></div>
        <div className="admin-guide-list polished-guide">
          <p><b>1. 공공데이터 적재</b> 대시보드에서 충전소와 충전기 실제 목록을 먼저 저장합니다.</p>
          <p><b>2. 통계 샘플 생성</b> 이 화면 버튼이 기존 충전소/충전기 기준으로 예약완료와 충전완료 데이터를 생성합니다.</p>
          <p><b>3. 실시간 반영</b> 사용자가 실제 예약 후 충전 완료하면 charging_session에 저장되고 통계 화면에 바로 반영됩니다.</p>
          <p><b>4. 기준 테이블</b> 이용/매출 통계는 reservation이 아니라 실제 사용 기록인 charging_session 완료 데이터 기준입니다.</p>
        </div>
        {result && (
          <div className="admin-result-box">
            <b>{result.message}</b>
            <span>이번 생성: {Number(result.saveCount || 0).toLocaleString("ko-KR")}건 · 전체 완료 세션: {Number(result.resetCount || 0).toLocaleString("ko-KR")}건</span>
          </div>
        )}
      </article>
    </section>
  );
};

export default StatisticsPage;
