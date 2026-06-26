import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import * as authApi from "../../apis/authApi";
import { getRole, hasAnyRole } from "../../utils/adminRoleUtils";

const Header = () => {
  console.log("Header 렌더링");

  const navigate = useNavigate();
  const [loginMember, setLoginMember] = useState(null);

  const getLoginMember = async () => {
    console.log("Header 로그인 정보 조회");

    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (!accessToken) {
      setLoginMember(null);
      return;
    }

    try {
      const response = await authApi.getMyInfo();
      console.log("Header 로그인 정보 응답", response.data);
      setLoginMember(response.data);
    } catch (error) {
      console.log("Header 로그인 정보 조회 실패", error);
      setLoginMember(null);
    }
  };

  useEffect(() => {
    getLoginMember();

    window.addEventListener("auth-change", getLoginMember);

    return () => {
      window.removeEventListener("auth-change", getLoginMember);
    };
  }, []);

  const logout = async () => {
    console.log("로그아웃 클릭");

    try {
      await authApi.logout();
    } catch (error) {
      console.log("로그아웃 API 오류", error);
    }

    localStorage.removeItem("ACCESS_TOKEN");
    localStorage.removeItem("REFRESH_TOKEN");

    setLoginMember(null);
    window.dispatchEvent(new Event("auth-change"));

    navigate("/");
  };

  const currentRole = getRole(loginMember);
  const isMisUser = hasAnyRole(currentRole, ["ADMIN", "MANAGER", "OPERATOR", "ENGINEER"]);

  return (
    <header className="public-header">
      <div className="top-util">
        <div className="top-util-inner">
          <span>공공 전기차 충전 인프라 운영 플랫폼</span>

          <div className="top-links">
            {loginMember ? (
              <>
                <span>{loginMember.memberName || loginMember.userId}님</span>
                <Link to="/mypage">마이페이지</Link>
                <button type="button" onClick={logout} className="top-link-button">
                  로그아웃
                </button>

                {isMisUser && (
                  <Link to="/admin/dashboard">운영기관 MIS</Link>
                )}
              </>
            ) : (
              <>
                <Link to="/login">로그인</Link>
                <Link to="/join">회원가입</Link>
              </>
            )}
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
          <Link to="/customer-center">고객센터</Link>
          <Link to="/mypage">마이페이지</Link>
          <Link to="/notice">공지사항</Link>
          <Link to="/stations">충전소찾기</Link>
          <Link to="/ai-chat">AI채팅</Link>
        </nav>
      </div>
    </header>
  );
};

export default Header;