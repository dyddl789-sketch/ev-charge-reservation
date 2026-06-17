import { useState } from "react";
import CustomerSidebar from "../../components/customer/CustomerSidebar";
import "../../styles/complaint.css";

const CustomerFaqPage = () => {
  console.log("CustomerFaqPage 렌더링");

  const [openId, setOpenId] = useState(1);

  const faqList = [
    {
      id: 1,
      question: "예약한 충전기가 사용 중이면 어떻게 하나요?",
      answer:
        "예약 상세 화면에서 현재 충전기 상태를 확인할 수 있습니다. 예약 시간 이후에도 충전기가 사용 중인 경우 고객센터의 민원 접수를 통해 운영기관에 문의해 주세요.",
    },
    {
      id: 2,
      question: "충전소 장애를 신고할 수 있나요?",
      answer:
        "고객센터 → 민원 접수 메뉴에서 충전기고장, 시설물 파손, 충전 속도 이상 유형을 선택하여 접수할 수 있습니다. 접수된 민원은 운영기관 MIS의 민원관리와 장애관리로 연계됩니다.",
    },
    {
      id: 3,
      question: "민원 처리 결과는 어디서 확인하나요?",
      answer:
        "고객센터 → 민원 내역 조회 메뉴에서 접수한 민원의 처리 상태, 처리 이력, 담당자 답변을 확인할 수 있습니다.",
    },
    {
      id: 4,
      question: "충전 예약 취소는 어떻게 하나요?",
      answer:
        "마이페이지 → 내 예약 조회 메뉴에서 예약 상세 화면으로 이동한 뒤 취소할 수 있습니다. 단, 이미 충전이 시작된 예약은 취소할 수 없습니다.",
    },
    {
      id: 5,
      question: "결제 오류가 발생했어요.",
      answer:
        "카드 한도, 결제 서버 통신 오류, 충전기 통신 오류 등으로 결제가 실패할 수 있습니다. 동일한 문제가 반복되면 민원 접수 메뉴에서 결제문의 유형으로 접수해 주세요.",
    },
  ];

  const toggleFaq = (id) => {
    console.log("FAQ 열기/닫기", id);
    setOpenId(openId === id ? null : id);
  };

  return (
    <main className="customer-complaint-page">
      <CustomerSidebar />

      <section className="complaint-form-area">
        <div className="complaint-title-box">
          <h1>자주 묻는 질문</h1>
          <p>전기차 충전 서비스 이용 중 자주 묻는 질문을 확인할 수 있습니다.</p>
        </div>

        <div className="faq-list-box">
          {faqList.map((faq) => (
            <div className="faq-accordion-item" key={faq.id}>
              <button type="button" onClick={() => toggleFaq(faq.id)}>
                <span>Q. {faq.question}</span>
                <strong>{openId === faq.id ? "−" : "+"}</strong>
              </button>

              {openId === faq.id && (
                <div className="faq-answer">
                  <p>{faq.answer}</p>
                </div>
              )}
            </div>
          ))}
        </div>
      </section>

      <aside className="complaint-guide">
        <div className="guide-card">
          <h3>FAQ 안내</h3>
          <p>예약, 결제, 충전기 장애 관련 자주 묻는 질문을 제공합니다.</p>
          <p>해결되지 않는 문제는 민원 접수를 이용해 주세요.</p>
        </div>
      </aside>
    </main>
  );
};

export default CustomerFaqPage;