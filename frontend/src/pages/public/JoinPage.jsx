import { Link } from "react-router-dom";

const JoinPage = () => {
  console.log("JoinPage 렌더링");

  return (
    <section className="page-section">
      <div className="section-inner">
        <div className="page-title-box">
          <p className="page-subtitle">EV Charge Reservation v2.0</p>
          <h1>회원가입</h1>
          <p>기존 join.jsp + join.js 검증 로직을 React로 옮길 자리입니다.</p>
        </div>

        <div className="page-placeholder-card">
          <strong>회원가입 화면 준비 영역</strong>
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

export default JoinPage;
