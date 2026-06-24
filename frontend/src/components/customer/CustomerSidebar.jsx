import { Link, useLocation } from "react-router-dom";

const CustomerSidebar = () => {
  console.log("CustomerSidebar 렌더링");

  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path;
  };

  return (
    <aside className="customer-side">
      <h2>고객센터</h2>

      <nav>
        <Link
          to="/customer-center"
          className={isActive("/customer-center") ? "active" : ""}
        >
          고객센터 홈
        </Link>

        <Link
          to="/customer-center/faq"
          className={isActive("/customer-center/faq") ? "active" : ""}
        >
          자주 묻는 질문
        </Link>

        <Link
          to="/complaint"
          className={isActive("/complaint") ? "active" : ""}
        >
          민원 접수
        </Link>

        <Link
          to="/complaints/my"
          className={
            location.pathname.startsWith("/complaints/my") ? "active" : ""
          }
        >
          민원 내역 조회
        </Link>
      </nav>

      <div className="customer-help-box">
        <h3>고객센터 안내</h3>
        <p>문의사항이 있으신가요?</p>
        <strong>1234-5678</strong>
        <span>평일 09:00 ~ 18:00</span>
        <small>주말/공휴일 휴무</small>
      </div>
    </aside>
  );
};

export default CustomerSidebar;