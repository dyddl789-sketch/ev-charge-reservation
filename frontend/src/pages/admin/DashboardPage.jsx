import { Link } from "react-router-dom";

const kpiList = [
  { label: "총 회원", value: "1,248명", note: "활성회원 1,102명" },
  { label: "오늘 예약", value: "42건", note: "예약완료 31건" },
  { label: "현재 충전중", value: "18건", note: "급속 12 / 완속 6" },
  { label: "미처리 민원", value: "7건", note: "긴급 2건" },
  { label: "결재 대기", value: "3건", note: "부품 교체 2건" },
];

const recentComplaints = [
  { id: 301, title: "충전기 화면 오류", type: "시설장애", status: "접수", owner: "운영팀" },
  { id: 302, title: "예약 시간 변경 문의", type: "예약문의", status: "처리중", owner: "운영팀" },
  { id: 303, title: "결제 승인 취소 요청", type: "결제문의", status: "배정", owner: "고객지원" },
];

const pendingApprovals = [
  { id: "APR-2026-001", title: "충전기 커넥터 교체 요청", cost: "320,000원", status: "상신" },
  { id: "APR-2026-002", title: "통신 모듈 점검 비용 승인", cost: "180,000원", status: "1차승인" },
  { id: "APR-2026-003", title: "시설 보수 작업 요청", cost: "520,000원", status: "상신" },
];

const DashboardPage = () => {
  console.log("DashboardPage 렌더링");

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>운영기관 MIS</p>
          <h1>MIS 대시보드</h1>
          <span>예약, 민원, 장애, 결재 현황을 한 화면에서 확인합니다.</span>
        </div>
      </div>

      <div className="admin-kpi-grid">
        {kpiList.map((item) => (
          <article className="admin-kpi-card" key={item.label}>
            <span>{item.label}</span>
            <strong>{item.value}</strong>
            <p>{item.note}</p>
          </article>
        ))}
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>운영 업무 흐름</strong>
              <p>민원 접수부터 장애·점검, 전자결재, 조치 완료까지 이어지는 MIS 처리 흐름입니다.</p>
            </div>
          </div>

          <div className="admin-flow-row">
            {[
              "민원 접수",
              "운영팀 확인",
              "장애·점검 이관",
              "전자결재",
              "조치 완료",
            ].map((step, index) => (
              <div className="admin-flow-step" key={step}>
                <span>{index + 1}</span>
                <b>{step}</b>
              </div>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>빠른 이동</strong>
          </div>
          <div className="admin-quick-links">
            <Link to="/admin/reservations">예약 현황</Link>
            <Link to="/admin/complaints">민원관리</Link>
            <Link to="/admin/faults">장애·점검관리</Link>
            <Link to="/admin/approvals">전자결재</Link>
          </div>
        </article>
      </div>

      <div className="admin-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>최근 민원</strong>
            <Link to="/admin/complaints">전체보기</Link>
          </div>
          <div className="admin-list">
            {recentComplaints.map((item) => (
              <div className="admin-list-row" key={item.id}>
                <div>
                  <b>{item.title}</b>
                  <span>{item.type} · 담당 {item.owner}</span>
                </div>
                <em className={`admin-badge ${item.status === "접수" ? "warning" : "blue"}`}>{item.status}</em>
              </div>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>결재 대기</strong>
            <Link to="/admin/approvals">결재하기</Link>
          </div>
          <div className="admin-list">
            {pendingApprovals.map((item) => (
              <div className="admin-list-row" key={item.id}>
                <div>
                  <b>{item.title}</b>
                  <span>{item.id} · 예상비용 {item.cost}</span>
                </div>
                <em className="admin-badge purple">{item.status}</em>
              </div>
            ))}
          </div>
        </article>
      </div>
    </section>
  );
};

export default DashboardPage;
