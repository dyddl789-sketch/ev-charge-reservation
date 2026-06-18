import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

// 백엔드 REST API 연결 전 상세 화면 확인용 Mock 데이터
const mockDetailMap = {
  85: {
    memberDetail: {
      memberId: 85,
      userId: "demo_user_30",
      memberName: "시연회원30",
      nickname: "데모회원30",
      phone: "010-8830-1030",
      email: "demo_user_30@evcharge.test",
      userType: "USER",
      loginType: "KAKAO",
      status: "ACTIVE",
      createdAtText: "2026-06-04",
      lastLoginAtText: "2026-06-02 08:37",
      vehicleCount: 1,
      reservationCount: 0,
      completedSessionCount: 0,
      totalPaymentAmount: 0,
    },
    vehicleList: [
      {
        vehicleId: 125,
        vehicleNickname: "업무용30",
        modelName: "벤츠 EQS",
        plateNumber: "50가2030",
        isDefault: true,
        createdAtText: "2026-05-20",
      },
    ],
    reservationList: [],
    chargingList: [],
    paymentList: [],
  },
  1: {
    memberDetail: {
      memberId: 1,
      userId: "kakao_4916296150",
      memberName: "김성민",
      nickname: "김성민",
      phone: "010-0000-0000",
      email: "dyddl456@nate.com",
      userType: "USER",
      loginType: "KAKAO",
      status: "ACTIVE",
      createdAtText: "2026-05-20",
      lastLoginAtText: "2026-06-18 09:20",
      vehicleCount: 2,
      reservationCount: 12,
      completedSessionCount: 5,
      totalPaymentAmount: 72400,
    },
    vehicleList: [
      {
        vehicleId: 101,
        vehicleNickname: "출퇴근용 아이오닉",
        modelName: "아이오닉5",
        plateNumber: "12가3456",
        isDefault: true,
        createdAtText: "2026-05-15",
      },
      {
        vehicleId: 102,
        vehicleNickname: "주말용 EV6",
        modelName: "EV6",
        plateNumber: "34나7890",
        isDefault: false,
        createdAtText: "2026-05-21",
      },
    ],
    reservationList: [
      {
        reservationId: 101,
        stationName: "부산시청 공공충전소",
        chargerName: "급속 01",
        vehicleName: "출퇴근용 아이오닉",
        startTimeText: "2026-06-18 14:00",
        estimatedCost: 12800,
        status: "예약완료",
      },
    ],
    chargingList: [
      {
        sessionId: 201,
        stationName: "부산역 공공충전소",
        chargerName: "급속 03",
        vehicleName: "출퇴근용 아이오닉",
        actualStartTimeText: "2026-06-10 09:00",
        actualKwh: 32.4,
        actualMinutes: 42,
        actualCost: 12100,
        status: "완료",
      },
    ],
    paymentList: [
      {
        paymentId: 301,
        paymentDateText: "2026-06-10",
        paymentMethod: "카드",
        amount: 12100,
        status: "결제완료",
      },
    ],
  },
};

const defaultDetail = (memberId) => ({
  memberDetail: {
    memberId,
    userId: `member_${memberId}`,
    memberName: `회원${memberId}`,
    nickname: `회원${memberId}`,
    phone: "-",
    email: `member_${memberId}@evcharge.test`,
    userType: "USER",
    loginType: "LOCAL",
    status: "ACTIVE",
    createdAtText: "-",
    lastLoginAtText: "-",
    vehicleCount: 0,
    reservationCount: 0,
    completedSessionCount: 0,
    totalPaymentAmount: 0,
  },
  vehicleList: [],
  reservationList: [],
  chargingList: [],
  paymentList: [],
});

const numberText = (value) => Number(value ?? 0).toLocaleString("ko-KR");

const getInitial = (name) => {
  if (!name) return "?";
  return name.substring(0, 1);
};

const getBadgeColor = (value) => {
  if (value === "ACTIVE" || value === "완료" || value === "결제완료") return "green";
  if (value === "BLOCKED" || value === "취소" || value === "반려") return "danger";
  if (value === "INACTIVE" || value === "노쇼" || value === "예약완료") return "warning";
  if (value === "ADMIN" || value === "MANAGER") return "purple";
  if (value === "OPERATOR" || value === "ENGINEER") return "blue";
  return "blue";
};

