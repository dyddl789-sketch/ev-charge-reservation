const Footer = () => {
  console.log("Footer 렌더링");

  const moveTop = () => {
    console.log("TOP 버튼 클릭");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <footer className="public-footer">
      <div className="footer-menu-bar">
        <div className="footer-menu-inner">
          <a href="#">개인정보처리방침</a>
          <a href="#">이메일무단수집거부</a>
          <a href="#">영상정보처리기기 운영관리지침</a>
          <a href="#">불편민원신고센터</a>

          <button type="button" onClick={moveTop}>
            TOP
          </button>
        </div>
      </div>

      <div className="footer-info">
        <div className="footer-logo">
          <strong>EV</strong>
          <span>공공 전기차 충전 인프라</span>
        </div>

        <div className="footer-address">
          <p>우) 47545 부산광역시 연제구 중앙대로 1001 공공 전기차 충전 인프라 운영센터</p>
          <p>대표전화 : 1661-0970 | 충전시설 이용문의 : 1661-9408 | 민원상담 : 120</p>
          <p>Copyright 2026. EV Charge Reservation MIS Platform. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;