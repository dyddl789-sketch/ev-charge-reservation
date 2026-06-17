import { Link } from "react-router-dom";

const Header = () => {
  console.log("Header 렌더링");

  return (
    <header className="public-header">
      <div className="top-util">
        <div className="top-util-inner">
          <span>공공 전기차 충전 인프라 운영 플랫폼</span>

          <div className="top-links">
            <Link to="/login">로그인</Link>
            <Link to="/join">회원가입</Link>
            <Link to="/admin/dashboard">운영기관 MIS</Link>
          </div>
        </div>
      </div>

      <div className="header-inner">
        <Link to="/" className="site-logo">
          <span className="logo-symbol">EV</span>
          <span className="logo-title">
            공공 전기차 충전
            <strong>인프라 운영 플랫폼</strong>
          </span>
        </Link>

        <nav className="main-nav">
          <Link to="/stations">충전소 찾기</Link>
          <Link to="/reservation">충전 예약</Link>
          <Link to="/ai-chat">AI 충전 비서</Link>
          <Link to="/complaint">민원 접수</Link>
          <Link to="/notice">새소식</Link>
        </nav>
      </div>
    </header>
  );
};

export default Header;