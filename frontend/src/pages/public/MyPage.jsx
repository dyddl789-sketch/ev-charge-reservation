import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as memberApi from "../../apis/memberApi";
import * as authApi from "../../apis/authApi";

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

  const navigate = useNavigate();

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

  const getProfileImageUrl = (profileImageUrl) => {
    console.log("프로필 이미지 URL", profileImageUrl);

    if (!profileImageUrl) {
      return "/images/member/profile/default-profile.png";
    }

    if (profileImageUrl.startsWith("blob:")) {
      return profileImageUrl;
    }

    if (profileImageUrl.startsWith("http")) {
      return profileImageUrl;
    }

    if (profileImageUrl.startsWith("/")) {
      return profileImageUrl;
    }

    return `/${profileImageUrl}`;
  };

  const logoutAfterPasswordChange = async () => {
    console.log("비밀번호 변경 완료 후 로그아웃 처리 시작");

    try {
      await authApi.logout();
      console.log("비밀번호 변경 후 로그아웃 API 성공");
    } catch (error) {
      console.log("비밀번호 변경 후 로그아웃 API 오류 - 로컬 토큰은 제거", error);
    }

    localStorage.removeItem("ACCESS_TOKEN");
    localStorage.removeItem("REFRESH_TOKEN");

    window.dispatchEvent(new Event("auth-change"));

    alert("비밀번호가 변경되었습니다. 다시 로그인해 주세요.");
    navigate("/login", { replace: true });
  };

  const submitMember = async (e) => {
    e.preventDefault();
    console.log("회원정보 수정 submit", form);

    if (!form.nickname.trim()) {
      alert("닉네임을 입력해 주세요.");
      return;
    }

    // 비밀번호 입력칸 중 하나라도 작성했다면 비밀번호 변경 요청으로 판단한다.
    const isPasswordChangeRequested =
      !isSocialLogin &&
      Boolean(
        form.currentPassword.trim() ||
        form.newPassword.trim() ||
        form.newPasswordConfirm.trim()
      );

    if (isPasswordChangeRequested && !form.currentPassword.trim()) {
      alert("현재 비밀번호를 입력해 주세요.");
      return;
    }

    if (isPasswordChangeRequested && !form.newPassword.trim()) {
      alert("새 비밀번호를 입력해 주세요.");
      return;
    }

    if (isPasswordChangeRequested && !form.newPasswordConfirm.trim()) {
      alert("새 비밀번호 확인을 입력해 주세요.");
      return;
    }

    if (isPasswordChangeRequested && form.newPassword !== form.newPasswordConfirm) {
      alert("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    try {
      await memberApi.update(form);

      if (isPasswordChangeRequested) {
        await logoutAfterPasswordChange();
        return;
      }

      alert("회원정보가 수정되었습니다.");

      window.dispatchEvent(new Event("auth-change"));

      await getMemberInfo();
    } catch (error) {
      console.log("회원정보 수정 오류", error);

      const message =
        error.response?.data?.message || "회원정보 수정 중 오류가 발생했습니다.";

      alert(message);
    }
  };

  const deleteMember = async () => {
    console.log("회원탈퇴 클릭", member);

    if (!member.memberId) {
      alert("회원 정보를 불러온 후 다시 시도해 주세요.");
      return;
    }

    const firstConfirm = window.confirm("정말 회원탈퇴를 진행하시겠습니까?");
    if (!firstConfirm) {
      return;
    }

    const secondConfirm = window.confirm(
      "회원탈퇴 시 계정 정보가 비활성화되며, 다시 로그인할 수 없습니다. 계속하시겠습니까?"
    );

    if (!secondConfirm) {
      return;
    }

    try {
      await memberApi.remove(member.memberId);

      localStorage.removeItem("ACCESS_TOKEN");
      localStorage.removeItem("REFRESH_TOKEN");

      window.dispatchEvent(new Event("auth-change"));

      alert("회원탈퇴가 완료되었습니다.");
      window.location.href = "/";
    } catch (error) {
      console.log("회원탈퇴 오류", error);

      const message =
        error.response?.data?.message || "회원탈퇴 중 오류가 발생했습니다.";

      alert(message);
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
            <img src={getProfileImageUrl(previewUrl)} alt="프로필 이미지" />
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
            <button type="button" className="mypage-danger-btn" onClick={deleteMember}>
              회원탈퇴
            </button>

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