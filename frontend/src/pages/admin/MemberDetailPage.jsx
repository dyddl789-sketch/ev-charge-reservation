import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

const getBadgeColor = (value) => {
  if (value === "ACTIVE") return "green";
  if (value === "BLOCKED") return "danger";
  if (value === "INACTIVE") return "warning";
  if (value === "ADMIN" || value === "MANAGER") return "purple";
  if (value === "OPERATOR" || value === "ENGINEER") return "blue";
  return "blue";
};

const formatDateTime = (value) => {
  if (!value) return "-";
  if (typeof value === "string") return value.replace("T", " ").slice(0, 16);
  return "-";
};

const numberText = (value) => Number(value || 0).toLocaleString("ko-KR");
const moneyText = (value) => `${numberText(value)}원`;

const normalizeDetail = (source = {}) => ({
  memberId: source.memberId,
  userId: source.userId ?? "-",
  memberName: source.memberName ?? "-",
  nickname: source.nickname ?? "-",
  phone: source.phone ?? "-",
  email: source.email ?? "-",
  userType: source.userType ?? "USER",
  loginType: source.loginType ?? "-",
  status: source.status ?? "ACTIVE",
  createdAtText: source.createdAtText ?? formatDateTime(source.createdAt),
  lastLoginAtText: source.lastLoginAtText ?? formatDateTime(source.lastLoginAt),
  updatedAtText: source.updatedAtText ?? formatDateTime(source.updatedAt),
  vehicleCount: source.vehicleCount ?? 0,
  reservationCount: source.reservationCount ?? 0,
  completedSessionCount: source.completedSessionCount ?? 0,
  totalPaymentAmount: source.totalPaymentAmount ?? 0,
});

