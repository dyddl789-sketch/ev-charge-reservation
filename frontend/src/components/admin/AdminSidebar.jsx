import { NavLink } from 'react-router-dom';
import { getRole, hasAnyRole } from '../../utils/adminRoleUtils';

const adminMenus = [
  {
    title: '대시보드',
    path: '/admin/dashboard',
    roles: ['ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'],
  },
  {
    title: '내 정보',
    path: '/admin/profile',
    roles: ['ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'],
  },
  {
    title: '회원관리',
    roles: ['ADMIN', 'MANAGER', 'OPERATOR'],
    children: [{ title: '회원 목록', path: '/admin/members', roles: ['ADMIN', 'MANAGER', 'OPERATOR'] }],
  },
  {
    title: '인사관리',
    roles: ['ADMIN', 'MANAGER'],
    children: [
      { title: '직원 등록', path: '/admin/employees/register', roles: ['ADMIN', 'MANAGER'] },
      { title: '직원 관리', path: '/admin/employees', roles: ['ADMIN', 'MANAGER'] },
    ],
  },
  {
    title: '충전소관리',
    roles: ['ADMIN', 'MANAGER', 'ENGINEER'],
    children: [
      { title: '충전소 운영관리', path: '/admin/stations', roles: ['ADMIN', 'MANAGER', 'ENGINEER'] },
      { title: '충전소 등록', path: '/admin/infrastructure', roles: ['ADMIN', 'MANAGER'] },
    ],
  },
  {
    title: '예약관리',
    roles: ['ADMIN', 'MANAGER', 'OPERATOR'],
    children: [{ title: '예약 현황', path: '/admin/reservations', roles: ['ADMIN', 'MANAGER', 'OPERATOR'] }],
  },
  {
    title: '민원관리',
    path: '/admin/complaints',
    roles: ['ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'],
  },
  {
    title: '장애·점검관리',
    path: '/admin/faults',
    roles: ['ADMIN', 'MANAGER', 'ENGINEER'],
  },
  {
    title: '전자결재',
    path: '/admin/approvals',
    roles: ['ADMIN', 'MANAGER', 'ENGINEER'],
  },
  {
    title: '통계/분석',
    roles: ['ADMIN', 'MANAGER'],
    children: [
      { title: '이용 통계', path: '/admin/statistics/usage', roles: ['ADMIN', 'MANAGER'] },
      { title: '매출 통계', path: '/admin/statistics/sales', roles: ['ADMIN', 'MANAGER'] },
    ],
  },
  {
    title: '시스템관리',
    path: '/admin/system',
    roles: ['ADMIN'],
  },
];

const LockedMenu = ({ title }) => (
  <button
    type="button"
    className="admin-menu-locked"
    onClick={() => {
      console.log('권한 없는 메뉴 클릭', title);
      alert('현재 권한으로는 접근할 수 없는 메뉴입니다.');
    }}
  >
    <span>{title}</span>
    <em>🔒</em>
  </button>
);

const AdminSidebar = ({ currentUser }) => {
  console.log('AdminSidebar 렌더링', currentUser);

  const currentRole = getRole(currentUser);

  const renderMenuLink = (item) => {
    const allowed = hasAnyRole(currentRole, item.roles);

    if (!allowed) {
      return <LockedMenu key={item.path || item.title} title={item.title} />;
    }

    return (
      <NavLink key={item.path} to={item.path} end>
        {item.title}
      </NavLink>
    );
  };

  return (
    <aside className="admin-sidebar">
      <div className="admin-logo">
        <span>EV</span>
        <div>
          <strong>MIS</strong>
          <em>{currentRole || '권한 확인중'}</em>
        </div>
      </div>

      <nav className="admin-menu">
        {adminMenus.map((menu) => {
          const groupAllowed = hasAnyRole(currentRole, menu.roles);

          return (
            <div className={`admin-menu-group ${!groupAllowed ? 'locked-group' : ''}`} key={menu.title}>
              {menu.children ? (
                <>
                  <div className="admin-menu-title">
                    {menu.title}
                    {!groupAllowed && <em>🔒</em>}
                  </div>

                  <div className="admin-submenu">
                    {menu.children.map((child) => renderMenuLink(child))}
                  </div>
                </>
              ) : (
                renderMenuLink(menu)
              )}
            </div>
          );
        })}
      </nav>
    </aside>
  );
};

export default AdminSidebar;
