import { NavLink } from "react-router-dom";

const MyPageSidebar = () => {
  console.log("MyPageSidebar 렌더링");

  const menuList = [
    { path: "/mypage", label: "회원정보 변경", end: true },
    { path: "/my-reservations", label: "내 예약" },
    { path: "/charging-history", label: "충전내역" },
    { path: "/vehicles", label: "내 차량" },
  ];

  return (
    <aside className="mypage-sidebar">
      <div className="mypage-sidebar-title">
        <span>MY PAGE</span>
        <strong>마이페이지</strong>
      </div>

      <nav className="mypage-sidebar-nav">
        {menuList.map((menu) => (
          <NavLink
            key={menu.path}
            to={menu.path}
            end={menu.end}
            className={({ isActive }) =>
              isActive ? "mypage-side-link active" : "mypage-side-link"
            }
          >
            {menu.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};

export default MyPageSidebar;