const normalizeDetail = (data, memberId) => {
  const source = data?.memberDetail ? data : data?.data?.memberDetail ? data.data : data;

  if (!source || typeof source === "string") {
    return defaultDetail(memberId);
  }

  const detail = source.memberDetail ?? source.detail ?? source.member ?? source;

  return {
    memberDetail: {
      memberId: detail.memberId ?? memberId,
      userId: detail.userId ?? "-",
      memberName: detail.memberName ?? detail.name ?? "-",
      nickname: detail.nickname ?? "-",
      phone: detail.phone ?? "-",
      email: detail.email ?? "-",
      userType: detail.userType ?? detail.type ?? "USER",
      loginType: detail.loginType ?? "-",
      status: detail.status ?? "ACTIVE",
      createdAtText: detail.createdAtText ?? detail.createdAt ?? "-",
      lastLoginAtText: detail.lastLoginAtText ?? detail.lastLoginAt ?? "-",
      vehicleCount: detail.vehicleCount ?? 0,
      reservationCount: detail.reservationCount ?? 0,
      completedSessionCount: detail.completedSessionCount ?? 0,
      totalPaymentAmount: detail.totalPaymentAmount ?? 0,
    },
    vehicleList: source.vehicleList ?? source.vehicles ?? [],
    reservationList: source.reservationList ?? source.reservations ?? [],
    chargingList: source.chargingList ?? source.chargingSessions ?? source.histories ?? [],
    paymentList: source.paymentList ?? source.payments ?? [],
  };
};

