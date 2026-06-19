import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import * as notices from "../../apis/noticeApi";
import { noticeMockList } from "../../apis/noticeMockData";
import "../../styles/notice.css";

const NoticeDetailPage = () => {
  console.log("NoticeDetailPage 렌더링");

  const { noticeId } = useParams();

  const [notice, setNotice] = useState(null);

  useEffect(() => {
    getNotice();
  }, [noticeId]);

  // 공지사항 상세 조회
  const getNotice = async () => {
    console.log("공지사항 상세 조회 실행", noticeId);

    try {
      const response = await notices.read(noticeId);
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

  if (!notice) {
    return (
      <main className="notice-page">
        <section className="notice-list-box">
          <p className="notice-empty">공지사항을 찾을 수 없습니다.</p>

          <div className="notice-detail-btn-area">
            <Link to="/notice">목록</Link>
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="notice-page">
      <section className="notice-detail-page">
        <div className="notice-detail-header">
          <span className="notice-badge">공지</span>

          <h1>{notice.title}</h1>

          <div className="notice-info">
            <span>작성자 : {notice.writerName}</span>
            <span>조회수 : {notice.viewCount}</span>
            <span>{notice.createdAt}</span>
          </div>
        </div>

        <div className="notice-content">
          {notice.content.split("\n").map((line, index) => (
            <p key={index}>{line}</p>
          ))}
        </div>

        <div className="notice-detail-btn-area">
          <Link to="/notice">목록</Link>
        </div>
      </section>
    </main>
  );
};

export default NoticeDetailPage;