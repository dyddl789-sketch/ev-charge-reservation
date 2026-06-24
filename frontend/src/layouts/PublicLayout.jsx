import { Outlet } from "react-router-dom";

import Header from "../components/common/Header";
import Footer from "../components/common/Footer";

const PublicLayout = () => {
  console.log("PublicLayout 렌더링");

  return (
    <div className="public-layout">
      <Header />

      <main className="public-main">
        <Outlet />
      </main>

      <Footer />
    </div>
  );
};

export default PublicLayout;