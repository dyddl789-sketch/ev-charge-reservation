/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useMemo, useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import * as adminApi from '../../apis/adminApi';
import { getCreatableRoles, getRole, canCreateEmployeeRole } from '../../utils/adminRoleUtils';
import {
  filterMisDepartments,
  findMappingByPosition,
  findMappingByRole,
  getRoleOptionsByDepartment,
} from '../../utils/adminEmployeeRoleMapping';

const statusOptions = ['ACTIVE', 'INACTIVE', 'RETIRED'];

const roleLabelMap = {
  OPERATOR: '운영담당자',
  ENGINEER: '시설관리담당자',
  MANAGER: '운영관리자',
  ADMIN: '기관장',
};

const getRoleBadgeClass = (role) => {
  if (role === 'ADMIN') return 'purple';
  if (role === 'MANAGER') return 'blue';
  if (role === 'ENGINEER') return 'warning';
  return 'green';
};

const EmployeePage = () => {
  console.log('EmployeePage 렌더링');

  const outletContext = useOutletContext();
  const currentRole = getRole(outletContext?.currentUser);
  const creatableRoles = useMemo(() => getCreatableRoles(currentRole), [currentRole]);

  const [employees, setEmployees] = useState([]);
  const [departmentList, setDepartmentList] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('전체');
  const [selected, setSelected] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [passwordModalEmployee, setPasswordModalEmployee] = useState(null);
  const [passwordForm, setPasswordForm] = useState({ newPassword: '', newPasswordConfirm: '' });
  const [isResettingPassword, setIsResettingPassword] = useState(false);

  const filteredEmployees = useMemo(() => {
    console.log('직원 목록 필터링', keyword, departmentFilter);

    return employees.filter((employee) => {
      const safeKeyword = keyword.trim();
      const keywordMatched =
        !safeKeyword ||
        employee.memberName?.includes(safeKeyword) ||
        employee.userId?.includes(safeKeyword) ||
        employee.employeeNo?.includes(safeKeyword) ||
        employee.email?.includes(safeKeyword);
      const departmentMatched =
        departmentFilter === '전체' || String(employee.departmentId) === String(departmentFilter);

      return keywordMatched && departmentMatched;
    });
  }, [employees, keyword, departmentFilter]);

  const loadEmployees = async () => {
    console.log('직원 목록 API 조회');

    try {
      setIsLoading(true);
      const response = await adminApi.employees();
      console.log('직원 목록 API 응답', response.data);
      const nextEmployees = response.data || [];
      setEmployees(nextEmployees);
      setSelected(nextEmployees[0] || null);
    } catch (error) {
      console.log('직원 목록 조회 실패', error);
      alert(error.response?.data?.message || '직원 목록을 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const loadDepartments = async () => {
    console.log('부서 목록 API 조회');

    try {
      const response = await adminApi.departments();
      console.log('부서 목록 API 응답', response.data);
      setDepartmentList(filterMisDepartments(response.data || []));
    } catch (error) {
      console.log('부서 목록 조회 실패', error);
    }
  };

  useEffect(() => {
    loadDepartments();
    loadEmployees();
  }, []);

  const getPositionOptions = (employee) => {
    const options = getRoleOptionsByDepartment(departmentList, employee.departmentId, creatableRoles);

    if (options.length > 0) {
      return options;
    }

    const currentOption = findMappingByRole(departmentList, employee.departmentId, employee.userType, [
      { value: employee.userType },
    ]);

    return currentOption ? [currentOption] : [];
  };

  const updateEmployeeField = async (employee, fieldName, value) => {
    console.log('직원 정보 변경', employee.employeeId, fieldName, value);

    if (!canCreateEmployeeRole(currentRole, employee.userType)) {
      alert('해당 직원 권한을 수정할 수 없습니다.');
      return;
    }

    let nextEmployee = {
      ...employee,
      [fieldName]: value,
    };

    if (fieldName === 'departmentId') {
      const department = departmentList.find((item) => String(item.departmentId) === String(value));
      const roleOptions = getRoleOptionsByDepartment(departmentList, value, creatableRoles);
      const nextRoleOption = roleOptions[0];

      if (!nextRoleOption) {
        alert('선택한 부서에서 부여 가능한 직책/권한이 없습니다.');
        return;
      }

      nextEmployee = {
        ...nextEmployee,
        departmentId: Number(value),
        departmentName: department?.departmentName || employee.departmentName,
        departmentCode: department?.departmentCode || employee.departmentCode,
        positionName: nextRoleOption.positionName,
        dutyName: nextRoleOption.dutyName,
        userType: nextRoleOption.userType,
      };
    }

    if (fieldName === 'positionName') {
      const nextRoleOption = findMappingByPosition(departmentList, employee.departmentId, value, creatableRoles);

      if (!nextRoleOption) {
        alert('부서와 직책/업무역할 조합을 확인해 주세요.');
        return;
      }

      nextEmployee = {
        ...nextEmployee,
        positionName: nextRoleOption.positionName,
        dutyName: nextRoleOption.dutyName,
        userType: nextRoleOption.userType,
      };
    }

    if (!canCreateEmployeeRole(currentRole, nextEmployee.userType)) {
      alert('해당 권한으로 직원을 수정할 수 없습니다.');
      return;
    }

    try {
      const response = await adminApi.updateEmployee(employee.employeeId, nextEmployee);
      console.log('직원 정보 변경 응답', response.data);
      const saved = response.data || nextEmployee;

      setEmployees((prev) => prev.map((item) => (item.employeeId === employee.employeeId ? saved : item)));
      setSelected((prev) => (prev?.employeeId === employee.employeeId ? saved : prev));
    } catch (error) {
      console.log('직원 정보 변경 실패', error);
      alert(error.response?.data?.message || '직원 정보 변경 중 오류가 발생했습니다.');
    }
  };

  const selectEmployee = (employee) => {
    console.log('직원 선택', employee);
    setSelected(employee);
  };

  const openPasswordResetModal = (employee) => {
    console.log('직원 비밀번호 초기화 모달 열기', employee);

    if (!canCreateEmployeeRole(currentRole, employee.userType)) {
      alert('해당 직원의 비밀번호를 초기화할 권한이 없습니다.');
      return;
    }

    setPasswordModalEmployee(employee);
    setPasswordForm({ newPassword: '', newPasswordConfirm: '' });
  };

  const changePasswordForm = (e) => {
    const { name, value } = e.target;
    console.log('직원 비밀번호 초기화 입력 변경', name);

    setPasswordForm({
      ...passwordForm,
      [name]: value,
    });
  };

  const submitPasswordReset = async (e) => {
    e.preventDefault();
    console.log('직원 비밀번호 초기화 제출', passwordModalEmployee);

    if (!passwordModalEmployee) {
      return;
    }

    if (!passwordForm.newPassword) {
      alert('새 임시 비밀번호를 입력해 주세요.');
      return;
    }

    if (passwordForm.newPassword !== passwordForm.newPasswordConfirm) {
      alert('새 임시 비밀번호와 확인값이 일치하지 않습니다.');
      return;
    }

    const confirmed = window.confirm(`${passwordModalEmployee.memberName} 직원의 비밀번호를 초기화하시겠습니까?`);
    if (!confirmed) {
      return;
    }

    try {
      setIsResettingPassword(true);
      await adminApi.resetEmployeePassword(passwordModalEmployee.employeeId, passwordForm);
      alert('직원 비밀번호가 초기화되었습니다. 임시 비밀번호를 직원에게 안내해 주세요.');
      setPasswordModalEmployee(null);
      setPasswordForm({ newPassword: '', newPasswordConfirm: '' });
    } catch (error) {
      console.log('직원 비밀번호 초기화 실패', error);
      alert(error.response?.data?.message || '비밀번호 초기화 중 오류가 발생했습니다.');
    } finally {
      setIsResettingPassword(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>인사관리</p>
          <h1>직원 관리</h1>
          <span>부서와 직책/업무역할에 따라 시스템 권한이 자동으로 연결됩니다.</span>
        </div>
        <div className="admin-action-row">
          <button type="button" className="gray" onClick={loadEmployees} disabled={isLoading}>{isLoading ? '조회 중' : '새로고침'}</button>
          <Link className="admin-link-button" to="/admin/employees/register">직원 등록</Link>
        </div>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card"><span>전체 직원</span><strong>{employees.length}명</strong><p>운영기관 등록 직원</p></article>
        <article className="admin-kpi-card"><span>운영담당자</span><strong>{employees.filter((item) => item.userType === 'OPERATOR').length}명</strong><p>민원/예약 처리</p></article>
        <article className="admin-kpi-card"><span>시설관리</span><strong>{employees.filter((item) => item.userType === 'ENGINEER').length}명</strong><p>장애/점검 처리</p></article>
        <article className="admin-kpi-card"><span>관리자</span><strong>{employees.filter((item) => item.userType === 'ADMIN' || item.userType === 'MANAGER').length}명</strong><p>배정/결재 권한</p></article>
      </div>

      <div className="admin-filter-panel">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="직원명, 아이디, 사번, 이메일 검색"
        />
        <select value={departmentFilter} onChange={(e) => setDepartmentFilter(e.target.value)}>
          <option value="전체">전체 부서</option>
          {departmentList.map((department) => (
            <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>
          ))}
        </select>
        <button type="button" onClick={loadEmployees}>검색</button>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>직원 목록</strong>
              <p>ADMIN은 전체 권한 변경 가능, MANAGER는 OPERATOR/ENGINEER만 관리할 수 있습니다.</p>
            </div>
          </div>

          <div className="admin-table-wrap">
            <table className="admin-table employee-table">
              <thead>
                <tr>
                  <th>사번</th>
                  <th>직원명</th>
                  <th>부서</th>
                  <th>직책/업무역할</th>
                  <th>시스템 권한</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {filteredEmployees.map((employee) => {
                  const editable = canCreateEmployeeRole(currentRole, employee.userType);
                  const positionOptions = getPositionOptions(employee);

                  return (
                    <tr key={employee.employeeId} className={!editable ? 'locked-row' : ''}>
                      <td>{employee.employeeNo}</td>
                      <td>
                        <button type="button" className="admin-text-button" onClick={() => selectEmployee(employee)}>
                          {employee.memberName}
                        </button>
                        <span className="admin-sub-text">{employee.userId}</span>
                      </td>
                      <td>
                        <select
                          value={employee.departmentId || ''}
                          disabled={!editable}
                          onChange={(e) => updateEmployeeField(employee, 'departmentId', Number(e.target.value))}
                        >
                          {departmentList.map((department) => (
                            <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <select
                          value={employee.positionName || ''}
                          disabled={!editable || positionOptions.length === 0}
                          onChange={(e) => updateEmployeeField(employee, 'positionName', e.target.value)}
                        >
                          {positionOptions.map((option) => <option key={option.userType} value={option.positionName}>{option.positionName}</option>)}
                          {!positionOptions.some((option) => option.positionName === employee.positionName) && (
                            <option value={employee.positionName}>{employee.positionName}</option>
                          )}
                        </select>
                      </td>
                      <td>
                        <em className={`admin-badge ${getRoleBadgeClass(employee.userType)}`}>
                          {employee.userType}
                        </em>
                      </td>
                      <td>
                        <select
                          value={employee.status || 'ACTIVE'}
                          disabled={!editable}
                          onChange={(e) => updateEmployeeField(employee, 'status', e.target.value)}
                        >
                          {statusOptions.map((status) => <option key={status} value={status}>{status}</option>)}
                        </select>
                      </td>
                      <td><button type="button" onClick={() => selectEmployee(employee)}>상세</button></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>직원 상세</strong>
            {selected && canCreateEmployeeRole(currentRole, selected.userType) && (
              <button type="button" className="warning" onClick={() => openPasswordResetModal(selected)}>
                비밀번호 초기화
              </button>
            )}
          </div>

          {selected ? (
            <div className="admin-detail-box">
              <h3>{selected.memberName}</h3>
              <p>{selected.employeeNo} · {selected.userId}</p>

              <dl>
                <div><dt>부서</dt><dd>{selected.departmentName}</dd></div>
                <div><dt>직책/업무역할</dt><dd>{selected.positionName}</dd></div>
                <div><dt>담당업무</dt><dd>{selected.dutyName || '-'}</dd></div>
                <div><dt>이메일</dt><dd>{selected.email || '-'}</dd></div>
                <div><dt>전화번호</dt><dd>{selected.phone || '-'}</dd></div>
                <div><dt>권한</dt><dd><em className={`admin-badge ${getRoleBadgeClass(selected.userType)}`}>{selected.userType}</em></dd></div>
                <div><dt>권한 설명</dt><dd>{roleLabelMap[selected.userType]}</dd></div>
                <div><dt>상태</dt><dd>{selected.status}</dd></div>
              </dl>

              <div className="admin-guide-list mt">
                <p><b>OPERATOR</b> 민원 접수 확인, 예약/결제 문의 처리</p>
                <p><b>ENGINEER</b> 장애 확인, 점검 수행, 조치 결과 등록</p>
                <p><b>MANAGER</b> 담당자 배정, 전자결재 1차 승인</p>
                <p><b>ADMIN</b> 직원 등록, 권한 관리, 최종 승인</p>
              </div>
            </div>
          ) : (
            <div className="empty-box">직원을 선택해 주세요.</div>
          )}
        </article>
      </div>

      {passwordModalEmployee && (
        <div className="admin-modal-backdrop">
          <div className="admin-modal employee-password-modal">
            <div className="admin-modal-head">
              <div>
                <p>인사관리</p>
                <h2>직원 비밀번호 초기화</h2>
              </div>
              <button type="button" onClick={() => setPasswordModalEmployee(null)}>닫기</button>
            </div>

            <div className="admin-detail-box compact">
              <dl>
                <div><dt>직원명</dt><dd>{passwordModalEmployee.memberName}</dd></div>
                <div><dt>아이디</dt><dd>{passwordModalEmployee.userId}</dd></div>
                <div><dt>권한</dt><dd>{passwordModalEmployee.userType}</dd></div>
                <div><dt>사번</dt><dd>{passwordModalEmployee.employeeNo}</dd></div>
              </dl>
            </div>

            <form className="admin-form-grid single" onSubmit={submitPasswordReset}>
              <label>
                새 임시 비밀번호
                <input
                  type="password"
                  name="newPassword"
                  value={passwordForm.newPassword}
                  onChange={changePasswordForm}
                  placeholder="직원에게 안내할 임시 비밀번호"
                />
              </label>

              <label>
                새 임시 비밀번호 확인
                <input
                  type="password"
                  name="newPasswordConfirm"
                  value={passwordForm.newPasswordConfirm}
                  onChange={changePasswordForm}
                  placeholder="비밀번호 확인"
                />
              </label>

              <p className="admin-help-text">
                관리자는 직원의 기존 비밀번호를 조회하지 않고 새 임시 비밀번호로 초기화만 할 수 있습니다.
              </p>

              <div className="admin-action-row right">
                <button type="button" className="gray" onClick={() => setPasswordModalEmployee(null)}>취소</button>
                <button type="submit" className="warning" disabled={isResettingPassword}>
                  {isResettingPassword ? '초기화 중' : '비밀번호 초기화'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
};

export default EmployeePage;
