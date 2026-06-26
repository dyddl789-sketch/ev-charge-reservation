const SystemPage = () => {
  console.log('SystemPage 렌더링');

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>시스템관리</p>
          <h1>시스템 관리</h1>
          <span>최고관리자 전용 메뉴입니다. 운영 시뮬레이션 초기화와 권한 정책을 관리합니다.</span>
        </div>
      </div>

      <article className="admin-panel">
        <div className="admin-panel-title">
          <div>
            <strong>권한 정책</strong>
            <p>관리자 역할별 접근 범위를 확인합니다.</p>
          </div>
        </div>

        <div className="admin-guide-list">
          <p><b>ADMIN</b> 전체 메뉴 접근, 직원 권한 관리, 시뮬레이션 초기화 가능</p>
          <p><b>MANAGER</b> 운영 관리, 직원 등록 일부, 공공데이터 적재 가능</p>
          <p><b>OPERATOR</b> 예약 및 민원 운영 중심</p>
          <p><b>ENGINEER</b> 장애, 점검, 충전기 상태 관리 중심</p>
        </div>
      </article>
    </section>
  );
};

export default SystemPage;
