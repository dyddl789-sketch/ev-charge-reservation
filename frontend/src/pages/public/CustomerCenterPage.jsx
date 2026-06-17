import { Link } from "react-router-dom";

import "../../styles/customer-center.css";

const CustomerCenterPage = () => {
  console.log("CustomerCenterPage 렌더링");

  const faqList = [
    {
      question: "예약한 충전기가 사용 중이면 어떻게 하나요?",
      answer:
        "예약 상세 화면에서 상태를 확인한 뒤 고객센터 민원 접수를 통해 운영기관에 문의할 수 있습니다.",
    },
    {
      question: "충전소 장애를 신고할 수 있나요?",
      answer:
        "민원 접수에서 시설장애 유형을 선택하면 운영기관 MIS의 민원관리와 장애관리로 연계됩니다.",
    },
    {
      question: "AI 충전 비서는 어떤 기능을 제공하나요?",
      answer:
        "대표 차량, 충전기 출력, 배터리 잔량을 기준으로 충전 시간과 비용을 계산하고 충전소를 추천합니다.",
    },
  ];

  return (
    <section className="customer-center-page">
      <div className="customer-center-inner">
        <div className="customer-hero">
          <p>Customer Center</p>
          <h1>고객센터</h1>
          <span>
            전기차 충전 예약, 결제, 충전기 장애 관련 문의를 빠르게 접수하고 확인할 수 있습니다.
          </span>
        </div>

        <div className="customer-quick-grid">
          <Link to="/complaint" className="customer-quick-card primary">
            <strong>민원 접수</strong>
            <p>예약, 결제, 시설장애 등 불편사항을 접수합니다.</p>
          </Link>

          <Link to="/complaints/my" className="customer-quick-card">
            <strong>내 민원 내역</strong>
            <p>접수한 민원의 처리 상태와 진행 상황을 확인합니다.</p>
          </Link>

          <Link to="/notice" className="customer-quick-card">
            <strong>새소식</strong>
            <p>운영기관 공지사항과 점검 안내를 확인합니다.</p>
          </Link>

          <Link to="/ai-chat" className="customer-quick-card">
            <strong>AI 충전 비서</strong>
            <p>충전소 추천과 충전 시간 계산을 문의합니다.</p>
          </Link>
        </div>

        <div className="customer-content-grid">
          <article className="customer-panel">
            <h2>자주 묻는 질문</h2>

            <div className="faq-list">
              {faqList.map((faq) => (
                <div className="faq-item" key={faq.question}>
                  <strong>{faq.question}</strong>
                  <p>{faq.answer}</p>
                </div>
              ))}
            </div>
          </article>

          <article className="customer-panel notice-panel">
            <h2>이용 안내</h2>
            <ul>
              <li>민원 접수 후 운영기관 담당자가 확인합니다.</li>
              <li>시설 장애 민원은 장애관리와 점검관리로 연계됩니다.</li>
              <li>처리 결과는 내 민원 내역에서 확인할 수 있습니다.</li>
              <li>긴급 장애는 우선순위 HIGH 또는 URGENT로 접수하세요.</li>
            </ul>
          </article>
        </div>
      </div>
    </section>
  );
};

export default CustomerCenterPage;
