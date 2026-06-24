import { Link } from "react-router-dom";

import { Autoplay, Pagination, Navigation } from "swiper/modules";
import { Swiper, SwiperSlide } from "swiper/react";

import "swiper/css";
import "swiper/css/pagination";
import "swiper/css/navigation";

const BannerSlider = () => {
  console.log("BannerSlider 렌더링");

  const banners = [
    {
      title: "지도에서 선택하는\n충전소 찾기 서비스",
      desc: "충전소를 먼저 찾고, 선택한 충전소에서 바로 예약을 진행하세요.",
      buttonText: "충전소 찾기",
      link: "/stations",
      bgClass: "banner-reservation",
      badge: "STATION SEARCH",
    },
    {
      title: "내 차량 등록하고\n맞춤 충전소 추천받기",
      desc: "차량 모델, 배터리 용량, 커넥터 정보를 기반으로 최적의 충전소를 안내합니다.",
      buttonText: "차량 등록하기",
      link: "/vehicles/register",
      bgClass: "banner-vehicle",
      badge: "VEHICLE",
    },
    {
      title: "AI 충전 비서로\n가까운 충전소 찾기",
      desc: "대표 차량과 저장 위치를 기준으로 주변 충전소와 예상 충전 시간을 추천합니다.",
      buttonText: "AI 비서 이용하기",
      link: "/ai-chat",
      bgClass: "banner-ai",
      badge: "AI ASSISTANT",
    },
    {
      title: "충전 불편사항은\n민원 접수로 빠르게",
      desc: "충전 불가, 결제 오류, 커넥터 파손 등 이용 불편사항을 접수할 수 있습니다.",
      buttonText: "민원 접수하기",
      link: "/complaint",
      bgClass: "banner-complaint",
      badge: "CUSTOMER CENTER",
    },
  ];

  return (
    <div className="banner-slider">
      <Swiper
        modules={[Autoplay, Pagination, Navigation]}
        slidesPerView={1}
        loop={true}
        navigation={true}
        pagination={{
          clickable: true,
        }}
        autoplay={{
          delay: 4500,
          disableOnInteraction: false,
        }}
        className="main-swiper"
      >
        {banners.map((banner, index) => (
          <SwiperSlide key={index}>
            <div className={`banner-slide ${banner.bgClass}`}>
              <div className="banner-content">
                <span className="banner-badge">{banner.badge}</span>

                <h2>
                  {banner.title.split("\n").map((line, lineIndex) => (
                    <span key={lineIndex}>
                      {line}
                      <br />
                    </span>
                  ))}
                </h2>

                <p>{banner.desc}</p>

                <Link to={banner.link} className="banner-button">
                  {banner.buttonText}
                </Link>
              </div>

              <div className="banner-visual">
                <div className="banner-device">
                  <span>EV</span>
                </div>
              </div>
            </div>
          </SwiperSlide>
        ))}
      </Swiper>
    </div>
  );
};

export default BannerSlider;