import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const OAuth2RedirectPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");

    console.log("카카오 로그인 Redirect");

    if (accessToken) {
      localStorage.setItem("ACCESS_TOKEN", accessToken);
    }

    if (refreshToken) {
      localStorage.setItem("REFRESH_TOKEN", refreshToken);
    }

    window.dispatchEvent(new Event("auth-change"));

    navigate("/");
  }, [navigate, searchParams]);

  return <div>카카오 로그인 처리중...</div>;
};

export default OAuth2RedirectPage;