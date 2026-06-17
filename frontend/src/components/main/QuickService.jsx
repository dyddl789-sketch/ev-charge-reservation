import { Link } from "react-router-dom";

const QuickService = () => {
  console.log("QuickService 렌더링");

  const tabs = ["무공해차 구매\n예약자", "충전소\n찾기", "차량\n등록", "충전소\n관리자"];

  const quickServices = [
    { title: "충전소 찾기", icon: "🌱", link: "/stations" },
    { title: "충전 예약", icon: "🚘", link: "/reservation" },
    { title: "차량 등록", icon: "🧾", link: "/vehicles/register" },
    { title: "내 예약 조회", icon: "💳", link: "/reservation/my" },
    { title: "AI 충전 비서", icon: "🏛️", link: "/ai-chat" },
    { title: "전기차 충전요금", icon: "⛽", link: "/charge-fee" },
  ];

  return (
    <aside className="quick-service-box">
      <h2>자주 찾는 서비스</h2>

      <div className="quick-tab-list">
        {tabs.map((tab, index) => (
          <button
            type="button"
            className={`quick-tab ${index === 0 ? "active" : ""}`}
            key={index}
          >
            {tab.split("\n").map((line, lineIndex) => (
              <span key={lineIndex}>{line}</span>
            ))}
          </button>
        ))}
      </div>

      <div className="quick-service-grid">
        {quickServices.map((service, index) => (
          <Link to={service.link} className="quick-service-item" key={index}>
            <div className="quick-service-icon">{service.icon}</div>
            <p>{service.title}</p>
          </Link>
        ))}
      </div>

      <button type="button" className="quick-more-button">
        ˅
      </button>
    </aside>
  );
};

export default QuickService;