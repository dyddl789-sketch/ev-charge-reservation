import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import * as notices from "../../apis/noticeApi";
import { noticeMockList } from "../../apis/noticeMockData";

const NoticeSection = () => {
  console.log("NoticeSection 렌더링");

  const [noticeList, setNoticeList] = useState([]);

  useEffect(() => {
    getMainNotice();
  }, []);

  // 메인 새소식 조회
  const getMainNotice = async () => {
    console.log("메인 공지 조회 실행");

    try {
      const response = await notices.main();
      const data = response.data;

      console.log("메인 공지 응답", data);

      setNoticeList(data);
    } catch (error) {
      console.log("메인 공지 조회 실패", error);

      // 백엔드 연결 전에는 게시판 목데이터 중 최신 4건 사용
      setNoticeList(noticeMockList.slice(0, 4));
    }
  };

  return (
    <section className="main-notice-section">
      <div className="main-notice-header">
        <div>
          <span>News & Notice</span>
          <h2>새소식</h2>
          <p>전기차 충전 서비스 관련 최신 공지사항을 확인하세요.</p>
        </div>

        <Link to="/notice" className="notice-more-btn">
          전체보기 →
        </Link>
      </div>

      <div className="notice-card-wrap">
        {noticeList.map((notice) => (
          <Link
            key={notice.noticeId}
            to={`/notice/${notice.noticeId}`}
            className="notice-card"
          >
            <span className="notice-category">공지</span>

            <h3>{notice.title}</h3>

            <p>{notice.createdAt}</p>
          </Link>
        ))}
      </div>
    </section>
  );
};

export default NoticeSection;