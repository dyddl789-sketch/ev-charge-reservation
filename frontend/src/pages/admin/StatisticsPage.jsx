import { Navigate } from "react-router-dom";

const StatisticsPage = () => {
  console.log("StatisticsPage 렌더링 - 이용 통계로 이동");

  return <Navigate to="/admin/statistics/usage" replace />;
};

export default StatisticsPage;
