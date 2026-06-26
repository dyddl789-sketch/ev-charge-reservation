import { useEffect, useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import * as authApi from '../apis/authApi';
import AdminSidebar from '../components/admin/AdminSidebar';
import '../styles/admin.css';

const AdminLayout = () => {
  console.log('AdminLayout 렌더링');

  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const loadMyInfo = async () => {
      console.log('관리자 현재 로그인 정보 조회');

      try {
        const response = await authApi.getMyInfo();
        console.log('관리자 현재 로그인 정보 응답', response.data);
        setCurrentUser(response.data);
        localStorage.setItem('USER_TYPE', response.data.userType || '');
        localStorage.setItem('ROLE', response.data.role || '');
        localStorage.setItem('MEMBER_NAME', response.data.memberName || '');
      } catch (error) {
        console.log('관리자 로그인 정보 조회 실패', error);
        alert('관리자 화면은 로그인이 필요합니다.');
        navigate('/login?authMsg=loginRequired');
      }
    };

    loadMyInfo();
  }, [navigate]);

  const logout = async () => {
    console.log('관리자 로그아웃 클릭');

    try {
      await authApi.logout();
    } catch (error) {
      console.log('관리자 로그아웃 API 실패', error);
    } finally {
      localStorage.removeItem('ACCESS_TOKEN');
      localStorage.removeItem('REFRESH_TOKEN');
      localStorage.removeItem('USER_TYPE');
      localStorage.removeItem('ROLE');
      localStorage.removeItem('MEMBER_NAME');
      window.dispatchEvent(new Event('auth-change'));
      navigate('/login?logout=1');
    }
  };

  return (
    <div className="admin-layout">
      <AdminSidebar currentUser={currentUser} />

      <div className="admin-content-wrap">
        <header className="admin-topbar">
          <div>
            <strong>공공 전기차 충전 인프라 운영 MIS</strong>
            <span>
              {currentUser
                ? `${currentUser.memberName} / ${currentUser.userType}`
                : '운영기관 업무관리 시스템'}
            </span>
          </div>

          <div className="admin-topbar-actions">
            <Link to="/admin/dashboard">대시보드</Link>
            <Link to="/admin/profile">내 정보</Link>
            <Link to="/">사용자 화면</Link>
            <button type="button" onClick={logout}>
              로그아웃
            </button>
          </div>
        </header>

        <main className="admin-main">
          <Outlet context={{ currentUser }} />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
