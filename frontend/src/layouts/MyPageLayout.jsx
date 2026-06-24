import { Outlet } from "react-router-dom";
import MyPageSidebar from "../components/mypage/MyPageSidebar";
import "../styles/mypage.css";

const MyPageLayout = () => {
  console.log("MyPageLayout 렌더링");

  return (
    <section className="mypage-layout-section">
      <div className="mypage-layout-inner">
        <MyPageSidebar />

        <main className="mypage-content">
          <Outlet />
        </main>
      </div>
    </section>
  );
};

export default MyPageLayout;