const MemberDetailPage = () => {
  console.log("MemberDetailPage 렌더링");

  const { memberId } = useParams();
  const navigate = useNavigate();

  const [detail, setDetail] = useState(null);
  const [vehicleList, setVehicleList] = useState([]);
  const [reservationList, setReservationList] = useState([]);
  const [chargingList, setChargingList] = useState([]);
  const [paymentList, setPaymentList] = useState([]);
  const [activeTab, setActiveTab] = useState("vehicles");
  const [loading, setLoading] = useState(true);

  const loadDetail = async () => {
    console.log("회원 상세 실제 조회", memberId);
    setLoading(true);

    try {
      const response = await adminApi.member(memberId);
      console.log("회원 상세 응답", response.data);

      const data = response.data || {};
      setDetail(normalizeDetail(data.memberDetail));
      setVehicleList(data.vehicleList || []);
      setReservationList(data.reservationList || []);
      setChargingList(data.chargingList || []);
      setPaymentList(data.paymentList || []);
    } catch (error) {
      console.log("회원 상세 조회 실패", error);
      alert("회원 상세 정보를 불러오지 못했습니다.");
      setDetail(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [memberId]);

  const summaryCards = useMemo(() => {
    const member = detail || {};
    return [
      { label: "등록 차량", value: `${numberText(member.vehicleCount)}대` },
      { label: "예약 건수", value: `${numberText(member.reservationCount)}건` },
      { label: "충전 완료", value: `${numberText(member.completedSessionCount)}건` },
      { label: "총 이용 금액", value: moneyText(member.totalPaymentAmount) },
    ];
  }, [detail]);

  if (loading) {
    return <section className="admin-page"><div className="admin-panel">회원 상세 정보를 불러오는 중입니다.</div></section>;
  }

  if (!detail) {
    return (
      <section className="admin-page">
        <div className="admin-panel">
          <p className="admin-empty-text">회원 정보를 찾을 수 없습니다.</p>
          <button type="button" onClick={() => navigate("/admin/members")}>목록으로</button>
        </div>
      </section>
    );
  }

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>회원관리</p>
          <h1>회원 상세보기</h1>
          <span>회원 기본 정보, 차량, 예약, 충전 완료, 결제 성격의 이용 금액을 실제 DB 기준으로 확인합니다.</span>
        </div>
        <button type="button" onClick={() => navigate("/admin/members")}>목록으로</button>
      </div>

      <div className="admin-member-profile admin-panel">
        <div className="admin-member-profile-head">
          <div className="admin-member-avatar">{detail.memberName?.slice(0, 1) || "?"}</div>
          <div>
            <h2>{detail.memberName}</h2>
            <p>{detail.userId}</p>
          </div>
          <em className={`admin-badge ${getBadgeColor(detail.status)}`}>{detail.status}</em>
        </div>
        <div className="admin-info-grid">
          <article><span>회원 번호</span><strong>{detail.memberId}</strong></article>
          <article><span>권한</span><strong>{detail.userType}</strong></article>
          <article><span>로그인 타입</span><strong>{detail.loginType}</strong></article>
          <article><span>닉네임</span><strong>{detail.nickname}</strong></article>
          <article><span>이메일</span><strong>{detail.email}</strong></article>
          <article><span>연락처</span><strong>{detail.phone}</strong></article>
          <article><span>가입일</span><strong>{detail.createdAtText}</strong></article>
          <article><span>마지막 로그인</span><strong>{detail.lastLoginAtText}</strong></article>
        </div>
      </div>

      <div className="admin-kpi-grid four">
        {summaryCards.map((card) => (
          <article className="admin-kpi-card" key={card.label}><span>{card.label}</span><strong>{card.value}</strong></article>
        ))}
      </div>

      <div className="admin-panel">
        <div className="admin-tab-row">
          <button type="button" className={activeTab === "vehicles" ? "active" : ""} onClick={() => setActiveTab("vehicles")}>등록 차량</button>
          <button type="button" className={activeTab === "reservations" ? "active" : ""} onClick={() => setActiveTab("reservations")}>예약 내역</button>
          <button type="button" className={activeTab === "charging" ? "active" : ""} onClick={() => setActiveTab("charging")}>충전 완료</button>
          <button type="button" className={activeTab === "payments" ? "active" : ""} onClick={() => setActiveTab("payments")}>이용 금액</button>
        </div>

        {activeTab === "vehicles" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table">
              <thead><tr><th>차량번호</th><th>별칭</th><th>모델</th><th>차량번호판</th><th>대표</th><th>등록일</th></tr></thead>
              <tbody>
                {vehicleList.map((item) => (
                  <tr key={item.vehicleId}><td>{item.vehicleId}</td><td>{item.vehicleNickname || "-"}</td><td>{item.manufacturer} {item.modelName}</td><td>{item.plateNumber || "-"}</td><td>{item.isDefault ? "대표" : "-"}</td><td>{item.createdAtText || formatDateTime(item.createdAt)}</td></tr>
                ))}
                {vehicleList.length === 0 && <tr><td colSpan="6">등록 차량이 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "reservations" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table">
              <thead><tr><th>예약번호</th><th>충전소</th><th>충전기</th><th>차량</th><th>예약시간</th><th>상태</th><th>예상금액</th></tr></thead>
              <tbody>
                {reservationList.map((item) => (
                  <tr key={item.reservationId}><td>RSV-{String(item.reservationId).padStart(6, "0")}</td><td>{item.stationName}</td><td>{item.chargerName}</td><td>{item.vehicleNickname || item.modelName}</td><td>{item.startTimeText || formatDateTime(item.startTime)}</td><td>{item.status}</td><td>{moneyText(item.estimatedCost)}</td></tr>
                ))}
                {reservationList.length === 0 && <tr><td colSpan="7">예약 내역이 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "charging" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table">
              <thead><tr><th>세션번호</th><th>충전소</th><th>충전기</th><th>차량</th><th>시작</th><th>종료</th><th>충전량</th><th>금액</th></tr></thead>
              <tbody>
                {chargingList.map((item) => (
                  <tr key={item.sessionId}><td>{item.sessionId}</td><td>{item.stationName}</td><td>{item.chargerName}</td><td>{item.vehicleNickname || item.modelName}</td><td>{item.actualStartTimeText || formatDateTime(item.actualStartTime)}</td><td>{item.actualEndTimeText || formatDateTime(item.actualEndTime)}</td><td>{numberText(item.actualKwh)}kWh</td><td>{moneyText(item.actualCost)}</td></tr>
                ))}
                {chargingList.length === 0 && <tr><td colSpan="8">충전 완료 내역이 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "payments" && (
          <div className="admin-table-wrap">
            <table className="admin-table elegant-table">
              <thead><tr><th>세션번호</th><th>예약번호</th><th>충전소</th><th>충전량</th><th>이용금액</th><th>완료일시</th><th>상태</th></tr></thead>
              <tbody>
                {paymentList.map((item) => (
                  <tr key={item.sessionId}><td>{item.sessionId}</td><td>RSV-{String(item.reservationId).padStart(6, "0")}</td><td>{item.stationName}</td><td>{numberText(item.actualKwh)}kWh</td><td>{moneyText(item.actualCost)}</td><td>{item.paidAtText || formatDateTime(item.paidAt)}</td><td>{item.status}</td></tr>
                ))}
                {paymentList.length === 0 && <tr><td colSpan="7">이용 금액 내역이 없습니다.</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default MemberDetailPage;
