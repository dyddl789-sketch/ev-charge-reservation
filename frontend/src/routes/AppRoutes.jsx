import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import PublicLayout from "../layouts/PublicLayout";
import AdminLayout from "../layouts/AdminLayout";

import MainPage from "../pages/public/MainPage";
import LoginPage from "../pages/public/LoginPage";
import JoinPage from "../pages/public/JoinPage";
import MyPage from "../pages/public/MyPage";
import VehiclePage from "../pages/public/VehiclePage";
import VehicleRegisterPage from "../pages/public/VehicleRegisterPage";
import StationPage from "../pages/public/StationPage";
import StationDetailPage from "../pages/public/StationDetailPage";
import StationMapPage from "../pages/public/StationMapPage";
import ReservationPage from "../pages/public/ReservationPage";
import ReservationCompletePage from "../pages/public/ReservationCompletePage";
import MyReservationPage from "../pages/public/MyReservationPage";
import ChargingHistoryPage from "../pages/public/ChargingHistoryPage";
import AiChatPage from "../pages/public/AiChatPage";
import CustomerCenterPage from "../pages/public/CustomerCenterPage";
import ComplaintCreatePage from "../pages/public/ComplaintCreatePage";
import MyComplaintPage from "../pages/public/MyComplaintPage";
import NoticePage from "../pages/public/NoticePage";
import NotFoundPage from "../pages/public/NotFoundPage";

import DashboardPage from "../pages/admin/DashboardPage";
import MemberPage from "../pages/admin/MemberPage";
import MemberDetailPage from "../pages/admin/MemberDetailPage";
import EmployeePage from "../pages/admin/EmployeePage";
import InfrastructurePage from "../pages/admin/InfrastructurePage";
import AdminStationPage from "../pages/admin/AdminStationPage";
import AdminChargerPage from "../pages/admin/AdminChargerPage";
import AdminReservationPage from "../pages/admin/AdminReservationPage";
import AdminReservationDetailPage from "../pages/admin/AdminReservationDetailPage";
import ComplaintPage from "../pages/admin/ComplaintPage";
import FaultPage from "../pages/admin/FaultPage";
import InspectionPage from "../pages/admin/InspectionPage";
import ApprovalPage from "../pages/admin/ApprovalPage";
import StatisticsPage from "../pages/admin/StatisticsPage";
import SalesStatPage from "../pages/admin/SalesStatPage";
import UsageStatPage from "../pages/admin/UsageStatPage";
import SystemPage from "../pages/admin/SystemPage";

const AppRoutes = () => {
  console.log("AppRoutes 렌더링");

  return (
    <BrowserRouter>
      <Routes>
        {/* 사용자 서비스 영역 */}
        <Route element={<PublicLayout />}>
          <Route path="/" element={<MainPage />} />
          <Route path="/main" element={<MainPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/join" element={<JoinPage />} />
          <Route path="/mypage" element={<MyPage />} />
          <Route path="/vehicles" element={<VehiclePage />} />
          <Route path="/vehicles/register" element={<VehicleRegisterPage />} />
          <Route path="/stations" element={<StationPage />} />
          <Route path="/stations/map" element={<StationMapPage />} />
          <Route path="/stations/:stationId" element={<StationDetailPage />} />
          <Route path="/reservation" element={<ReservationPage />} />
          <Route path="/reservation/complete" element={<ReservationCompletePage />} />
          <Route path="/my-reservations" element={<MyReservationPage />} />
          <Route path="/charging-history" element={<ChargingHistoryPage />} />
          <Route path="/ai-chat" element={<AiChatPage />} />
          <Route path="/customer-center" element={<CustomerCenterPage />} />
          <Route path="/complaint" element={<ComplaintCreatePage />} />
          <Route path="/complaints/my" element={<MyComplaintPage />} />
          <Route path="/notice" element={<NoticePage />} />
        </Route>

        {/* 운영기관 MIS 영역 */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="members" element={<MemberPage />} />
          <Route path="members/:memberId" element={<MemberDetailPage />} />
          <Route path="employees" element={<EmployeePage />} />
          <Route path="infrastructure" element={<InfrastructurePage />} />
          <Route path="stations" element={<AdminStationPage />} />
          <Route path="chargers" element={<AdminChargerPage />} />
          <Route path="reservations" element={<AdminReservationPage />} />
          <Route path="reservations/:reservationId" element={<AdminReservationDetailPage />} />
          <Route path="complaints" element={<ComplaintPage />} />
          <Route path="faults" element={<FaultPage />} />
          <Route path="inspections" element={<InspectionPage />} />
          <Route path="approvals" element={<ApprovalPage />} />
          <Route path="statistics" element={<StatisticsPage />} />
          <Route path="statistics/usage" element={<UsageStatPage />} />
          <Route path="statistics/sales" element={<SalesStatPage />} />
          <Route path="system" element={<SystemPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
};

export default AppRoutes;
