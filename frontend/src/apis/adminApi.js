import api from './api';

// 관리자 대시보드 조회
export const dashboard = (params = {}) => {
  console.log('관리자 대시보드 요청', params);
  return api.get('/admin/dashboard', { params });
};

// MIS 대시보드: 환경부 공공데이터 시도별 샘플 적재
export const syncPublicChargerSample = (limitPerRegion = 10) => {
  console.log('공공데이터 샘플 적재 요청', limitPerRegion);
  return api.post('/admin/public-api/chargers/sync-sample', null, {
    params: { limitPerRegion },
  });
};

// MIS 대시보드: 장애 시뮬레이션 발생
export const triggerFaultSimulation = () => {
  console.log('장애 시뮬레이션 발생 요청');
  return api.post('/admin/simulation/fault');
};

// MIS 대시보드: 시뮬레이션 초기화
export const resetSimulation = () => {
  console.log('시뮬레이션 초기화 요청');
  return api.post('/admin/simulation/reset');
};

// 회원 목록 조회
export const members = (params = {}) => {
  console.log('관리자 회원 목록 요청', params);
  return api.get('/admin/member/list', { params });
};

// 회원 상세 조회
export const member = (memberId) => {
  console.log('관리자 회원 상세 요청', memberId);
  return api.get('/admin/member/detail', {
    params: { memberId },
  });
};

// 회원 탈퇴 처리
export const withdrawMember = (memberId) => {
  console.log('관리자 회원 탈퇴 처리 요청', memberId);
  return api.post('/admin/member/withdraw', { memberId });
};

// 회원 복구 처리
export const restoreMember = (memberId) => {
  console.log('관리자 회원 복구 처리 요청', memberId);
  return api.post('/admin/member/restore', { memberId });
};

// 충전소 목록 조회
export const stations = (params = {}) => {
  console.log('관리자 충전소 목록 요청', params);
  return api.get('/admin/station/list', { params });
};

// 충전소 상세 조회
export const stationDetail = (stationId) => {
  console.log('관리자 충전소 상세 요청', stationId);
  return api.get('/admin/station/detail', {
    params: { stationId },
  });
};

// 충전소 등록
export const registerStation = (stationData) => {
  console.log('관리자 충전소 등록 요청', stationData);
  return api.post('/admin/station/register', stationData);
};

// 충전소 수정
export const updateStation = (stationData) => {
  console.log('관리자 충전소 수정 요청', stationData);
  return api.post('/admin/station/update', stationData);
};

// 충전소 상태 변경
export const updateStationStatus = (stationId, status) => {
  console.log('관리자 충전소 상태 변경 요청', stationId, status);
  return api.post('/admin/station/status', {
    stationId,
    status,
  });
};

// 충전소별 충전기 목록 조회
export const stationChargers = (stationId) => {
  console.log('관리자 충전소별 충전기 목록 요청', stationId);
  return api.get('/admin/station/chargers', {
    params: { stationId },
  });
};

// 충전기 등록
export const registerCharger = (chargerData) => {
  console.log('관리자 충전기 등록 요청', chargerData);
  return api.post('/admin/charger/register', chargerData);
};

// 충전기 상태 변경
export const updateChargerStatus = (chargerId, status) => {
  console.log('관리자 충전기 상태 변경 요청', chargerId, status);
  return api.post('/admin/charger/status', {
    chargerId,
    status,
  });
};

// 예약 목록 조회
export const reservations = (params = {}) => {
  console.log('관리자 예약 목록 요청', params);
  return api.get('/admin/reservation/list', { params });
};

// 예약 상세 조회
export const reservationDetail = (reservationId) => {
  console.log('관리자 예약 상세 요청', reservationId);
  return api.get('/admin/reservation/detail', {
    params: { reservationId },
  });
};

// 예약 취소
export const cancelReservation = (reservationId) => {
  console.log('관리자 예약 취소 요청', reservationId);
  return api.post('/admin/reservation/cancel', { reservationId });
};

// 노쇼 처리
export const noShowReservation = (reservationId) => {
  console.log('관리자 노쇼 처리 요청', reservationId);
  return api.post('/admin/reservation/noshow', { reservationId });
};

// 충전 시작 처리
export const startReservation = (reservationId) => {
  console.log('관리자 충전 시작 처리 요청', reservationId);
  return api.post('/admin/reservation/start', { reservationId });
};

// 이용 통계 조회
export const usageStatistics = (params = {}) => {
  console.log('관리자 이용 통계 요청', params);
  return api.get('/admin/usage', { params });
};

// 매출 통계 조회
export const salesStatistics = (params = {}) => {
  console.log('관리자 매출 통계 요청', params);
  return api.get('/admin/sales', { params });
};

// MIS 인사관리: 직원 목록
export const employees = (params = {}) => {
  console.log('관리자 직원 목록 요청', params);
  return api.get('/admin/employees', { params });
};

// MIS 인사관리: 직원 상세
export const employeeDetail = (employeeId) => {
  console.log('관리자 직원 상세 요청', employeeId);
  return api.get(`/admin/employees/${employeeId}`);
};

// MIS 인사관리: 부서 목록
export const departments = () => {
  console.log('관리자 부서 목록 요청');
  return api.get('/admin/departments');
};

// MIS 인사관리: 직원 등록
export const registerEmployee = (employeeData) => {
  console.log('관리자 직원 등록 요청', employeeData);
  return api.post('/admin/employees', employeeData);
};

// MIS 인사관리: 직원 정보 수정
export const updateEmployee = (employeeId, employeeData) => {
  console.log('관리자 직원 정보 수정 요청', employeeId, employeeData);
  return api.put(`/admin/employees/${employeeId}`, employeeData);
};

// MIS 인사관리: 직원 상태 변경
export const updateEmployeeStatus = (employeeId, statusData) => {
  console.log('관리자 직원 상태 변경 요청', employeeId, statusData);
  return api.put(`/admin/employees/${employeeId}/status`, statusData);
};

// MIS 민원 목록
export const complaints = (params = {}) => {
  console.log('관리자 민원 목록 요청', params);
  return api.get('/admin/complaints', { params });
};

// MIS 장애·점검 목록
export const faults = (params = {}) => {
  console.log('관리자 장애·점검 목록 요청', params);
  return api.get('/admin/faults', { params });
};

// MIS 전자결재 목록
export const approvals = (params = {}) => {
  console.log('관리자 전자결재 목록 요청', params);
  return api.get('/admin/approvals', { params });
};

// 관리자 공지사항 목록 조회
export const notices = (params = {}) => {
  console.log('관리자 공지사항 목록 요청', params);
  return api.get('/admin/notice/list', { params });
};

// 관리자 공지사항 상세 조회
export const noticeDetail = (noticeId) => {
  console.log('관리자 공지사항 상세 요청', noticeId);
  return api.get(`/admin/notice/${noticeId}`);
};

// 관리자 공지사항 등록
export const createNotice = (noticeData) => {
  console.log('관리자 공지사항 등록 요청', noticeData);
  return api.post('/admin/notice', noticeData);
};

// 관리자 공지사항 수정
export const updateNotice = (noticeId, noticeData) => {
  console.log('관리자 공지사항 수정 요청', noticeId, noticeData);
  return api.put(`/admin/notice/${noticeId}`, noticeData);
};

// 관리자 공지사항 삭제 처리
export const deleteNotice = (noticeId) => {
  console.log('관리자 공지사항 삭제 요청', noticeId);
  return api.delete(`/admin/notice/${noticeId}`);
};
