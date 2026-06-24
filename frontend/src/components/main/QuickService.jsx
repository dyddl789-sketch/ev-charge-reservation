import { Link } from "react-router-dom";

const QuickService = () => {
  console.log("QuickService 렌더링");

  const serviceGroups = [
    {
      groupTitle: "충전 서비스",
      menus: [
        {
          title: "충전소 찾기",
          icon: "🔍",
          link: "/stations",
        },
        {
          title: "AI 충전 비서",
          icon: "🤖",
          link: "/ai-chat",
        },
        {
          title: "내 차량 관리",
          icon: "🚗",
          link: "/vehicles",
        },
      ],
    },
    {
      groupTitle: "이용 정보",
      menus: [
        {
          title: "내 예약 조회",
          icon: "📋",
          link: "/my-reservations",
        },
        {
          title: "민원 접수",
          icon: "📢",
          link: "/complaint",
        },
        {
          title: "공지사항",
          icon: "📰",
          link: "/notice",
        },
      ],
    },
  ];

  return (
    <aside className="quick-service-box">
      <div className="quick-service-title-area">
        <h2>자주 찾는 서비스</h2>
        <p>전기차 충전 서비스와 이용 정보를 빠르게 확인하세요.</p>
      </div>

      <div className="quick-service-group-wrap">
        {serviceGroups.map((group) => (
          <div className="quick-service-group" key={group.groupTitle}>
            <h3>{group.groupTitle}</h3>

            <div className="quick-service-grid">
              {group.menus.map((service) => (
                <Link
                  to={service.link}
                  className="quick-service-item"
                  key={service.title}
                >
                  <div className="quick-service-icon">{service.icon}</div>
                  <p>{service.title}</p>
                </Link>
              ))}
            </div>
          </div>
        ))}
      </div>
    </aside>
  );
};

export default QuickService;
