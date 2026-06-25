import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useOutletContext } from 'react-router-dom';
import * as adminApi from '../../apis/adminApi';
import { getCreatableRoles, getRole } from '../../utils/adminRoleUtils';
import {
  filterMisDepartments,
  findMappingByPosition,
  getFirstAvailableDepartment,
  getRoleOptionsByDepartment,
} from '../../utils/adminEmployeeRoleMapping';

const initialForm = {
  userId: '',
  password: '',
  memberName: '',
  email: '',
  phone: '',
  employeeNo: '',
  departmentId: '',
  positionName: '',
  dutyName: '',
  userType: '',
  status: 'ACTIVE',
  hiredAt: '',
};

const EmployeeRegisterPage = () => {
  console.log('EmployeeRegisterPage 렌더링');

  const navigate = useNavigate();
  const outletContext = useOutletContext();
  const currentRole = getRole(outletContext?.currentUser);
  const creatableRoles = useMemo(() => getCreatableRoles(currentRole), [currentRole]);

  const [form, setForm] = useState(initialForm);
  const [departmentList, setDepartmentList] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const roleOptions = useMemo(() => {
    return getRoleOptionsByDepartment(departmentList, form.departmentId, creatableRoles);
  }, [departmentList, form.departmentId, creatableRoles]);

  const selectedRoleOption = useMemo(() => {
    return findMappingByPosition(departmentList, form.departmentId, form.positionName, creatableRoles);
  }, [departmentList, form.departmentId, form.positionName, creatableRoles]);

  useEffect(() => {
    const loadDepartments = async () => {
      console.log('직원 등록 부서 목록 조회');

      try {
        const response = await adminApi.departments();
        console.log('직원 등록 부서 목록 응답', response.data);
        const nextDepartments = filterMisDepartments(response.data || []);
        const firstDepartment = getFirstAvailableDepartment(nextDepartments, creatableRoles);
        const firstRoleOption = firstDepartment
          ? getRoleOptionsByDepartment(nextDepartments, firstDepartment.departmentId, creatableRoles)[0]
          : null;

        setDepartmentList(nextDepartments);
        setForm((prev) => ({
          ...prev,
          departmentId: prev.departmentId || String(firstDepartment?.departmentId || ''),
          positionName: prev.positionName || firstRoleOption?.positionName || '',
          dutyName: prev.dutyName || firstRoleOption?.dutyName || '',
          userType: prev.userType || firstRoleOption?.userType || '',
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

    if (name === 'departmentId') {
      const firstRoleOption = getRoleOptionsByDepartment(departmentList, value, creatableRoles)[0];

      setForm({
        ...form,
        departmentId: value,
        positionName: firstRoleOption?.positionName || '',
        dutyName: firstRoleOption?.dutyName || '',
        userType: firstRoleOption?.userType || '',
      });
      return;
    }

    if (name === 'positionName') {
      const nextRoleOption = findMappingByPosition(departmentList, form.departmentId, value, creatableRoles);

      setForm({
        ...form,
        positionName: value,
        dutyName: nextRoleOption?.dutyName || form.dutyName,
        userType: nextRoleOption?.userType || form.userType,
      });
      return;
    }

    setForm({
      ...form,
      [name]: value,
    });
  };

  const submitEmployee = async (e) => {
    e.preventDefault();
    console.log('직원 등록 제출', form, selectedRoleOption);

    if (creatableRoles.length === 0) {
      alert('직원 등록 권한이 없습니다.');
      return;
    }

    if (!form.userId || !form.password || !form.memberName || !form.employeeNo || !form.departmentId || !form.positionName) {
      alert('아이디, 초기 비밀번호, 이름, 사번, 부서, 직책/업무역할은 필수입니다.');
      return;
    }

    if (!selectedRoleOption) {
      alert('부서와 직책/업무역할 조합을 확인해 주세요.');
      return;
    }

    try {
      setIsSubmitting(true);
      const response = await adminApi.registerEmployee({
        ...form,
        departmentId: Number(form.departmentId),
        positionName: selectedRoleOption.positionName,
        dutyName: form.dutyName || selectedRoleOption.dutyName,
        userType: selectedRoleOption.userType,
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
          <span>부서를 선택하면 직책/업무역할과 시스템 권한이 자동으로 연결됩니다.</span>
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
              직책/업무역할 <b>*</b>
              <select name="positionName" value={form.positionName} onChange={changeValue} disabled={roleOptions.length === 0}>
                {roleOptions.map((option) => (
                  <option key={option.userType} value={option.positionName}>{option.positionName}</option>
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
              시스템 권한
              <input type="text" value={selectedRoleOption ? `${selectedRoleOption.userType} - ${selectedRoleOption.positionName}` : ''} readOnly />
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
              <strong>부서·직책·권한 매핑</strong>
              <p>직원이 담당하는 업무에 따라 시스템 권한을 자동 부여합니다.</p>
            </div>
          </div>

          <div className="admin-guide-list">
            <p><b>운영관리팀</b> 운영담당자(OPERATOR), 운영관리자(MANAGER)</p>
            <p><b>시설관리팀</b> 시설관리담당자(ENGINEER)</p>
            <p><b>시스템관리팀</b> 기관장(ADMIN)</p>
            <p>경영관리팀은 현재 프로젝트 업무 흐름에서 사용하지 않아 선택 목록에서 제외했습니다.</p>
          </div>

          <div className="admin-role-grid compact">
            <article className="admin-role-card"><span>ADMIN</span><strong>기관장</strong><p>최종 결재, 전체 권한 관리</p></article>
            <article className="admin-role-card"><span>MANAGER</span><strong>운영관리자</strong><p>담당자 배정, 1차 결재</p></article>
            <article className="admin-role-card"><span>OPERATOR</span><strong>운영담당자</strong><p>민원, 예약 운영</p></article>
            <article className="admin-role-card"><span>ENGINEER</span><strong>시설관리</strong><p>장애, 점검 업무</p></article>
          </div>
        </article>
      </div>
    </section>
  );
};

export default EmployeeRegisterPage;
