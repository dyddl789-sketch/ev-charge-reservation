import { BrowserRouter, Routes, Route } from "react-router-dom";

import PublicLayout from "../layouts/PublicLayout";
import MainPage from "../pages/public/MainPage";

const AppRoutes = () => {
  console.log("AppRoutes 렌더링");

  return (
    <BrowserRouter>
      <Routes>
        {/* 사용자 서비스 영역 */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<MainPage />} />
          <Route path="/main" element={<MainPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;