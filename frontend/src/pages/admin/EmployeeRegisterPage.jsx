import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import * as adminApi from '../../apis/adminApi';
import { getCreatableRoles, getRole } from '../../utils/adminRoleUtils';

const initialForm = {
  userId: '',
  password: '',
  memberName: '',
  email: '',
  phone: '',
  employeeNo: '',
  departmentId: '',
  positionName: '운영담당자',
  dutyName: '민원 및 예약 운영',
  userType: 'OPERATOR',
  status: 'ACTIVE',
  hiredAt: '',
};

const positionOptions = ['운영담당자', '시설관리담당자', '운영관리자', '기관장'];

const EmployeeRegisterPage = () => {
  console.log('EmployeeRegisterPage 렌더링');

  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const currentRole = getRole(outletContext?.currentUser);
  const creatableRoles = useMemo(() => getCreatableRoles(currentRole), [currentRole]);

  const [form, setForm] = useState(initialForm);
  const [departmentList, setDepartmentList] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const loadDepartments = async () => {
      console.log('직원 등록 부서 목록 조회');

      try {
        const response = await adminApi.departments();
        console.log('직원 등록 부서 목록 응답', response.data);
        const nextDepartments = response.data || [];
        setDepartmentList(nextDepartments);
        setForm((prev) => ({
          ...prev,
          departmentId: prev.departmentId || String(nextDepartments[0]?.departmentId || ''),
          userType: creatableRoles[0]?.value || 'OPERATOR',
        }));
      } catch (error) {
        console.log('부서 목록 조회 실패', error);
        alert('부서 목록을 불러오지 못했습니다.');
      }
    };

    loadDepartments();
  }, [creatableRoles]);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log('직원 등록 입력 변경', name, value);

    setForm({
      ...form,
      [name]: value,
    });
  };

  const submitEmployee = async (e) => {
    e.preventDefault();
    console.log('직원 등록 제출', form);

    if (creatableRoles.length === 0) {
      alert('직원 등록 권한이 없습니다.');
      return;
    }

    if (!form.userId || !form.password || !form.memberName || !form.employeeNo || !form.departmentId) {
      alert('아이디, 초기 비밀번호, 이름, 사번, 부서는 필수입니다.');
      return;
    }

    try {
      setIsSubmitting(true);
      const response = await adminApi.registerEmployee({
        ...form,
        departmentId: Number(form.departmentId),
        email: form.email || `${form.userId}@ev-mis.go.kr`,
      });
      console.log('직원 등록 응답', response.data);
      alert('직원 계정이 등록되었습니다.');
      navigate('/admin/employees');
    } catch (error) {
      console.log('직원 등록 실패', error);
      alert(error.response?.data?.message || '직원 등록 중 오류가 발생했습니다.');
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
          <span>직원 계정과 직원 정보를 DB에 함께 생성합니다. MANAGER는 OPERATOR/ENGINEER만 등록할 수 있습니다.</span>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>직원 계정 생성</strong>
              <p>등록 시 app_member 계정과 employee 직원 정보가 동시에 저장됩니다.</p>
            </div>
          </div>

          <form className="admin-form-grid" onSubmit={submitEmployee}>
            <label>
              아이디 <b>*</b>
              <input type="text" name="userId" value={form.userId} onChange={changeValue} placeholder="예: operator01" />
            </label>

            <label>
              초기 비밀번호 <b>*</b>
              <input type="password" name="password" value={form.password} onChange={changeValue} placeholder="초기 비밀번호" />
            </label>

            <label>
              직원명 <b>*</b>
              <input type="text" name="memberName" value={form.memberName} onChange={changeValue} placeholder="예: 김운영" />
            </label>

            <label>
              사번 <b>*</b>
              <input type="text" name="employeeNo" value={form.employeeNo} onChange={changeValue} placeholder="예: EMP-2026-001" />
            </label>

            <label>
              이메일
              <input type="email" name="email" value={form.email} onChange={changeValue} placeholder="비우면 아이디@ev-mis.go.kr 자동 사용" />
            </label>

            <label>
              전화번호
              <input type="text" name="phone" value={form.phone} onChange={changeValue} placeholder="예: 010-1234-5678" />
            </label>

            <label>
              부서 <b>*</b>
              <select name="departmentId" value={form.departmentId} onChange={changeValue}>
                <option value="">부서 선택</option>
                {departmentList.map((department) => (
                  <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>
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

            <label>
              담당업무
              <input type="text" name="dutyName" value={form.dutyName} onChange={changeValue} placeholder="예: 민원 및 예약 운영" />
            </label>

            <label>
              입사일
              <input type="date" name="hiredAt" value={form.hiredAt} onChange={changeValue} />
            </label>

            <label className="full">
              권한
              <select name="userType" value={form.userType} onChange={changeValue} disabled={creatableRoles.length === 0}>
                {creatableRoles.map((role) => (
                  <option key={role.value} value={role.value}>{role.label}</option>
                ))}
              </select>
            </label>

            <div className="admin-action-row right full">
              <button type="button" className="gray" onClick={() => setForm(initialForm)}>
                초기화
              </button>
              <button type="submit" disabled={isSubmitting || creatableRoles.length === 0}>
                {isSubmitting ? '등록 중' : '직원 등록'}
              </button>
            </div>
          </form>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>권한별 등록 기준</strong>
              <p>본인보다 높은 권한을 생성하거나 수정할 수 없도록 제한합니다.</p>
            </div>
          </div>

          <div className="admin-guide-list">
            <p><b>ADMIN</b> 모든 직원과 권한을 등록/수정할 수 있습니다.</p>
            <p><b>MANAGER</b> OPERATOR, ENGINEER만 등록/수정할 수 있습니다.</p>
            <p><b>OPERATOR</b> 직원 등록과 권한 변경은 불가능합니다.</p>
            <p><b>ENGINEER</b> 직원 등록과 권한 변경은 불가능합니다.</p>
          </div>

          <div className="admin-role-grid compact">
            <article className="admin-role-card"><span>ADMIN</span><strong>최고관리자</strong><p>전체 권한, 시뮬레이션 초기화</p></article>
            <article className="admin-role-card"><span>MANAGER</span><strong>운영관리자</strong><p>운영 담당자/시설 담당자 등록</p></article>
            <article className="admin-role-card"><span>OPERATOR</span><strong>운영담당자</strong><p>민원, 예약 운영</p></article>
            <article className="admin-role-card"><span>ENGINEER</span><strong>시설관리</strong><p>장애, 점검 업무</p></article>
          </div>
        </article>
      </div>
    </section>
  );
};

export default EmployeeRegisterPage;
