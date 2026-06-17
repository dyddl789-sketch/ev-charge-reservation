import { NavLink } from "react-router-dom";

const adminMenus = [
  { path: "/admin/dashboard", label: "대시보드" },
  { path: "/admin/members", label: "회원관리" },
  { path: "/admin/employees", label: "인사관리" },
  { path: "/admin/infrastructure", label: "충전인프라관리" },
  { path: "/admin/stations", label: "충전소관리" },
  { path: "/admin/chargers", label: "충전기관리" },
  { path: "/admin/reservations", label: "예약관리" },
  { path: "/admin/complaints", label: "민원관리" },
  { path: "/admin/faults", label: "장애관리" },
  { path: "/admin/inspections", label: "점검관리" },
  { path: "/admin/approvals", label: "전자결재" },
  { path: "/admin/statistics", label: "통계관리" },
  { path: "/admin/system", label: "시스템관리" },
];

const Sidebar = () => {
  console.log("Sidebar 렌더링");

  return (
    <aside className="admin-sidebar">
      <div className="admin-logo">
        <span>EV</span>
        <strong>MIS</strong>
      </div>

      <nav className="admin-menu">
        {adminMenus.map((menu) => (
          <NavLink key={menu.path} to={menu.path}>
            {menu.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};

export default Sidebar;
