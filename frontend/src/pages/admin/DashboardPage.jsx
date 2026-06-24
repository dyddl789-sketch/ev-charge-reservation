import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useOutletContext } from 'react-router-dom';
import * as adminApi from '../../apis/adminApi';
import { getRole, hasAnyRole } from '../../utils/adminRoleUtils';

const numberText = (value) => Number(value || 0).toLocaleString('ko-KR');
const moneyText = (value) => `${Number(value || 0).toLocaleString('ko-KR')}원`;

const DashboardPage = () => {
  console.log('DashboardPage 렌더링');

  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const currentUser = outletContext?.currentUser;
  const currentRole = getRole(currentUser);

  const [dashboard, setDashboard] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [operationMessage, setOperationMessage] = useState('');
  const [isOperating, setIsOperating] = useState(false);

  const canSyncPublicData = hasAnyRole(currentRole, ['ADMIN', 'MANAGER']);
  const canTriggerFault = hasAnyRole(currentRole, ['ADMIN', 'MANAGER', 'ENGINEER']);
  const canResetSimulation = hasAnyRole(currentRole, ['ADMIN']);

  const chargerStatusList = useMemo(() => {
    const total = Number(dashboard?.totalChargerCount || 0);

    return [
      { label: '사용가능', value: Number(dashboard?.availableChargerCount || 0), className: 'green' },
      { label: '사용중', value: Number(dashboard?.chargingChargerCount || 0), className: 'blue' },
      { label: '예약중', value: Number(dashboard?.reservedChargerCount || 0), className: 'purple' },
      { label: '점검/고장', value: Number(dashboard?.troubleChargerCount || 0), className: 'red' },
    ].map((item) => ({
      ...item,
      rate: total > 0 ? Math.round((item.value / total) * 1000) / 10 : 0,
    }));
  }, [dashboard]);

  const loadDashboard = async () => {
    console.log('대시보드 데이터 조회');

    try {
      setIsLoading(true);
      const response = await adminApi.dashboard();
      console.log('대시보드 데이터 응답', response.data);
      setDashboard(response.data);
    } catch (error) {
      console.log('대시보드 데이터 조회 실패', error);
      setDashboard(null);
      alert('대시보드 데이터를 불러오지 못했습니다. 로그인 권한 또는 백엔드 API를 확인해 주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  const runOperation = async (type) => {
    console.log('대시보드 운영 버튼 클릭', type);

    if (type === 'sync' && !canSyncPublicData) {
      alert('공공데이터 적재 권한이 없습니다.');
      return;
    }

    if (type === 'fault' && !canTriggerFault) {
      alert('장애 시뮬레이션 권한이 없습니다.');
      return;
    }

    if (type === 'reset' && !canResetSimulation) {
      alert('시뮬레이션 초기화는 최고관리자만 실행할 수 있습니다.');
      return;
    }

    const confirmMessage = {
      sync: '전국 시도별로 공공데이터 샘플을 적재하시겠습니까? 기존 충전기 운영 상태는 덮어쓰지 않습니다.',
      fault: '부산 지역 사용가능 충전기 중 1대를 고장 상태로 변경하시겠습니까?',
      reset: '고장 상태 충전기를 모두 사용가능으로 초기화하시겠습니까? 예약 데이터는 변경하지 않습니다.',
    }[type];

    if (!window.confirm(confirmMessage)) {
      return;
    }

    try {
      setIsOperating(true);
      setOperationMessage('처리 중입니다...');

      const response =
        type === 'sync'
          ? await adminApi.syncPublicChargerSample(10)
          : type === 'fault'
            ? await adminApi.triggerFaultSimulation()
            : await adminApi.resetSimulation();

      console.log('운영 버튼 처리 응답', response.data);
      setOperationMessage(response.data?.message || '처리가 완료되었습니다.');
      await loadDashboard();
    } catch (error) {
      console.log('운영 버튼 처리 실패', error);
      setOperationMessage(error.response?.data?.message || '처리 중 오류가 발생했습니다.');
      alert(error.response?.data?.message || '처리 중 오류가 발생했습니다.');
    } finally {
      setIsOperating(false);
    }
  };

  const moveToStationMap = (charger) => {
    console.log('장애 충전기 지도에서 보기', charger);

    const params = new URLSearchParams({
      focusType: 'station',
      stationId: String(charger.stationId),
      lat: String(charger.latitude || ''),
      lng: String(charger.longitude || ''),
      name: charger.stationName || '',
      address: charger.address || '',
    });

    navigate(`/stations?${params.toString()}`);
  };

  const moveToRelatedComplaints = (charger) => {
    console.log('관련 민원 확인', charger);

    const params = new URLSearchParams({
      stationId: String(charger.stationId),
      chargerId: String(charger.chargerId),
    });

    navigate(`/admin/complaints?${params.toString()}`);
  };

  const moveToChargerDetail = (charger) => {
    console.log('충전기 상세보기', charger);
    navigate(`/admin/stations?stationId=${charger.stationId}&chargerId=${charger.chargerId}`);
  };

  const kpiList = [
    { label: '총 회원', value: `${numberText(dashboard?.totalMemberCount)}명`, note: `오늘 신규 ${numberText(dashboard?.memberIncreaseCount)}명` },
    { label: '총 충전소', value: `${numberText(dashboard?.totalStationCount)}개`, note: `오늘 신규 ${numberText(dashboard?.stationIncreaseCount)}개` },
    { label: '총 충전기', value: `${numberText(dashboard?.totalChargerCount)}대`, note: `사용가능 ${numberText(dashboard?.availableChargerCount)}대` },
    { label: '오늘 예약', value: `${numberText(dashboard?.todayReservationCount)}건`, note: `전일 대비 ${numberText(dashboard?.reservationIncreaseCount)}건` },
    { label: '오늘 매출', value: moneyText(dashboard?.todaySalesAmount), note: '완료된 충전 세션 기준' },
  ];

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>운영기관 MIS</p>
          <h1>MIS 대시보드</h1>
          <span>예약, 충전기 상태, 공공데이터 적재, 장애 시뮬레이션을 한 화면에서 확인합니다.</span>
        </div>
        <button type="button" className="admin-outline-button" onClick={loadDashboard} disabled={isLoading}>
          {isLoading ? '새로고침 중' : '대시보드 새로고침'}
        </button>
      </div>

      <div className="admin-kpi-grid">
        {kpiList.map((item) => (
          <article className="admin-kpi-card" key={item.label}>
            <span>{item.label}</span>
            <strong>{item.value}</strong>
            <p>{item.note}</p>
          </article>
        ))}
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel simulation-panel">
          <div className="admin-panel-title">
            <div>
              <strong>운영 시뮬레이션</strong>
              <p>공공데이터 적재, 부산 장애 발생, 발표용 초기화를 빠르게 실행합니다.</p>
            </div>
          </div>

          <div className="simulation-button-grid">
            <button type="button" className="simulation-btn blue" disabled={!canSyncPublicData || isOperating} onClick={() => runOperation('sync')}>
              <b>공공데이터 샘플 적재</b>
              <span>전국 시도별 약 10개씩 저장</span>
              {!canSyncPublicData && <em>권한 없음</em>}
            </button>

            <button type="button" className="simulation-btn red" disabled={!canTriggerFault || isOperating} onClick={() => runOperation('fault')}>
              <b>장애 시뮬레이션 발생</b>
              <span>부산 사용가능 충전기 1대 고장 처리</span>
              {!canTriggerFault && <em>권한 없음</em>}
            </button>

            <button type="button" className="simulation-btn dark" disabled={!canResetSimulation || isOperating} onClick={() => runOperation('reset')}>
              <b>시뮬레이션 초기화</b>
              <span>고장 충전기만 사용가능으로 복구</span>
              {!canResetSimulation && <em>ADMIN 전용</em>}
            </button>
          </div>

          {operationMessage && <div className="operation-message">{operationMessage}</div>}
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>충전기 상태 현황</strong>
          </div>

          <div className="charger-status-grid">
            {chargerStatusList.map((item) => (
              <div className={`charger-status-card ${item.className}`} key={item.label}>
                <span>{item.label}</span>
                <strong>{numberText(item.value)}대</strong>
                <p>{item.rate}%</p>
              </div>
            ))}
          </div>
        </article>
      </div>

      <div className="admin-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>최근 장애 발생 충전기</strong>
              <p>부산 장애 시뮬레이션으로 고장 처리된 충전기를 바로 확인합니다.</p>
            </div>
          </div>

          <div className="admin-fault-card-list">
            {(dashboard?.faultChargerList || []).length === 0 && <div className="empty-box">현재 고장 상태 충전기가 없습니다.</div>}
            {(dashboard?.faultChargerList || []).map((charger) => (
              <article className="admin-fault-card" key={charger.chargerId}>
                <div className="admin-fault-main">
                  <div>
                    <span>{charger.regionName || '지역'} · {charger.status}</span>
                    <h3>{charger.stationName}</h3>
                    <p>{charger.address}</p>
                  </div>
                  <em className="admin-badge danger">고장</em>
                </div>
                <div className="admin-fault-meta">
                  <span>충전기 <b>{charger.chargerName}</b></span>
                  <span>커넥터 <b>{charger.connectorType}</b></span>
                  <span>출력 <b>{charger.chargingSpeedKw}kW</b></span>
                  <span>발생시각 <b>{charger.updatedAtText || '-'}</b></span>
                </div>
                <div className="admin-action-row">
                  <button type="button" onClick={() => moveToStationMap(charger)}>지도에서 보기</button>
                  <button type="button" onClick={() => moveToRelatedComplaints(charger)}>관련 민원 확인</button>
                  <button type="button" onClick={() => moveToChargerDetail(charger)}>충전기 상세보기</button>
                </div>
              </article>
            ))}
          </div>
        </article>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>운영 업무 흐름</strong>
              <p>사용자 민원과 시스템 감지 장애가 MIS 업무 흐름으로 연결됩니다.</p>
            </div>
          </div>

          <div className="admin-flow-row">
            {['장애 감지', '사용자 민원', '운영팀 확인', '장애·점검', '조치 완료'].map((step, index) => (
              <div className="admin-flow-step" key={step}>
                <span>{index + 1}</span>
                <b>{step}</b>
              </div>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>빠른 이동</strong>
          </div>
          <div className="admin-quick-links">
            <Link to="/admin/reservations">예약 현황</Link>
            <Link to="/admin/complaints">민원관리</Link>
            <Link to="/admin/faults">장애·점검관리</Link>
            <Link to="/admin/employees">직원관리</Link>
          </div>
        </article>
      </div>

      <div className="admin-grid">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>최근 운영 알림</strong>
          </div>
          <div className="admin-list">
            {(dashboard?.noticeList || []).length === 0 && <div className="empty-box">최근 운영 알림이 없습니다.</div>}
            {(dashboard?.noticeList || []).map((item, index) => (
              <div className="admin-list-row" key={`${item.message}-${index}`}>
                <div>
                  <b>{item.message}</b>
                  <span>{item.timeText || item.createdAt || '-'}</span>
                </div>
                <em className="admin-badge blue">알림</em>
              </div>
            ))}
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>충전소 운영 현황</strong>
            <Link to="/admin/stations">전체보기</Link>
          </div>
          <div className="admin-list">
            {(dashboard?.stationStatusList || []).length === 0 && <div className="empty-box">충전소 데이터가 없습니다.</div>}
            {(dashboard?.stationStatusList || []).map((item) => (
              <div className="admin-list-row" key={item.stationId}>
                <div>
                  <b>{item.stationName}</b>
                  <span>
                    전체 {numberText(item.totalChargerCount)}대 · 사용가능 {numberText(item.availableChargerCount)}대 · 고장/점검 {numberText(item.troubleChargerCount)}대
                  </span>
                </div>
                <em className={`admin-badge ${Number(item.troubleChargerCount) > 0 ? 'warning' : 'green'}`}>
                  {Number(item.troubleChargerCount) > 0 ? '주의' : '정상'}
                </em>
              </div>
            ))}
          </div>
        </article>
      </div>
    </section>
  );
};

export default DashboardPage;