const MemberDetailPage = () => {
  console.log("MemberDetailPage 렌더링");

  const { memberId } = useParams();
  const navigate = useNavigate();

  const numericMemberId = Number(memberId);

  const [detailData, setDetailData] = useState(defaultDetail(numericMemberId));
  const [activeTab, setActiveTab] = useState("vehicles");

  const member = detailData.memberDetail;

  const loadMemberDetail = async () => {
    console.log("회원 상세 조회 실행", numericMemberId);

    try {
      const response = await adminApi.member(numericMemberId);
      console.log("회원 상세 응답", response.data);

      // 기존 JSP Controller는 HTML을 반환할 수 있으므로 JSON일 때만 화면 데이터로 사용
      const normalized = normalizeDetail(response.data, numericMemberId);

      if (normalized.memberDetail?.memberId) {
        setDetailData(normalized);
        return;
      }

      console.log("회원 상세 JSON 데이터 없음 - Mock 데이터 사용");
      setDetailData(mockDetailMap[numericMemberId] ?? defaultDetail(numericMemberId));
    } catch (error) {
      console.log("회원 상세 API 미연결 또는 오류 - Mock 데이터 사용", error);
      setDetailData(mockDetailMap[numericMemberId] ?? defaultDetail(numericMemberId));
    }
  };

  useEffect(() => {
    loadMemberDetail();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [memberId]);

  const summaryCards = useMemo(() => [
    { label: "등록 차량 수", value: `${numberText(member.vehicleCount)}대`, color: "blue" },
    { label: "예약 건수", value: `${numberText(member.reservationCount)}건`, color: "warning" },
    { label: "충전 완료 건수", value: `${numberText(member.completedSessionCount)}건`, color: "green" },
    { label: "총 결제 금액", value: `${numberText(member.totalPaymentAmount)}원`, color: "purple" },
  ], [member]);

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>회원관리</p>
          <h1>회원 상세보기</h1>
          <span>회원의 기본 정보와 이용 현황을 확인합니다.</span>
        </div>
        <button type="button" onClick={() => navigate("/admin/members")}>목록으로</button>
      </div>

      <div className="admin-member-profile admin-panel">
        <div className="admin-member-profile-head">
          <div className="admin-member-avatar">
            {getInitial(member.memberName)}
          </div>
          <div>
            <h2>{member.memberName}</h2>
            <p>{member.userId}</p>
          </div>
          <em className={`admin-badge ${getBadgeColor(member.status)}`}>
            {member.status}
          </em>
        </div>

        <div className="admin-info-grid">
          <article>
            <span>회원 번호</span>
            <strong>{member.memberId}</strong>
          </article>
          <article>
            <span>회원 구분</span>
            <strong>{member.userType}</strong>
          </article>
          <article>
            <span>로그인 타입</span>
            <strong>{member.loginType}</strong>
          </article>
          <article>
            <span>닉네임</span>
            <strong>{member.nickname}</strong>
          </article>
          <article>
            <span>이메일</span>
            <strong>{member.email}</strong>
          </article>
          <article>
            <span>연락처</span>
            <strong>{member.phone}</strong>
          </article>
          <article>
            <span>가입일</span>
            <strong>{member.createdAtText}</strong>
          </article>
          <article>
            <span>마지막 로그인</span>
            <strong>{member.lastLoginAtText}</strong>
          </article>
        </div>
      </div>

      <h2 className="admin-section-title">이용 현황 요약</h2>
      <div className="admin-kpi-grid four">
        {summaryCards.map((card) => (
          <article className="admin-kpi-card" key={card.label}>
            <span>{card.label}</span>
            <strong>{card.value}</strong>
          </article>
        ))}
      </div>

      <div className="admin-panel">
        <div className="admin-tab-row">
          <button
            type="button"
            className={activeTab === "vehicles" ? "active" : ""}
            onClick={() => setActiveTab("vehicles")}
          >
            등록 차량
          </button>
          <button
            type="button"
            className={activeTab === "reservations" ? "active" : ""}
            onClick={() => setActiveTab("reservations")}
          >
            예약 내역
          </button>
          <button
            type="button"
            className={activeTab === "charging" ? "active" : ""}
            onClick={() => setActiveTab("charging")}
          >
            충전 내역
          </button>
          <button
            type="button"
            className={activeTab === "payments" ? "active" : ""}
            onClick={() => setActiveTab("payments")}
          >
            결제 내역
          </button>
        </div>

        {activeTab === "vehicles" && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>번호</th>
                  <th>차량 별칭</th>
                  <th>차량 모델</th>
                  <th>차량 번호</th>
                  <th>기본 차량</th>
                  <th>등록일</th>
                </tr>
              </thead>
              <tbody>
                {detailData.vehicleList.map((vehicle) => (
                  <tr key={vehicle.vehicleId ?? vehicle.id}>
                    <td>{vehicle.vehicleId ?? vehicle.id}</td>
                    <td>{vehicle.vehicleNickname ?? vehicle.nickname ?? "-"}</td>
                    <td>{vehicle.modelName ?? vehicle.vehicleModel ?? "-"}</td>
                    <td>{vehicle.plateNumber ?? "-"}</td>
                    <td>
                      {vehicle.isDefault ? <em className="admin-badge green">대표</em> : "-"}
                    </td>
                    <td>{vehicle.createdAtText ?? vehicle.createdAt ?? "-"}</td>
                  </tr>
                ))}
                {detailData.vehicleList.length === 0 && (
                  <tr><td colSpan="6">등록 차량이 없습니다.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "reservations" && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>예약번호</th>
                  <th>충전소</th>
                  <th>충전기</th>
                  <th>차량</th>
                  <th>예약시간</th>
                  <th>예상금액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {detailData.reservationList.map((reservation) => (
                  <tr key={reservation.reservationId ?? reservation.id}>
                    <td>{reservation.reservationId ?? reservation.id}</td>
                    <td>{reservation.stationName ?? "-"}</td>
                    <td>{reservation.chargerName ?? "-"}</td>
                    <td>{reservation.vehicleName ?? reservation.vehicleNickname ?? "-"}</td>
                    <td>{reservation.startTimeText ?? reservation.reservationTime ?? "-"}</td>
                    <td>{numberText(reservation.estimatedCost)}원</td>
                    <td>
                      <em className={`admin-badge ${getBadgeColor(reservation.status)}`}>
                        {reservation.status ?? "-"}
                      </em>
                    </td>
                  </tr>
                ))}
                {detailData.reservationList.length === 0 && (
                  <tr><td colSpan="7">예약 내역이 없습니다.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "charging" && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>이용번호</th>
                  <th>충전소</th>
                  <th>충전기</th>
                  <th>차량</th>
                  <th>시작시간</th>
                  <th>충전량</th>
                  <th>충전시간</th>
                  <th>금액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {detailData.chargingList.map((charging) => (
                  <tr key={charging.sessionId ?? charging.id}>
                    <td>{charging.sessionId ?? charging.id}</td>
                    <td>{charging.stationName ?? "-"}</td>
                    <td>{charging.chargerName ?? "-"}</td>
                    <td>{charging.vehicleName ?? charging.vehicleNickname ?? "-"}</td>
                    <td>{charging.actualStartTimeText ?? charging.startTimeText ?? "-"}</td>
                    <td>{charging.actualKwh ?? 0}kWh</td>
                    <td>{charging.actualMinutes ?? 0}분</td>
                    <td>{numberText(charging.actualCost)}원</td>
                    <td>
                      <em className={`admin-badge ${getBadgeColor(charging.status)}`}>
                        {charging.status ?? "-"}
                      </em>
                    </td>
                  </tr>
                ))}
                {detailData.chargingList.length === 0 && (
                  <tr><td colSpan="9">충전 내역이 없습니다.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === "payments" && (
          <div className="admin-table-wrap">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>결제번호</th>
                  <th>결제일</th>
                  <th>결제수단</th>
                  <th>금액</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {detailData.paymentList.map((payment) => (
                  <tr key={payment.paymentId ?? payment.id}>
                    <td>{payment.paymentId ?? payment.id}</td>
                    <td>{payment.paymentDateText ?? payment.createdAtText ?? "-"}</td>
                    <td>{payment.paymentMethod ?? "-"}</td>
                    <td>{numberText(payment.amount ?? payment.paymentAmount)}원</td>
                    <td>
                      <em className={`admin-badge ${getBadgeColor(payment.status)}`}>
                        {payment.status ?? "-"}
                      </em>
                    </td>
                  </tr>
                ))}
                {detailData.paymentList.length === 0 && (
                  <tr><td colSpan="5">결제 내역이 없습니다.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default MemberDetailPage;
