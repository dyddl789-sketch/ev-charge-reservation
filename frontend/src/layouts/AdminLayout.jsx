import { Link, Outlet } from "react-router-dom";
import AdminSidebar from "../components/admin/AdminSidebar";
import "../styles/admin.css";

const AdminLayout = () => {
  console.log("AdminLayout 렌더링");

  return (
    <div className="admin-layout">
      <AdminSidebar />

      <div className="admin-content-wrap">
        <header className="admin-topbar">
          <div>
            <strong>공공 전기차 충전 인프라 운영 MIS</strong>
            <span>운영기관 업무관리 시스템</span>
          </div>

          <div className="admin-topbar-actions">
            <Link to="/admin/dashboard">대시보드</Link>
            <Link to="/">사용자 화면</Link>
            <button type="button" onClick={() => console.log("관리자 로그아웃 클릭")}>
              로그아웃
            </button>
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
