import { useMemo, useState } from "react";
import * as adminApi from "../../apis/adminApi";

const initialEmployees = [
  {
    employeeId: 1,
    employeeNo: "EMP-2026-001",
    memberName: "김운영",
    userId: "operator01",
    departmentName: "운영팀",
    positionName: "운영담당자",
    userType: "OPERATOR",
    status: "ACTIVE",
  },
  {
    employeeId: 2,
    employeeNo: "EMP-2026-002",
    memberName: "박시설",
    userId: "engineer01",
    departmentName: "시설관리팀",
    positionName: "시설관리담당자",
    userType: "ENGINEER",
    status: "ACTIVE",
  },
  {
    employeeId: 3,
    employeeNo: "EMP-2026-003",
    memberName: "이관리",
    userId: "manager01",
    departmentName: "운영관리팀",
    positionName: "운영관리자",
    userType: "MANAGER",
    status: "ACTIVE",
  },
  {
    employeeId: 4,
    employeeNo: "EMP-2026-004",
    memberName: "최기관",
    userId: "admin01",
    departmentName: "기관장실",
    positionName: "기관장",
    userType: "ADMIN",
    status: "ACTIVE",
  },
];

const departmentOptions = ["운영팀", "시설관리팀", "운영관리팀", "기관장실"];
const positionOptions = ["운영담당자", "시설관리담당자", "운영관리자", "기관장"];
const roleOptions = ["OPERATOR", "ENGINEER", "MANAGER", "ADMIN"];
const statusOptions = ["ACTIVE", "INACTIVE"];

const roleLabelMap = {
  OPERATOR: "운영담당자",
  ENGINEER: "시설관리담당자",
  MANAGER: "운영관리자",
  ADMIN: "최고관리자",
};

const getRoleBadgeClass = (role) => {
  if (role === "ADMIN") return "purple";
  if (role === "MANAGER") return "blue";
  if (role === "ENGINEER") return "warning";
  return "green";
};

