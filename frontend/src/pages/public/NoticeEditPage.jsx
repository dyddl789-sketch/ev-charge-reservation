import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import * as adminApi from "../../apis/adminApi";
import * as authApi from "../../apis/authApi";
import "../../styles/notice.css";

const ADMIN_TYPES = ["ADMIN", "MANAGER", "OPERATOR", "ENGINEER"];

const NoticeEditPage = () => {
  console.log("NoticeEditPage 렌더링");

  const { noticeId } = useParams();
  const navigate = useNavigate();

  const [loginMember, setLoginMember] = useState(null);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [form, setForm] = useState({
    title: "",
    content: "",
    category: "공지",
    status: "게시",
    isPinned: false,
    isPublic: true,
  });

  const isAdmin = ADMIN_TYPES.includes(loginMember?.userType);

  useEffect(() => {
    checkAdminAuthAndLoadNotice();
  }, [noticeId]);

  // 관리자 권한 확인 후 관리자 상세 API로 공지 조회
  const checkAdminAuthAndLoadNotice = async () => {
    console.log("공지사항 수정 권한 확인 및 상세 조회", noticeId);

    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (!accessToken) {
      alert("로그인이 필요합니다.");
      navigate("/login?authMsg=loginRequired");
      return;
    }

    try {
      const authResponse = await authApi.getMyInfo();
      console.log("공지사항 수정 권한 응답", authResponse.data);
      setLoginMember(authResponse.data);

      if (!ADMIN_TYPES.includes(authResponse.data.userType)) {
        alert("공지사항 수정 권한이 없습니다.");
        navigate("/notice");
        return;
      }

      const noticeResponse = await adminApi.noticeDetail(noticeId);
      console.log("공지사항 수정 상세 응답", noticeResponse.data);

      setForm({
        title: noticeResponse.data.title || "",
        content: noticeResponse.data.content || "",
        category: noticeResponse.data.category || "공지",
        status: noticeResponse.data.status || "게시",
        isPinned: Boolean(noticeResponse.data.isPinned),
        isPublic: Boolean(noticeResponse.data.isPublic),
      });
    } catch (error) {
      console.log("공지사항 수정 화면 초기화 실패", error);
      alert("공지사항 정보를 불러오지 못했습니다.");
      navigate("/notice");
    } finally {
      setIsCheckingAuth(false);
      setIsLoading(false);
    }
  };

  // 입력값 변경 처리
  const changeValue = (e) => {
    const { name, value, type, checked } = e.target;
    console.log("공지사항 수정 입력 변경", name, type === "checkbox" ? checked : value);

    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  // 공지사항 수정
  const submitNotice = async (e) => {
    e.preventDefault();
    console.log("공지사항 수정 submit", noticeId, form);

    if (!form.title.trim()) {
      alert("제목을 입력해 주세요.");
      return;
    }

    if (!form.content.trim()) {
      alert("내용을 입력해 주세요.");
      return;
    }

    try {
      setIsSubmitting(true);
      const response = await adminApi.updateNotice(noticeId, form);
      console.log("공지사항 수정 응답", response.data);

      alert("공지사항이 수정되었습니다.");
      navigate(`/notice/${noticeId}`);
    } catch (error) {
      console.log("공지사항 수정 실패", error);
      alert(error.response?.data?.message || "공지사항 수정에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isCheckingAuth || isLoading) {
    return (
      <main className="notice-page">
        <section className="notice-form-card">
          <p className="notice-empty">공지사항 정보를 불러오고 있습니다.</p>
        </section>
      </main>
    );
  }

  if (!isAdmin) {
    return null;
  }

  return (
    <main className="notice-page">
      <section className="notice-form-card">
        <div className="notice-form-header">
          <span>Notice Management</span>
          <h1>공지사항 수정</h1>
          <p>등록된 공지사항의 제목, 내용, 공개 상태를 수정합니다.</p>
        </div>

        <form className="notice-form" onSubmit={submitNotice}>
          <div className="notice-form-row">
            <label htmlFor="category">구분</label>
            <select id="category" name="category" value={form.category} onChange={changeValue}>
              <option value="공지">공지</option>
              <option value="안내">안내</option>
              <option value="점검">점검</option>
            </select>
          </div>

          <div className="notice-form-row">
            <label htmlFor="title">제목</label>
            <input
              id="title"
              type="text"
              name="title"
              value={form.title}
              placeholder="공지사항 제목을 입력하세요."
              onChange={changeValue}
            />
          </div>

          <div className="notice-form-row align-start">
            <label htmlFor="content">내용</label>
            <textarea
              id="content"
              name="content"
              value={form.content}
              placeholder="공지사항 내용을 입력하세요."
              onChange={changeValue}
            />
          </div>

          <div className="notice-form-row notice-check-row">
            <label>게시 설정</label>
            <div className="notice-check-list">
              <label>
                <input
                  type="checkbox"
                  name="isPublic"
                  checked={form.isPublic}
                  onChange={changeValue}
                />
                사용자에게 공개
              </label>
              <label>
                <input
                  type="checkbox"
                  name="isPinned"
                  checked={form.isPinned}
                  onChange={changeValue}
                />
                상단 고정
              </label>
            </div>
          </div>

          <div className="notice-form-row">
            <label htmlFor="status">상태</label>
            <select id="status" name="status" value={form.status} onChange={changeValue}>
              <option value="게시">게시</option>
              <option value="임시저장">임시저장</option>
              <option value="숨김">숨김</option>
            </select>
          </div>

          <div className="notice-form-btn-area">
            <Link to={`/notice/${noticeId}`} className="notice-btn notice-btn-light">
              취소
            </Link>
            <button type="submit" className="notice-btn notice-btn-primary" disabled={isSubmitting}>
              {isSubmitting ? "수정중" : "수정"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
};

export default NoticeEditPage;
