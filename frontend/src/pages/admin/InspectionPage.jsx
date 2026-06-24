import { Navigate } from "react-router-dom";

const InspectionPage = () => {
  console.log("InspectionPage 렌더링 - 장애·점검관리로 이동");

  return <Navigate to="/admin/faults" replace />;
};

export default InspectionPage;
