import { Link } from "react-router-dom";

const CustomerCenterPage = () => {
  console.log("CustomerCenterPage 렌더링");

  return (
    <section className="customer-main">
      <div className="customer-title">
        <h1>고객센터</h1>
        <p>
          전기차 충전 예약, 결제, 충전기 장애 관련 문의를 접수하고 처리
          현황을 확인할 수 있습니다.
        </p>
      </div>

      <section className="customer-card-grid three">
        <Link to="/complaint" className="customer-service-card primary">
          <h3>민원 접수</h3>
          <p>예약, 결제, 시설장애 등 불편사항을 접수합니다.</p>
        </Link>

        <Link to="/complaints/my" className="customer-service-card">
          <h3>내 민원 내역</h3>
          <p>접수한 민원의 처리 상태와 진행 상황을 확인합니다.</p>
        </Link>

        <Link to="/customer-center/faq" className="customer-service-card">
          <h3>자주 묻는 질문</h3>
          <p>예약, 결제, 장애 신고 관련 해결 방법을 확인합니다.</p>
        </Link>
      </section>

      <section className="customer-content-grid">
        <div className="customer-content-box">
          <h2>자주 묻는 질문</h2>

          <div className="faq-item">
            <strong>예약한 충전기가 사용 중이면 어떻게 하나요?</strong>
            <p>
              예약 상세 화면에서 상태를 확인한 뒤 민원 접수를 통해
              운영기관에 문의할 수 있습니다.
            </p>
          </div>

          <div className="faq-item">
            <strong>충전소 장애를 신고할 수 있나요?</strong>
            <p>
              민원 접수에서 시설장애 유형을 선택하면 민원관리와
              장애관리로 연계됩니다.
            </p>
          </div>

          <Link to="/customer-center/faq" className="faq-more-link">
            FAQ 전체보기
          </Link>
        </div>

        <div className="customer-content-box">
          <h2>이용 안내</h2>

          <ul className="guide-list">
            <li>민원 접수 후 운영기관 담당자가 확인합니다.</li>
            <li>시설 장애 민원은 장애관리와 점검관리로 연계됩니다.</li>
            <li>처리 결과는 내 민원 내역에서 확인할 수 있습니다.</li>
            <li>긴급 장애는 우선순위를 HIGH 또는 URGENT로 접수하세요.</li>
          </ul>
        </div>
      </section>
    </section>
  );
};

export default CustomerCenterPage;