/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useState } from 'react';
import * as adminApi from '../../apis/adminApi';

const initialForm = {
  email: '',
  phone: '',
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: '',
};

const roleLabelMap = {
  ADMIN: '기관장/최고관리자',
  MANAGER: '운영관리자',
  OPERATOR: '운영담당자',
  ENGINEER: '시설관리담당자',
};

const AdminProfilePage = () => {
  console.log('AdminProfilePage 렌더링');

  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(initialForm);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const loadProfile = async () => {
    console.log('MIS 내 정보 조회');

    try {
      setIsLoading(true);
      const response = await adminApi.adminProfile();
      console.log('MIS 내 정보 응답', response.data);
      const nextProfile = response.data || null;
      setProfile(nextProfile);
      setForm({
        ...initialForm,
        email: nextProfile?.email || '',
        phone: nextProfile?.phone || '',
      });
    } catch (error) {
      console.log('MIS 내 정보 조회 실패', error);
      alert(error.response?.data?.message || '내 정보를 불러오지 못했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log('MIS 내 정보 입력 변경', name, value);

    setForm({
      ...form,
      [name]: value,
    });
  };

  const submitProfile = async (e) => {
    e.preventDefault();
    console.log('MIS 내 정보 저장 요청', form);

    if (form.newPassword || form.newPasswordConfirm || form.currentPassword) {
      if (!form.currentPassword) {
        alert('비밀번호를 변경하려면 현재 비밀번호를 입력해 주세요.');
        return;
      }

      if (!form.newPassword) {
        alert('새 비밀번호를 입력해 주세요.');
        return;
      }

      if (form.newPassword !== form.newPasswordConfirm) {
        alert('새 비밀번호와 확인값이 일치하지 않습니다.');
        return;
      }
    }

    try {
      setIsSaving(true);
      const response = await adminApi.updateAdminProfile(form);
      console.log('MIS 내 정보 저장 응답', response.data);
      setProfile(response.data);
      setForm({
        ...initialForm,
        email: response.data?.email || '',
        phone: response.data?.phone || '',
      });
      alert('내 정보가 수정되었습니다.');
    } catch (error) {
      console.log('MIS 내 정보 저장 실패', error);
      alert(error.response?.data?.message || '내 정보 수정 중 오류가 발생했습니다.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>계정 설정</p>
          <h1>MIS 내 정보 관리</h1>
          <span>직원 계정은 일반 마이페이지가 아니라 MIS 내부에서 연락처와 비밀번호를 관리합니다.</span>
        </div>
        <div className="admin-action-row">
          <button type="button" className="gray" onClick={loadProfile} disabled={isLoading}>
            {isLoading ? '조회 중' : '새로고침'}
          </button>
        </div>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>내 계정 정보</strong>
              <p>이메일, 전화번호, 비밀번호를 수정할 수 있습니다.</p>
            </div>
          </div>

          <form className="admin-form-grid" onSubmit={submitProfile}>
            <label>
              아이디
              <input value={profile?.userId || ''} readOnly />
            </label>

            <label>
              직원명
              <input value={profile?.memberName || ''} readOnly />
            </label>

            <label>
              사번
              <input value={profile?.employeeNo || ''} readOnly />
            </label>

            <label>
              권한
              <input value={profile?.userType || ''} readOnly />
            </label>

            <label>
              부서
              <input value={profile?.departmentName || ''} readOnly />
            </label>

            <label>
              직급/역할
              <input value={profile?.positionName || ''} readOnly />
            </label>

            <label>
              이메일
              <input type="email" name="email" value={form.email} onChange={changeValue} placeholder="업무 이메일" />
            </label>

            <label>
              전화번호
              <input type="text" name="phone" value={form.phone} onChange={changeValue} placeholder="010-0000-0000" />
            </label>

            <div className="admin-password-section full">
              <h3>비밀번호 변경</h3>
              <p>비밀번호를 변경하지 않으려면 아래 입력칸을 비워두세요.</p>
            </div>

            <label className="full">
              현재 비밀번호
              <input type="password" name="currentPassword" value={form.currentPassword} onChange={changeValue} />
            </label>

            <label>
              새 비밀번호
              <input type="password" name="newPassword" value={form.newPassword} onChange={changeValue} />
            </label>

            <label>
              새 비밀번호 확인
              <input type="password" name="newPasswordConfirm" value={form.newPasswordConfirm} onChange={changeValue} />
            </label>

            <div className="admin-action-row right full">
              <button type="button" className="gray" onClick={() => setForm({ ...initialForm, email: profile?.email || '', phone: profile?.phone || '' })}>
                입력 초기화
              </button>
              <button type="submit" disabled={isSaving}>
                {isSaving ? '저장 중' : '내 정보 저장'}
              </button>
            </div>
          </form>
        </article>

        <article className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>직원 계정 운영 기준</strong>
              <p>내부 직원 계정은 MIS 전용 계정으로 관리합니다.</p>
            </div>
          </div>

          <div className="admin-detail-box">
            <h3>{profile?.memberName || '직원 정보'}</h3>
            <p>{profile?.employeeNo || '-'} · {roleLabelMap[profile?.userType] || profile?.userType || '-'}</p>

            <dl>
              <div><dt>로그인 계정</dt><dd>app_member</dd></div>
              <div><dt>직원 정보</dt><dd>employee</dd></div>
              <div><dt>계정 상태</dt><dd>{profile?.status || '-'}</dd></div>
              <div><dt>로그인 방식</dt><dd>{profile?.loginType || '-'}</dd></div>
            </dl>
          </div>

          <div className="admin-guide-list mt">
            <p><b>직원 본인</b> MIS 내 정보 관리에서 본인 연락처와 비밀번호를 변경합니다.</p>
            <p><b>관리자</b> 인사관리에서 직원 계정을 생성하고 권한을 부여합니다.</p>
            <p><b>비밀번호 초기화</b> 관리자는 직원 비밀번호를 조회하지 않고 새 임시 비밀번호로 초기화만 할 수 있습니다.</p>
          </div>
        </article>
      </div>
    </section>
  );
};

export default AdminProfilePage;
