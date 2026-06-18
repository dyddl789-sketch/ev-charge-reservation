import { NavLink } from "react-router-dom";

const adminMenus = [
  {
    title: "대시보드",
    path: "/admin/dashboard",
  },
  {
    title: "회원관리",
    children: [
      { title: "회원 목록", path: "/admin/members" },
    ],
  },
  {
    title: "인사관리",
    children: [
      { title: "직원 등록", path: "/admin/employees/register" },
      { title: "직원 관리", path: "/admin/employees" },
    ],
  },
  {
    title: "충전소관리",
    children: [
      { title: "충전소 운영관리", path: "/admin/stations" },
      { title: "충전소 등록", path: "/admin/infrastructure" },
    ],
  },
  {
    title: "예약관리",
    children: [
      { title: "예약 현황", path: "/admin/reservations" },
    ],
  },
  {
    title: "민원관리",
    path: "/admin/complaints",
  },
  {
    title: "장애·점검관리",
    path: "/admin/faults",
  },
  {
    title: "전자결재",
    path: "/admin/approvals",
  },
  {
    title: "통계/분석",
    children: [
      { title: "이용 통계", path: "/admin/statistics/usage" },
      { title: "매출 통계", path: "/admin/statistics/sales" },
    ],
  },
];

const AdminSidebar = () => {
  console.log("AdminSidebar 렌더링");

  return (
    <aside className="admin-sidebar">
      <div className="admin-logo">
        <span>EV</span>
        <div>
          <strong>MIS</strong>
          <em>운영기관</em>
        </div>
      </div>

      <nav className="admin-menu">
        {adminMenus.map((menu) => (
          <div className="admin-menu-group" key={menu.title}>
            {menu.children ? (
              <>
                <div className="admin-menu-title">{menu.title}</div>
                <div className="admin-submenu">
                  {menu.children.map((child) => (
                    <NavLink key={child.path} to={child.path}>
                      {child.title}
                    </NavLink>
                  ))}
                </div>
              </>
            ) : (
              <NavLink to={menu.path}>{menu.title}</NavLink>
            )}
          </div>
        ))}
      </nav>
    </aside>
  );
};

export default AdminSidebar;