const EmployeePage = () => {
  console.log("EmployeePage 렌더링");

  const [employees, setEmployees] = useState(initialEmployees);
  const [keyword, setKeyword] = useState("");
  const [departmentFilter, setDepartmentFilter] = useState("전체");
  const [selected, setSelected] = useState(initialEmployees[0]);

  const filteredEmployees = useMemo(() => {
    console.log("직원 목록 필터링", keyword, departmentFilter);

    return employees.filter((employee) => {
      const keywordMatched =
        employee.memberName.includes(keyword) ||
        employee.userId.includes(keyword) ||
        employee.employeeNo.includes(keyword);
      const departmentMatched =
        departmentFilter === "전체" || employee.departmentName === departmentFilter;

      return keywordMatched && departmentMatched;
    });
  }, [employees, keyword, departmentFilter]);

  const updateEmployeeField = async (employeeId, fieldName, value) => {
    console.log("직원 정보 변경", employeeId, fieldName, value);

    setEmployees((prev) =>
      prev.map((employee) =>
        employee.employeeId === employeeId
          ? { ...employee, [fieldName]: value }
          : employee
      )
    );

    setSelected((prev) =>
      prev.employeeId === employeeId ? { ...prev, [fieldName]: value } : prev
    );

    try {
      await adminApi.updateEmployee(employeeId, {
        [fieldName]: value,
      });
    } catch (error) {
      console.log("직원 정보 변경 API 실패 - Mock 상태 유지", error);
    }
  };

  const selectEmployee = (employee) => {
    console.log("직원 선택", employee);
    setSelected(employee);
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>인사관리</p>
          <h1>직원 관리</h1>
          <span>
            직원 목록을 확인하고 부서, 직급, 권한, 재직 상태를 한 화면에서 변경합니다.
          </span>
        </div>
      </div>

      <div className="admin-kpi-grid four">
        <article className="admin-kpi-card">
          <span>전체 직원</span>
          <strong>{employees.length}명</strong>
          <p>운영기관 등록 직원</p>
        </article>
        <article className="admin-kpi-card">
          <span>운영담당자</span>
          <strong>{employees.filter((item) => item.userType === "OPERATOR").length}명</strong>
          <p>민원/예약 처리</p>
        </article>
        <article className="admin-kpi-card">
          <span>시설관리</span>
          <strong>{employees.filter((item) => item.userType === "ENGINEER").length}명</strong>
          <p>장애/점검 처리</p>
        </article>
        <article className="admin-kpi-card">
          <span>관리자</span>
          <strong>{employees.filter((item) => item.userType === "ADMIN" || item.userType === "MANAGER").length}명</strong>
          <p>배정/결재 권한</p>
        </article>
      </div>

      <div className="admin-filter-panel">
        <input
          type="text"
          value={keyword}
          onChange={(e) => {
            console.log("직원 검색어 입력", e.target.value);
            setKeyword(e.target.value);
          }}
          placeholder="직원명, 아이디, 사번 검색"
        />
        <select
          value={departmentFilter}
          onChange={(e) => {
            console.log("직원 부서 필터 변경", e.target.value);
            setDepartmentFilter(e.target.value);
          }}
        >
          <option value="전체">전체 부서</option>
          {departmentOptions.map((department) => (
            <option key={department} value={department}>{department}</option>
          ))}
        </select>
        <button type="button" onClick={() => console.log("직원 검색 클릭", { keyword, departmentFilter })}>
          검색
        </button>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>직원 목록</strong>
              <p>권한 변경은 업무 처리 가능 범위를 바꾸므로 관리자 이상만 수행하는 것을 기준으로 설계합니다.</p>
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
                {filteredEmployees.map((employee) => (
                  <tr key={employee.employeeId}>
                    <td>{employee.employeeNo}</td>
                    <td>
                      <button
                        type="button"
                        className="admin-text-button"
                        onClick={() => selectEmployee(employee)}
                      >
                        {employee.memberName}
                      </button>
                      <span className="admin-sub-text">{employee.userId}</span>
                    </td>
                    <td>
                      <select
                        value={employee.departmentName}
                        onChange={(e) => updateEmployeeField(employee.employeeId, "departmentName", e.target.value)}
                      >
                        {departmentOptions.map((department) => (
                          <option key={department} value={department}>{department}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={employee.positionName}
                        onChange={(e) => updateEmployeeField(employee.employeeId, "positionName", e.target.value)}
                      >
                        {positionOptions.map((position) => (
                          <option key={position} value={position}>{position}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={employee.userType}
                        onChange={(e) => updateEmployeeField(employee.employeeId, "userType", e.target.value)}
                      >
                        {roleOptions.map((role) => (
                          <option key={role} value={role}>{role}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <select
                        value={employee.status}
                        onChange={(e) => updateEmployeeField(employee.employeeId, "status", e.target.value)}
                      >
                        {statusOptions.map((status) => (
                          <option key={status} value={status}>{status}</option>
                        ))}
                      </select>
                    </td>
                    <td>
                      <button type="button" onClick={() => selectEmployee(employee)}>상세</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <strong>직원 상세</strong>
          </div>

          {selected && (
            <div className="admin-detail-box">
              <h3>{selected.memberName}</h3>
              <p>{selected.employeeNo} · {selected.userId}</p>

              <dl>
                <div><dt>부서</dt><dd>{selected.departmentName}</dd></div>
                <div><dt>직급</dt><dd>{selected.positionName}</dd></div>
                <div>
                  <dt>권한</dt>
                  <dd>
                    <em className={`admin-badge ${getRoleBadgeClass(selected.userType)}`}>
                      {selected.userType}
                    </em>
                  </dd>
                </div>
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
          )}
        </article>
      </div>
    </section>
  );
};

export default EmployeePage;
