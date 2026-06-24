import { useEffect, useMemo, useState } from 'react';
import { Link, useOutletContext } from 'react-router-dom';
import * as adminApi from '../../apis/adminApi';
import { getCreatableRoles, getRole, canCreateEmployeeRole } from '../../utils/adminRoleUtils';

const statusOptions = ['ACTIVE', 'INACTIVE', 'RETIRED'];
const positionOptions = ['운영담당자', '시설관리담당자', '운영관리자', '기관장'];

const roleLabelMap = {
  OPERATOR: '운영담당자',
  ENGINEER: '시설관리담당자',
  MANAGER: '운영관리자',
  ADMIN: '최고관리자',
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
      setDepartmentList(response.data || []);
    } catch (error) {
      console.log('부서 목록 조회 실패', error);
    }
  };

  useEffect(() => {
    loadDepartments();
    loadEmployees();
  }, []);

  const updateEmployeeField = async (employee, fieldName, value) => {
    console.log('직원 정보 변경', employee.employeeId, fieldName, value);

    if (!canCreateEmployeeRole(currentRole, fieldName === 'userType' ? value : employee.userType)) {
      alert('해당 직원 권한을 수정할 수 없습니다.');
      return;
    }

    const nextEmployee = {
      ...employee,
      [fieldName]: value,
    };

    if (fieldName === 'departmentId') {
      const department = departmentList.find((item) => String(item.departmentId) === String(value));
      nextEmployee.departmentName = department?.departmentName || employee.departmentName;
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

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>인사관리</p>
          <h1>직원 관리</h1>
          <span>직원 목록을 조회하고 부서, 직급, 권한, 재직 상태를 실제 DB 기준으로 변경합니다.</span>
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
                  <th>직급</th>
                  <th>권한</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {filteredEmployees.map((employee) => {
                  const editable = canCreateEmployeeRole(currentRole, employee.userType);

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
                          disabled={!editable}
                          onChange={(e) => updateEmployeeField(employee, 'positionName', e.target.value)}
                        >
                          {positionOptions.map((position) => <option key={position} value={position}>{position}</option>)}
                        </select>
                      </td>
                      <td>
                        <select
                          value={employee.userType || ''}
                          disabled={!editable}
                          onChange={(e) => updateEmployeeField(employee, 'userType', e.target.value)}
                        >
                          {creatableRoles.map((role) => <option key={role.value} value={role.value}>{role.value}</option>)}
                          {!creatableRoles.some((role) => role.value === employee.userType) && <option value={employee.userType}>{employee.userType}</option>}
                        </select>
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
          <div className="admin-panel-title"><strong>직원 상세</strong></div>

          {selected ? (
            <div className="admin-detail-box">
              <h3>{selected.memberName}</h3>
              <p>{selected.employeeNo} · {selected.userId}</p>

              <dl>
                <div><dt>부서</dt><dd>{selected.departmentName}</dd></div>
                <div><dt>직급</dt><dd>{selected.positionName}</dd></div>
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
    </section>
  );
};

export default EmployeePage;
