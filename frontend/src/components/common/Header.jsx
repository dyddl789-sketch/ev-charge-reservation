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
          <div className="nav-dropdown">
            <Link to="/customer-center" className="nav-main-link">
              고객센터
            </Link>

            <div className="nav-dropdown-menu">
              <Link to="/customer-center">고객센터 홈</Link>
              <Link to="/complaint">민원 접수</Link>
              <Link to="/complaints/my">내 민원 내역</Link>
              <Link to="/customer-center">FAQ</Link>
              <Link to="/customer-center">이용안내</Link>
            </div>
          </div>

          <div className="nav-dropdown">
            <Link to="/mypage" className="nav-main-link">
              마이페이지
            </Link>

            <div className="nav-dropdown-menu">
              <Link to="/mypage">마이페이지 홈</Link>
              <Link to="/mypage">회원정보 변경</Link>
              <Link to="/vehicles">내 차량 관리</Link>
              <Link to="/my-reservations">내 예약 조회</Link>
              <Link to="/charging-history">충전 이용 내역</Link>
            </div>
          </div>

          <Link to="/notice">공지사항</Link>
          <Link to="/stations">충전소찾기</Link>
          <Link to="/ai-chat">AI채팅</Link>
        </nav>
      </div>
    </header>
  );
};

export default Header;