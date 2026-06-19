import { Link, useSearchParams } from "react-router-dom";
import "../../styles/auth.css";

const LoginPage = () => {
  console.log("LoginPage 렌더링");

  const [searchParams] = useSearchParams();
  const authMsg = searchParams.get("authMsg");
  const error = searchParams.get("error");
  const logout = searchParams.get("logout");

  const handleSubmit = () => {
    console.log("로그인 form submit");
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <section className="login-visual">
          <Link to="/main" className="visual-link">
            <div className="visual-overlay">
              <h1>EV Charge</h1>
              <p>전기차 충전 최적화 및 예약 관리 서비스</p>
            </div>
          </Link>
        </section>

        <section className="login-form-area">
          <div className="login-header">
            <h2>로그인</h2>
            <p>서비스를 이용하려면 로그인하세요.</p>

            {authMsg === "loginRequired" && (
              <div className="error-message">로그인이 필요한 페이지입니다.</div>
            )}

            {error && <div className="error-message">아이디 또는 비밀번호를 확인해 주세요.</div>}
            {logout && <div className="success-message">로그아웃되었습니다.</div>}
          </div>

          {/* 기존 Spring Security /login form 처리와 연결 */}
          <form action="/login" method="post" className="login-form" onSubmit={handleSubmit}>
            <div className="input-box">
              <span className="input-icon">👤</span>
              <input type="text" name="userId" placeholder="아이디" autoComplete="username" />
            </div>

            <div className="input-box">
              <span className="input-icon">🔒</span>
              <input type="password" name="password" placeholder="비밀번호" autoComplete="current-password" />
            </div>

            <div className="login-options">
              <label>
                <input type="checkbox" name="rememberId" />
                아이디 저장
              </label>

              <Link to="/member/find">아이디/비밀번호 찾기</Link>
            </div>

            <button type="submit" className="login-btn">
              로그인
            </button>
          </form>

          <a href="/oauth2/authorization/kakao" className="kakao-btn">
            카카오로 로그인
          </a>

          <Link to="/join" className="login-join-btn">
            회원가입
          </Link>
        </section>
      </div>
    </div>
  );
};

export default LoginPage;
