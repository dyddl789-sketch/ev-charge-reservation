import MainHeroSection from "../../components/main/MainHeroSection";
import NoticeSection from "../../components/main/NoticeSection";

const MainPage = () => {
  console.log("MainPage 렌더링");

  return (
    <div className="main-page">
      {/* 메인 배너 + 자주 찾는 서비스 */}
      <MainHeroSection />

      {/* 새소식 */}
      <NoticeSection />
    </div>
  );
};

export default MainPage;