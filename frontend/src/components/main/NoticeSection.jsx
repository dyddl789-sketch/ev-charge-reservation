const NoticeSection = () => {
  console.log("NoticeSection 렌더링");

  const notices = [
    {
      title: "2026년 전기자동차 충전 인프라 정기점검 안내",
      desc: "공공 전기차 충전시설의 안정적인 운영을 위해 정기점검 일정을 안내드립니다.",
      date: "2026.06.15",
    },
    {
      title: "전기차 충전 예약 서비스 이용 안내",
      desc: "충전소 검색부터 예약, 인증코드 확인까지 통합 서비스를 제공합니다.",
      date: "2026.06.12",
    },
    {
      title: "AI 충전 비서 시범 서비스 안내",
      desc: "대표 차량과 저장 위치를 기준으로 가까운 충전소를 추천합니다.",
      date: "2026.06.08",
    },
    {
      title: "충전기 장애 및 이용 불편 신고 안내",
      desc: "충전 불가, 커넥터 파손, 결제 오류 등 이용 불편사항을 접수할 수 있습니다.",
      date: "2026.06.01",
    },
  ];

  return (
    <section className="notice-section">
      <div className="section-inner">
        <div className="notice-heading center">
          <span>NOTICE</span>
          <h2>새소식</h2>
          <p>공공 전기차 충전 인프라 운영 소식을 알려드립니다.</p>
        </div>

        <div className="notice-grid">
          {notices.map((notice, index) => (
            <article className="notice-card" key={index}>
              <h3>{notice.title}</h3>
              <p>{notice.desc}</p>

              <div className="notice-bottom">
                <span>{notice.date}</span>
                <button type="button" aria-label="공지사항 상세보기">
                  +
                </button>
              </div>
            </article>
          ))}
        </div>

        <div className="notice-more">
          <button type="button">전체보기 →</button>
        </div>
      </div>
    </section>
  );
};

export default NoticeSection;