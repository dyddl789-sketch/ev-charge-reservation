import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import * as adminApi from "../../apis/adminApi";
import * as authApi from "../../apis/authApi";
import * as notices from "../../apis/noticeApi";
import { noticeMockList } from "../../apis/noticeMockData";
import "../../styles/notice.css";

const ADMIN_TYPES = ["ADMIN", "MANAGER", "OPERATOR", "ENGINEER"];

// React 개발 모드 StrictMode에서 상세 API가 짧은 시간 안에 2번 호출되어 조회수가 2씩 증가하는 문제를 방지한다.
const NOTICE_DETAIL_CACHE_TTL = 1500;
const noticeDetailRequestCache = new Map();

const requestNoticeDetailOnce = (noticeId) => {
  const cacheKey = String(noticeId);
  const cached = noticeDetailRequestCache.get(cacheKey);

  if (cached && Date.now() - cached.createdAt < NOTICE_DETAIL_CACHE_TTL) {
    console.log("공지사항 상세 중복 요청 방지 - 기존 요청 재사용", noticeId);
    return cached.promise;
  }

  const promise = notices.read(noticeId).finally(() => {
    window.setTimeout(() => {
      const current = noticeDetailRequestCache.get(cacheKey);
      if (current?.promise === promise) {
        noticeDetailRequestCache.delete(cacheKey);
      }
    }, NOTICE_DETAIL_CACHE_TTL);
  });

  noticeDetailRequestCache.set(cacheKey, {
    promise,
    createdAt: Date.now(),
  });

  return promise;
};

const NoticeDetailPage = () => {
  console.log("NoticeDetailPage 렌더링");

  const { noticeId } = useParams();
  const navigate = useNavigate();

  const [notice, setNotice] = useState(null);
  const [loginMember, setLoginMember] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const isAdmin = ADMIN_TYPES.includes(loginMember?.userType);

  useEffect(() => {
    getNotice();
    getLoginMember();
  }, [noticeId]);

  // 현재 로그인 사용자 권한 조회
  const getLoginMember = async () => {
    console.log("공지사항 상세 로그인 사용자 정보 조회");

    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (!accessToken) {
      setLoginMember(null);
      return;
    }

    try {
      const response = await authApi.getMyInfo();
      console.log("공지사항 상세 로그인 사용자 정보 응답", response.data);
      setLoginMember(response.data);
    } catch (error) {
      console.log("공지사항 상세 로그인 사용자 정보 조회 실패", error);
      setLoginMember(null);
    }
  };

  // 공지사항 상세 조회
  const getNotice = async () => {
    console.log("공지사항 상세 조회 실행", noticeId);

    try {
      const response = await requestNoticeDetailOnce(noticeId);
      const data = response.data;

      console.log("공지사항 상세 응답", data);

      setNotice(data);
    } catch (error) {
      console.log("공지사항 상세 조회 실패", error);

      // 백엔드 연결 전에는 게시판 목데이터에서 noticeId로 조회
      const mockNotice = noticeMockList.find(
        (item) => String(item.noticeId) === String(noticeId)
      );

      setNotice(mockNotice || null);
    }
  };

  // 관리자 공지사항 삭제 처리
  const deleteNotice = async () => {
    console.log("공지사항 삭제 버튼 클릭", noticeId);

    const isConfirm = window.confirm("공지사항을 삭제 처리하시겠습니까?");

    if (!isConfirm) {
      return;
    }

    try {
      setIsDeleting(true);
      const response = await adminApi.deleteNotice(noticeId);
      console.log("공지사항 삭제 응답", response.data);

      alert("공지사항이 삭제 처리되었습니다.");
      navigate("/notice");
    } catch (error) {
      console.log("공지사항 삭제 실패", error);
      alert("공지사항 삭제에 실패했습니다. 관리자 권한을 확인해 주세요.");
    } finally {
      setIsDeleting(false);
    }
  };

  if (!notice) {
    return (
      <main className="notice-page">
        <section className="notice-list-box">
          <p className="notice-empty">공지사항을 찾을 수 없습니다.</p>

          <div className="notice-detail-btn-area">
            <Link to="/notice" className="notice-btn notice-btn-light">
              목록
            </Link>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="notice-page">
      <section className="notice-detail-card">
        <div className="notice-detail-header">
          <div className="notice-detail-top">
            <span className="notice-badge">{notice.category || "공지"}</span>

            {isAdmin && (
              <div className="notice-admin-action-area">
                <Link
                  to={`/notice/${notice.noticeId}/edit`}
                  className="notice-btn notice-btn-primary"
                >
                  수정
                </Link>
                <button
                  type="button"
                  className="notice-btn notice-btn-danger"
                  onClick={deleteNotice}
                  disabled={isDeleting}
                >
                  {isDeleting ? "삭제중" : "삭제"}
                </button>
              </div>
            )}
          </div>

          <h1>{notice.title}</h1>

          <div className="notice-info-grid">
            <div>
              <strong>작성자</strong>
              <span>{notice.writerName || "관리자"}</span>
            </div>
            <div>
              <strong>등록일</strong>
              <span>{notice.createdAt}</span>
            </div>
            <div>
              <strong>조회수</strong>
              <span>{notice.viewCount}</span>
            </div>
          </div>
        </div>

        <div className="notice-content">
          {(notice.content || "").split("\n").map((line, index) => (
            <p key={index}>{line || "\u00a0"}</p>
          ))}
        </div>

        <div className="notice-detail-btn-area">
          <Link to="/notice" className="notice-btn notice-btn-light">
            목록
          </Link>
        </div>
      </section>
    </main>
  );
};

export default NoticeDetailPage;
