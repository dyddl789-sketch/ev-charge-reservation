// MIS 직원등록 부서-직책-권한 자동 매핑 유틸
export const DEPARTMENT_ROLE_OPTIONS = {
  OPS: [
    {
      positionName: '운영담당자',
      userType: 'OPERATOR',
      dutyName: '민원 및 예약 운영',
      label: '운영담당자 - OPERATOR',
    },
    {
      positionName: '운영관리자',
      userType: 'MANAGER',
      dutyName: '운영 업무 배정 및 1차 승인',
      label: '운영관리자 - MANAGER',
    },
  ],
  FACILITY: [
    {
      positionName: '시설관리담당자',
      userType: 'ENGINEER',
      dutyName: '충전기 장애 및 점검 처리',
      label: '시설관리담당자 - ENGINEER',
    },
  ],
  SYSTEM: [
    {
      positionName: '기관장',
      userType: 'ADMIN',
      dutyName: '권한 및 시스템 총괄',
      label: '기관장 - ADMIN',
    },
  ],
};

const ALLOWED_DEPARTMENT_CODES = Object.keys(DEPARTMENT_ROLE_OPTIONS);

export const filterMisDepartments = (departments = []) => {
  return departments.filter((department) => ALLOWED_DEPARTMENT_CODES.includes(department.departmentCode));
};

export const getDepartmentCode = (departments = [], departmentId) => {
  const department = departments.find((item) => String(item.departmentId) === String(departmentId));
  return department?.departmentCode || '';
};

export const getRoleOptionsByDepartment = (departments = [], departmentId, creatableRoles = []) => {
  const departmentCode = getDepartmentCode(departments, departmentId);
  const creatableRoleValues = creatableRoles.map((role) => role.value);
  const options = DEPARTMENT_ROLE_OPTIONS[departmentCode] || [];

  if (creatableRoleValues.length === 0) {
    return [];
  }

  return options.filter((option) => creatableRoleValues.includes(option.userType));
};

export const findMappingByPosition = (departments = [], departmentId, positionName, creatableRoles = []) => {
  const options = getRoleOptionsByDepartment(departments, departmentId, creatableRoles);
  return options.find((option) => option.positionName === positionName) || options[0] || null;
};

export const findMappingByRole = (departments = [], departmentId, userType, creatableRoles = []) => {
  const options = getRoleOptionsByDepartment(departments, departmentId, creatableRoles);
  return options.find((option) => option.userType === userType) || options[0] || null;
};

export const getFirstAvailableDepartment = (departments = [], creatableRoles = []) => {
  return departments.find((department) => getRoleOptionsByDepartment(departments, department.departmentId, creatableRoles).length > 0) || departments[0] || null;
};
