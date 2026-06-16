import BannerSlider from "./BannerSlider";
import QuickService from "./QuickService";

const MainHeroSection = () => {
  console.log("MainHeroSection 렌더링");

  return (
    <section className="main-hero-section">
      <div className="main-hero-inner">
        {/* 왼쪽 배너 슬라이더 */}
        <BannerSlider />

        {/* 오른쪽 자주 찾는 서비스 */}
        <QuickService />
      </div>
    </section>
  );
};

export default MainHeroSection;