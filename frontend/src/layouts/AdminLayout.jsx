import { Link, Outlet } from "react-router-dom";
import Sidebar from "../components/common/Sidebar";
import "../styles/admin.css";

const AdminLayout = () => {
  console.log("AdminLayout 렌더링");

  return (
    <div className="admin-layout">
      <Sidebar />

      <div className="admin-content-wrap">
        <header className="admin-topbar">
          <div>
            <strong>공공 전기차 충전 인프라 운영 MIS</strong>
            <span>운영기관 업무관리 시스템</span>
          </div>

          <div className="admin-topbar-actions">
            <Link to="/">사용자 메인</Link>
            <button type="button" onClick={() => console.log("관리자 로그아웃 클릭")}>로그아웃</button>
          </div>
        </header>

        <main className="admin-main">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
