import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import * as authApi from "../../apis/authApi";
import * as notices from "../../apis/noticeApi";
import { noticeMockList } from "../../apis/noticeMockData";
import "../../styles/notice.css";

const ADMIN_TYPES = ["ADMIN", "MANAGER", "OPERATOR", "ENGINEER"];

const NoticePage = () => {
  console.log("NoticePage 렌더링");

  const [keyword, setKeyword] = useState("");
  const [noticeList, setNoticeList] = useState([]);
  const [loginMember, setLoginMember] = useState(null);

  const isAdmin = ADMIN_TYPES.includes(loginMember?.userType);

  useEffect(() => {
    getNoticeList();
    getLoginMember();
  }, []);

  // 현재 로그인 사용자 권한 조회
  const getLoginMember = async () => {
    console.log("공지사항 로그인 사용자 정보 조회");

    const accessToken = localStorage.getItem("ACCESS_TOKEN");

    if (!accessToken) {
      setLoginMember(null);
      return;
    }

    try {
      const response = await authApi.getMyInfo();
      console.log("공지사항 로그인 사용자 정보 응답", response.data);
      setLoginMember(response.data);
    } catch (error) {
      console.log("공지사항 로그인 사용자 정보 조회 실패", error);
      setLoginMember(null);
    }
  };

  // 공지사항 목록 조회
  const getNoticeList = async () => {
    console.log("공지사항 목록 조회 실행");

    try {
      const response = await notices.list();
      const data = response.data;

      console.log("공지사항 목록 응답", data);

      setNoticeList(data);
    } catch (error) {
      console.log("공지사항 목록 조회 실패", error);

      // 백엔드 연결 전에는 게시판 목데이터를 그대로 사용
      setNoticeList(noticeMockList);
    }
  };

  // 공지사항 검색
  const searchNotice = async () => {
    console.log("공지사항 검색 실행", keyword);

    if (!keyword.trim()) {
      getNoticeList();
      return;
    }

    try {
      const response = await notices.search(keyword);
      const data = response.data;

      console.log("공지사항 검색 응답", data);

      setNoticeList(data);
    } catch (error) {
      console.log("공지사항 검색 실패", error);

      // 백엔드 연결 전에는 같은 게시판 목데이터에서 검색
      const filteredList = noticeMockList.filter((notice) =>
        notice.title.includes(keyword)
      );

      setNoticeList(filteredList);
    }
  };

  return (
    <main className="notice-page">
      <section className="notice-hero">
        <span>News & Notice</span>
        <h1>새소식</h1>
        <p>충전소 점검, 운영 안내, 서비스 공지사항을 확인하세요.</p>
      </section>

      <section className="notice-search-box">
        <div className="notice-count-area">
          <strong>전체 공지 {noticeList.length}건</strong>
          {isAdmin && <span>관리자 권한으로 접속 중입니다.</span>}
        </div>

        <div className="notice-search-right">
          <div className="notice-search-form">
            <input
              type="text"
              value={keyword}
              placeholder="공지사항 검색"
              onChange={(e) => setKeyword(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  searchNotice();
                }
              }}
            />

            <button type="button" onClick={searchNotice}>
              검색
            </button>
          </div>

          {isAdmin && (
            <Link to="/notice/write" className="notice-admin-write-btn">
              글쓰기
            </Link>
          )}
        </div>
      </section>

      <section className="notice-list-box">
        <table className="notice-table">
          <thead>
            <tr>
              <th>구분</th>
              <th>제목</th>
              <th>등록일</th>
              <th>조회수</th>
            </tr>
          </thead>

          <tbody>
            {noticeList.map((notice) => (
              <tr key={notice.noticeId}>
                <td>
                  <span className="notice-badge">{notice.category || "공지"}</span>
                </td>

                <td className="notice-title-cell">
                  <Link to={`/notice/${notice.noticeId}`}>{notice.title}</Link>
                </td>

                <td>{notice.createdAt}</td>
                <td>{notice.viewCount}</td>
              </tr>
            ))}

            {noticeList.length === 0 && (
              <tr>
                <td colSpan="4" className="notice-empty">
                  검색 결과가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </section>
    </main>
  );
};

export default NoticePage;
