import { Outlet, useLocation } from "react-router-dom";
import CustomerSidebar from "../components/customer/CustomerSidebar";
import "../styles/customer-center.css";

const CustomerLayout = () => {
  console.log("CustomerLayout 렌더링");

  const location = useLocation();

  const renderGuide = () => {
    if (location.pathname === "/customer-center/faq") {
      return (
        <div className="guide-card">
          <h3>FAQ 안내</h3>
          <p>예약, 결제, 충전기 장애 관련 자주 묻는 질문을 제공합니다.</p>
          <p>해결되지 않는 문제는 민원 접수를 이용해 주세요.</p>
        </div>
      );
    }

    if (location.pathname === "/complaints/my") {
      return (
        <>
          <div className="guide-card">
            <h3>민원 내역 안내</h3>
            <p>접수한 민원의 처리 상태를 확인할 수 있습니다.</p>
            <p>제목을 클릭하면 상세 내용과 처리 이력을 볼 수 있습니다.</p>
          </div>

          <div className="guide-card">
            <h3>상태 안내</h3>
            <p>접수: 민원이 정상 접수된 상태입니다.</p>
            <p>처리중: 담당자가 민원을 확인하고 처리 중입니다.</p>
            <p>완료: 민원 처리가 완료된 상태입니다.</p>
          </div>
        </>
      );
    }

    if (location.pathname.startsWith("/complaints/my/")) {
      return (
        <div className="guide-card">
          <h3>민원 상세 안내</h3>
          <p>접수 내용과 처리 상태를 확인할 수 있습니다.</p>
          <p>처리 이력은 담당자 조치에 따라 순서대로 표시됩니다.</p>
        </div>
      );
    }

    return (
      <>
        <div className="guide-card">
          <h3>민원 접수 안내</h3>
          <p>정확한 접수를 위해 상세하게 작성해 주세요.</p>
          <p>충전기명이 있으면 처리에 도움이 됩니다.</p>
          <p>접수 결과는 선택하신 방법으로 안내해 드립니다.</p>
        </div>

        <div className="guide-card">
          <h3>민원 처리 절차</h3>

          <ol className="process-list">
            <li><span>1</span>민원 접수</li>
            <li><span>2</span>AI 자동 분류</li>
            <li><span>3</span>담당자 배정</li>
            <li><span>4</span>처리 진행</li>
            <li><span>5</span>처리 완료</li>
          </ol>
        </div>
      </>
    );
  };

  return (
    <main className="customer-complaint-page">
      <CustomerSidebar />

      <Outlet />

      <aside className="complaint-guide">
        {renderGuide()}
      </aside>
    </main>
  );
};

export default CustomerLayout;