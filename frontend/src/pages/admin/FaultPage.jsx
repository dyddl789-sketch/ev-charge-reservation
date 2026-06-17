const FaultPage = () => {
  console.log("FaultPage 렌더링");

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>운영기관 MIS</p>
          <h1>장애관리</h1>
        </div>
        <button type="button" onClick={() => console.log("장애관리 주요 기능 클릭")}>주요 기능</button>
      </div>

      <div className="admin-grid">
        <article className="admin-card">
          <strong>장애관리 목록</strong>
          <p>기존 JSP 관리자 화면과 백엔드 Controller/Service/DAO를 REST API로 연결할 영역입니다.</p>
        </article>
        <article className="admin-card">
          <strong>처리 현황</strong>
          <p>대시보드 KPI, 검색 조건, 테이블, 상세 이동 버튼을 이 위치에 추가합니다.</p>
        </article>
      </div>
    </section>
  );
};

export default FaultPage;
