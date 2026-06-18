import { useEffect, useState } from "react";
import * as memberApi from "../../apis/memberApi";

const defaultMember = {
  userId: "kakao_4916296150",
  memberName: "김성민",
  email: "dyddl456@nate.com",
  nickname: "김성민",
  loginType: "KAKAO",
  profileImageUrl: "/images/member/profile/default-profile.png",
};

const MyPage = () => {
  console.log("MyPage 렌더링");

  const [member, setMember] = useState(defaultMember);
  const [previewUrl, setPreviewUrl] = useState(defaultMember.profileImageUrl);
  const [form, setForm] = useState({
    nickname: "",
    currentPassword: "",
    newPassword: "",
    newPasswordConfirm: "",
    profileImage: null,
  });

  const isSocialLogin = member.loginType && member.loginType !== "LOCAL";

  const getMemberInfo = async () => {
    console.log("getMemberInfo 실행");

    try {
      const response = await memberApi.info();
      console.log("회원정보 응답", response.data);

      if (response.data && typeof response.data === "object") {
        setMember(response.data);
        setPreviewUrl(response.data.profileImageUrl || defaultMember.profileImageUrl);
        setForm((prev) => ({ ...prev, nickname: response.data.nickname || "" }));
      }
    } catch (error) {
      console.log("회원정보 조회 실패 - 화면 확인용 기본 데이터 사용", error);
      setForm((prev) => ({ ...prev, nickname: defaultMember.nickname }));
    }
  };

  useEffect(() => {
    getMemberInfo();
  }, []);

  const changeValue = (e) => {
    const { name, value } = e.target;
    console.log("회원정보 입력 변경", name, value);

    setForm({
      ...form,
      [name]: value,
    });
  };

  const changeProfileImage = (e) => {
    const file = e.target.files?.[0];
    console.log("프로필 이미지 변경", file);

    if (!file) {
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert("프로필 이미지는 최대 5MB까지 등록할 수 있습니다.");
      return;
    }

    setForm({ ...form, profileImage: file });
    setPreviewUrl(URL.createObjectURL(file));
  };

  const submitMember = async (e) => {
    e.preventDefault();
    console.log("회원정보 수정 submit", form);

    if (!form.nickname.trim()) {
      alert("닉네임을 입력해 주세요.");
      return;
    }

    if (!isSocialLogin && form.newPassword !== form.newPasswordConfirm) {
      alert("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    try {
      await memberApi.update(form);
      alert("회원정보가 수정되었습니다.");
    } catch (error) {
      console.log("회원정보 수정 오류", error);
      alert("회원정보 수정 중 오류가 발생했습니다. 백엔드 REST 전환 후 다시 확인해 주세요.");
    }
  };

  return (
    <>
      <div className="mypage-page-header">
        <span>MEMBER INFO</span>
        <h1>회원정보 변경</h1>
        <p>닉네임, 비밀번호, 프로필 이미지를 변경할 수 있습니다.</p>
      </div>

      <section className="mypage-card">
        <div className="profile-edit-area">
          <div className="profile-preview">
            <img src={previewUrl} alt="프로필 이미지" />
          </div>

          <label className="profile-file-label">
            사진 변경
            <input type="file" accept="image/*" onChange={changeProfileImage} />
          </label>

          <p className="profile-help">지원 형식: jpg, jpeg, png, gif, webp / 최대 5MB</p>
        </div>

        <form className="mypage-form" onSubmit={submitMember}>
          <div className="mypage-form-grid">
            <div className="mypage-form-group">
              <label>아이디</label>
              <input value={member.userId || ""} readOnly />
            </div>

            <div className="mypage-form-group">
              <label>이름</label>
              <input value={member.memberName || ""} readOnly />
            </div>

            <div className="mypage-form-group">
              <label>이메일</label>
              <input value={member.email || ""} readOnly />
            </div>

            <div className="mypage-form-group">
              <label htmlFor="nickname">닉네임</label>
              <input id="nickname" name="nickname" value={form.nickname} onChange={changeValue} />
            </div>
          </div>

          <div className="password-box">
            <h3>비밀번호 변경</h3>
            <p>비밀번호를 변경하지 않으려면 비워두세요.</p>

            {isSocialLogin ? (
              <div className="kakao-password-notice">
                카카오 로그인 회원은 비밀번호를 변경할 수 없습니다. 비밀번호 변경은 카카오 계정에서 진행해 주세요.
              </div>
            ) : (
              <div className="mypage-form-grid">
                <div className="mypage-form-group full">
                  <label htmlFor="currentPassword">현재 비밀번호</label>
                  <input
                    id="currentPassword"
                    name="currentPassword"
                    type="password"
                    value={form.currentPassword}
                    onChange={changeValue}
                  />
                </div>

                <div className="mypage-form-group">
                  <label htmlFor="newPassword">새 비밀번호</label>
                  <input
                    id="newPassword"
                    name="newPassword"
                    type="password"
                    value={form.newPassword}
                    onChange={changeValue}
                  />
                </div>

                <div className="mypage-form-group">
                  <label htmlFor="newPasswordConfirm">새 비밀번호 확인</label>
                  <input
                    id="newPasswordConfirm"
                    name="newPasswordConfirm"
                    type="password"
                    value={form.newPasswordConfirm}
                    onChange={changeValue}
                  />
                </div>
              </div>
            )}
          </div>

          <div className="mypage-actions">
            <button type="submit" className="mypage-primary-btn">
              수정하기
            </button>
          </div>
        </form>
      </section>
    </>
  );
};

export default MyPage;
