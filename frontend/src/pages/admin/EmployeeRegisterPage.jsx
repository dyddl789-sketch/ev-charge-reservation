import { useState } from "react";
import * as adminApi from "../../apis/adminApi";

const initialForm = {
  userId: "",
  password: "",
  memberName: "",
  email: "",
  phone: "",
  employeeNo: "",
  departmentName: "운영팀",
  positionName: "운영담당자",
  userType: "OPERATOR",
};

const departmentOptions = ["운영팀", "시설관리팀", "운영관리팀", "기관장실"];

const positionOptions = [
  "운영담당자",
  "시설관리담당자",
  "운영관리자",
  "기관장",
];

const roleOptions = [
  { value: "OPERATOR", label: "OPERATOR - 운영담당자" },
  { value: "ENGINEER", label: "ENGINEER - 시설관리담당자" },
  { value: "MANAGER", label: "MANAGER - 운영관리자" },
  { value: "ADMIN", label: "ADMIN - 최고관리자" },
];

const EmployeeRegisterPage = () => {
  console.log("EmployeeRegisterPage 렌더링");

  const [form, setForm] = useState(initialForm);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("직원 등록 입력 변경", name, value);

    setForm({
      ...form,
      [name]: value,
    });
  };

  const submitEmployee = async (e) => {
    e.preventDefault();
    console.log("직원 등록 제출", form);

    if (!form.userId || !form.password || !form.memberName || !form.employeeNo) {
      alert("아이디, 초기 비밀번호, 이름, 사번은 필수입니다.");
      return;
    }

    try {
      setIsSubmitting(true);
      await adminApi.registerEmployee(form);
      alert("직원 계정 등록 요청이 완료되었습니다.");
      setForm(initialForm);
    } catch (error) {
      console.log("직원 등록 API 실패 - Mock 화면 유지", error);
      alert("현재 백엔드 직원 등록 API가 없으면 화면만 확인됩니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>인사관리</p>
          <h1>직원 등록</h1>
          <span>
            최고관리자가 운영기관 직원 계정을 생성하고 부서, 직급, 권한을 함께 부여합니다.
          </span>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>직원 계정 생성</strong>
              <p>등록 시 app_member 계정과 employee 직원 정보가 함께 생성되는 구조입니다.</p>
            </div>
          </div>

          <form className="admin-form-grid" onSubmit={submitEmployee}>
            <label>
              아이디 <b>*</b>
              <input
                type="text"
                name="userId"
                value={form.userId}
                onChange={changeValue}
                placeholder="예: operator01"
              />
            </label>

            <label>
              초기 비밀번호 <b>*</b>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={changeValue}
                placeholder="초기 비밀번호"
              />
            </label>

            <label>
              직원명 <b>*</b>
              <input
                type="text"
                name="memberName"
                value={form.memberName}
                onChange={changeValue}
                placeholder="예: 김운영"
              />
            </label>

            <label>
              사번 <b>*</b>
              <input
                type="text"
                name="employeeNo"
                value={form.employeeNo}
                onChange={changeValue}
                placeholder="예: EMP-2026-001"
              />
            </label>

            <label>
              이메일
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={changeValue}
                placeholder="예: operator01@ev-mis.go.kr"
              />
            </label>

            <label>
              전화번호
              <input
                type="text"
                name="phone"
                value={form.phone}
                onChange={changeValue}
                placeholder="예: 010-1234-5678"
              />
            </label>

            <label>
              부서
              <select name="departmentName" value={form.departmentName} onChange={changeValue}>
                {departmentOptions.map((department) => (
                  <option key={department} value={department}>{department}</option>
                ))}
              </select>
            </label>

            <label>
              직급/역할명
              <select name="positionName" value={form.positionName} onChange={changeValue}>
                {positionOptions.map((position) => (
                  <option key={position} value={position}>{position}</option>
                ))}
              </select>
            </label>

            <label className="full">
              권한
              <select name="userType" value={form.userType} onChange={changeValue}>
                {roleOptions.map((role) => (
                  <option key={role.value} value={role.value}>{role.label}</option>
                ))}
              </select>
            </label>

            <div className="admin-action-row right full">
              <button
                type="button"
                className="gray"
                onClick={() => {
                  console.log("직원 등록 폼 초기화");
                  setForm(initialForm);
                }}
              >
                초기화
              </button>
              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "등록 중" : "직원 등록"}
              </button>
            </div>
          </form>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>등록 처리 흐름</strong>
              <p>일반 회원가입과 분리하여 운영기관 직원만 인사관리에서 생성합니다.</p>
            </div>
          </div>

          <div className="admin-guide-list">
            <p><b>1.</b> 최고관리자 또는 운영관리자가 직원 등록 화면에 접근합니다.</p>
            <p><b>2.</b> 로그인 계정 정보와 직원 사번, 부서, 직급을 입력합니다.</p>
            <p><b>3.</b> 업무 역할에 맞게 OPERATOR, ENGINEER, MANAGER, ADMIN 권한을 부여합니다.</p>
            <p><b>4.</b> 저장 시 회원 계정과 직원 정보가 생성되어 MIS 담당자로 배정할 수 있습니다.</p>
          </div>

          <div className="admin-role-grid compact">
            <article className="admin-role-card">
              <span>OPERATOR</span>
              <strong>운영담당자</strong>
              <p>민원 확인, 예약/결제 문의 처리</p>
            </article>
            <article className="admin-role-card">
              <span>ENGINEER</span>
              <strong>시설관리</strong>
              <p>장애 확인, 점검 수행, 조치 등록</p>
            </article>
            <article className="admin-role-card">
              <span>MANAGER</span>
              <strong>운영관리자</strong>
              <p>담당자 배정, 1차 결재 승인</p>
            </article>
            <article className="admin-role-card">
              <span>ADMIN</span>
              <strong>최고관리자</strong>
              <p>직원 등록, 권한 관리, 최종 승인</p>
            </article>
          </div>
        </article>
      </div>
    </section>
  );
};

export default EmployeeRegisterPage;
