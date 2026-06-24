// 관리자 권한별 메뉴/버튼 제어 유틸
export const ROLE_LABELS = {
  ADMIN: '기관장 / 최고관리자',
  MANAGER: '운영관리자',
  OPERATOR: '운영담당자',
  ENGINEER: '시설관리담당자',
  USER: '일반회원',
};

const normalizeRole = (role) => {
  if (!role) {
    return '';
  }

  return String(role).replace('ROLE_', '').toUpperCase();
};

export const getRole = (user) => {
  const role = user?.userType || user?.role || localStorage.getItem('USER_TYPE') || localStorage.getItem('ROLE');
  return normalizeRole(role);
};

export const hasAnyRole = (currentRole, allowedRoles = []) => {
  const role = normalizeRole(currentRole);
  return allowedRoles.map(normalizeRole).includes(role);
};

export const canCreateEmployeeRole = (currentRole, targetRole) => {
  const role = normalizeRole(currentRole);
  const target = normalizeRole(targetRole);

  if (role === 'ADMIN') {
    return ['ADMIN', 'MANAGER', 'OPERATOR', 'ENGINEER'].includes(target);
  }

  if (role === 'MANAGER') {
    return ['OPERATOR', 'ENGINEER'].includes(target);
  }

  return false;
};

export const getCreatableRoles = (currentRole) => {
  const role = normalizeRole(currentRole);

  if (role === 'ADMIN') {
    return [
      { value: 'ADMIN', label: 'ADMIN - 기관장/최고관리자' },
      { value: 'MANAGER', label: 'MANAGER - 운영관리자' },
      { value: 'OPERATOR', label: 'OPERATOR - 운영담당자' },
      { value: 'ENGINEER', label: 'ENGINEER - 시설관리담당자' },
    ];
  }

  if (role === 'MANAGER') {
    return [
      { value: 'OPERATOR', label: 'OPERATOR - 운영담당자' },
      { value: 'ENGINEER', label: 'ENGINEER - 시설관리담당자' },
    ];
  }

  return [];
};
