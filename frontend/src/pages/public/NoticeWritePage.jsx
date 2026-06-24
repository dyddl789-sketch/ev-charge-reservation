import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import * as adminApi from "../../apis/adminApi";
import * as authApi from "../../apis/authApi";
import "../../styles/notice.css";

const ADMIN_TYPES = ["ADMIN", "MANAGER", "OPERATOR", "ENGINEER"];

const NoticeWritePage = () => {
  console.log("NoticeWritePage 렌더링");

  const navigate = useNavigate();

  const [loginMember, setLoginMember] = useState(null);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
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
    checkAdminAuth();
  }, []);

  // 관리자 권한 확인
  const checkAdminAuth = async () => {
    console.log("공지사항 작성 권한 확인");

    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (!accessToken) {
      alert("로그인이 필요합니다.");
      navigate("/login?authMsg=loginRequired");
      return;
    }

    try {
      const response = await authApi.getMyInfo();
      console.log("공지사항 작성 권한 응답", response.data);
      setLoginMember(response.data);

      if (!ADMIN_TYPES.includes(response.data.userType)) {
        alert("공지사항 작성 권한이 없습니다.");
        navigate("/notice");
      }
    } catch (error) {
      console.log("공지사항 작성 권한 확인 실패", error);
      alert("로그인 정보를 확인할 수 없습니다.");
      navigate("/login?authMsg=loginRequired");
    } finally {
      setIsCheckingAuth(false);
    }
  };

  // 입력값 변경 처리
  const changeValue = (e) => {
    const { name, value, type, checked } = e.target;
    console.log("공지사항 작성 입력 변경", name, type === "checkbox" ? checked : value);

    setForm({
      ...form,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  // 공지사항 등록
  const submitNotice = async (e) => {
    e.preventDefault();
    console.log("공지사항 등록 submit", form);

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
      const response = await adminApi.createNotice(form);
      console.log("공지사항 등록 응답", response.data);

      const savedNoticeId = response.data?.notice?.noticeId;
      alert("공지사항이 등록되었습니다.");

      if (savedNoticeId) {
        navigate(`/notice/${savedNoticeId}`);
      } else {
        navigate("/notice");
      }
    } catch (error) {
      console.log("공지사항 등록 실패", error);
      alert(error.response?.data?.message || "공지사항 등록에 실패했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isCheckingAuth) {
    return (
      <main className="notice-page">
        <section className="notice-form-card">
          <p className="notice-empty">관리자 권한을 확인하고 있습니다.</p>
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
          <h1>공지사항 글쓰기</h1>
          <p>운영기관 관리자 권한으로 사용자에게 노출할 공지사항을 등록합니다.</p>
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
            <Link to="/notice" className="notice-btn notice-btn-light">
              취소
            </Link>
            <button type="submit" className="notice-btn notice-btn-primary" disabled={isSubmitting}>
              {isSubmitting ? "등록중" : "등록"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
};

export default NoticeWritePage;
