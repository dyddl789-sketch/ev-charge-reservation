import { Link } from "react-router-dom";

const StationPage = () => {
  console.log("StationPage 렌더링");

  return (
    <section className="page-section">
      <div className="section-inner">
        <div className="page-title-box">
          <p className="page-subtitle">EV Charge Reservation v2.0</p>
          <h1>충전소 찾기</h1>
          <p>충전소 목록/검색 API 연결 예정 화면입니다.</p>
        </div>

        <div className="page-placeholder-card">
          <strong>충전소 찾기 화면 준비 영역</strong>
          <p>라우터와 메뉴 연결을 먼저 완료하고, 다음 단계에서 JSP/CSS/JS 기준으로 실제 화면을 채웁니다.</p>
          <div className="page-actions">
            <Link to="/">메인으로</Link>
            <Link to="/admin/dashboard">MIS 대시보드</Link>
          </div>
        </div>
      </div>
    </section>
  );
};

export default StationPage;
