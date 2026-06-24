import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as adminApi from "../../apis/adminApi";

const mockReservations = {
  "RSV-000580": {
    reservationId: 580,
    reservationNo: "RSV-000580",
    memberName: "시연회원14",
    memberId: 14,
    phone: "010-1200-0014",
    email: "demo_user_14@evcharge.test",
    vehicleName: "BMW i4",
    plateNumber: "50가2030",
    stationName: "테스트 해운대 충전소",
    stationAddress: "부산광역시 해운대구 센텀중앙로 55",
    chargerName: "해운대 완속 3번",
    chargerType: "완속",
    connectorType: "AC3상",
    startTime: "2026-06-05 11:37",
    endTime: "2026-06-05 16:43",
    currentSoc: 48,
    targetSoc: 85,
    estimatedMinutes: 306,
    estimatedCost: 8692,
    authCode: "ADM_R_00028",
    status: "노쇼",
    createdAt: "2026-06-05 11:20",
    updatedAt: "2026-06-05 12:00",
    memo: "예약 시작 시간이 지났으나 인증되지 않아 노쇼 처리된 건입니다.",
  },
  "RSV-000629": {
    reservationId: 629,
    reservationNo: "RSV-000629",
    memberName: "김성민",
    memberId: 1,
    phone: "010-8830-1030",
    email: "dyddl456@nate.com",
    vehicleName: "BMW i4",
    plateNumber: "50가2030",
    stationName: "테스트 기장 충전소",
    stationAddress: "부산광역시 기장군 기장읍 차성로 12",
    chargerName: "기장 급속 2번",
    chargerType: "급속",
    connectorType: "DC콤보",
    startTime: "2026-06-05 11:25",
    endTime: "2026-06-05 11:27",
    currentSoc: 30,
    targetSoc: 32,
    estimatedMinutes: 2,
    estimatedCost: 583,
    authCode: "-",
    status: "노쇼",
    createdAt: "2026-06-05 11:10",
    updatedAt: "2026-06-05 11:40",
    memo: "예약 인증 코드 미발급 상태입니다.",
  },
  "RSV-000628": {
    reservationId: 628,
    reservationNo: "RSV-000628",
    memberName: "김성민",
    memberId: 1,
    phone: "010-8830-1030",
    email: "dyddl456@nate.com",
    vehicleName: "BMW i4",
    plateNumber: "50가2030",
    stationName: "테스트 기장 충전소",
    stationAddress: "부산광역시 기장군 기장읍 차성로 12",
    chargerName: "기장 급속 2번",
    chargerType: "급속",
    connectorType: "DC콤보",
    startTime: "2026-06-05 11:20",
    endTime: "2026-06-05 11:22",
    currentSoc: 30,
    targetSoc: 33,
    estimatedMinutes: 2,
    estimatedCost: 875,
    authCode: "-",
    status: "완료",
    createdAt: "2026-06-05 11:03",
    updatedAt: "2026-06-05 11:23",
    memo: "정상 완료된 예약입니다.",
  },
  "RSV-000579": {
    reservationId: 579,
    reservationNo: "RSV-000579",
    memberName: "시연회원9",
    memberId: 9,
    phone: "010-1200-0009",
    email: "demo_user_9@evcharge.test",
    vehicleName: "BMW i4",
    plateNumber: "41나1009",
    stationName: "테스트 해운대 충전소",
    stationAddress: "부산광역시 해운대구 센텀중앙로 55",
    chargerName: "해운대 급속 2번",
    chargerType: "급속",
    connectorType: "DC콤보",
    startTime: "2026-06-05 10:52",
    endTime: "2026-06-05 11:14",
    currentSoc: 47,
    targetSoc: 85,
    estimatedMinutes: 22,
    estimatedCost: 11069,
    authCode: "ADM_R_00027",
    status: "노쇼",
    createdAt: "2026-06-05 10:30",
    updatedAt: "2026-06-05 11:20",
    memo: "예약 인증 지연으로 확인이 필요한 건입니다.",
  },
};

const statusClassMap = {
  예약완료: "orange",
  인증완료: "blue",
  충전중: "blue",
  완료: "green",
  취소: "gray",
  노쇼: "purple",
};

const timelineSteps = [
  { key: "created", label: "예약 생성" },
  { key: "verified", label: "인증 확인" },
  { key: "charging", label: "충전 진행" },
  { key: "completed", label: "완료/종료" },
];

const toNumberId = (reservationId) => {
  if (!reservationId) return "";
  return String(reservationId).replace("RSV-", "").replace(/^0+/, "") || reservationId;
};

const formatMoney = (value) => {
  const numberValue = Number(value || 0);
  return numberValue.toLocaleString("ko-KR") + "원";
};

const normalizeReservation = (data, reservationId) => {
  if (!data || typeof data !== "object") {
    return null;
  }

  const source = data.reservation || data.detail || data.data || data;

  return {
    reservationId: source.reservationId || toNumberId(reservationId),
    reservationNo: source.reservationNo || `RSV-${String(source.reservationId || toNumberId(reservationId)).padStart(6, "0")}`,
    memberName: source.memberName || source.member_name || "-",
    memberId: source.memberId || source.member_id || "-",
    phone: source.phone || "-",
    email: source.email || "-",
    vehicleName: source.vehicleName || source.vehicle_name || "-",
    plateNumber: source.plateNumber || source.plate_number || "-",
    stationName: source.stationName || source.station_name || "-",
    stationAddress: source.stationAddress || source.station_address || source.address || "-",
    chargerName: source.chargerName || source.charger_name || "-",
    chargerType: source.chargerType || source.charger_type || "-",
    connectorType: source.connectorType || source.connector_type || "-",
    startTime: source.startTime || source.start_time || "-",
    endTime: source.endTime || source.end_time || "-",
    currentSoc: source.currentSoc ?? source.current_soc ?? "-",
    targetSoc: source.targetSoc ?? source.target_soc ?? "-",
    estimatedMinutes: source.estimatedMinutes ?? source.estimated_minutes ?? "-",
    estimatedCost: source.estimatedCost ?? source.estimated_cost ?? 0,
    authCode: source.authCode || source.auth_code || "-",
    status: source.status || "예약완료",
    createdAt: source.createdAt || source.created_at || "-",
    updatedAt: source.updatedAt || source.updated_at || "-",
    memo: source.memo || "예약 상세 정보를 확인합니다.",
  };
};

const AdminReservationDetailPage = () => {
  console.log("AdminReservationDetailPage 렌더링");

  const { reservationId } = useParams();
  const navigate = useNavigate();

  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(true);

  const numericReservationId = useMemo(() => toNumberId(reservationId), [reservationId]);

  useEffect(() => {
    console.log("관리자 예약 상세 초기화", reservationId);

    const loadReservation = async () => {
      try {
        setLoading(true);
        const response = await adminApi.reservationDetail(numericReservationId);
        console.log("관리자 예약 상세 응답", response.data);

        const normalized = normalizeReservation(response.data, reservationId);
        setReservation(normalized || mockReservations[reservationId] || mockReservations[`RSV-${String(numericReservationId).padStart(6, "0")}`]);
      } catch (error) {
        console.log("관리자 예약 상세 API 실패 - Mock 데이터 사용", error);
        setReservation(mockReservations[reservationId] || mockReservations[`RSV-${String(numericReservationId).padStart(6, "0")}`] || mockReservations["RSV-000580"]);
      } finally {
        setLoading(false);
      }
    };

    loadReservation();
  }, [numericReservationId, reservationId]);

  const changeStatus = async (type) => {
    console.log("예약 상태 처리 요청", type, reservation?.reservationId);

    if (!reservation) return;

    try {
      if (type === "cancel") {
        await adminApi.cancelReservation(reservation.reservationId);
        setReservation({ ...reservation, status: "취소" });
        alert("예약이 취소 처리되었습니다.");
      }

      if (type === "noshow") {
        await adminApi.noShowReservation(reservation.reservationId);
        setReservation({ ...reservation, status: "노쇼" });
        alert("노쇼 처리되었습니다.");
      }

      if (type === "start") {
        await adminApi.startReservation(reservation.reservationId);
        setReservation({ ...reservation, status: "충전중" });
        alert("충전 시작 처리되었습니다.");
      }
    } catch (error) {
      console.log("예약 상태 처리 API 실패 - 화면 상태만 반영", error);
      if (type === "cancel") setReservation({ ...reservation, status: "취소" });
      if (type === "noshow") setReservation({ ...reservation, status: "노쇼" });
      if (type === "start") setReservation({ ...reservation, status: "충전중" });
      alert("현재 백엔드 응답이 없어 화면에서만 상태를 반영했습니다.");
    }
  };

  if (loading) {
    return (
      <section className="admin-page">
        <div className="admin-panel">
          <p className="admin-empty-text">예약 상세 정보를 불러오는 중입니다.</p>
        </div>
      </section>
    );
  }

  if (!reservation) {
    return (
      <section className="admin-page">
        <div className="admin-panel">
          <p className="admin-empty-text">예약 정보를 찾을 수 없습니다.</p>
          <button type="button" onClick={() => navigate("/admin/reservations")}>목록으로</button>
        </div>
      </section>
    );
  }

  const statusClass = statusClassMap[reservation.status] || "gray";
  const socText = `${reservation.currentSoc}% → ${reservation.targetSoc}%`;

  return (
    <section className="admin-page">
      <div className="admin-page-header">
        <div>
          <p>예약관리</p>
          <h1>예약 상세보기</h1>
          <span>예약 정보, 회원 정보, 충전소 정보와 처리 상태를 확인합니다.</span>
        </div>
        <div className="admin-action-row">
          <button type="button" className="line" onClick={() => navigate("/admin/reservations")}>목록으로</button>
          <button type="button" onClick={() => changeStatus("start")}>충전 시작</button>
          <button type="button" className="warning" onClick={() => changeStatus("noshow")}>노쇼 처리</button>
          <button type="button" className="danger" onClick={() => changeStatus("cancel")}>예약 취소</button>
        </div>
      </div>

      <div className="admin-detail-hero">
        <div>
          <span className="admin-detail-label">예약번호</span>
          <h2>{reservation.reservationNo}</h2>
          <p>{reservation.stationName} · {reservation.chargerName}</p>
        </div>
        <em className={`admin-badge ${statusClass}`}>{reservation.status}</em>
      </div>

      <div className="admin-kpi-grid reservation-detail-kpi">
        <article className="admin-kpi-card">
          <span>예약 회원</span>
          <strong>{reservation.memberName}</strong>
          <p>{reservation.phone}</p>
        </article>
        <article className="admin-kpi-card">
          <span>예약 시간</span>
          <strong>{reservation.startTime}</strong>
          <p>종료 예정 {reservation.endTime}</p>
        </article>
        <article className="admin-kpi-card">
          <span>SOC</span>
          <strong>{socText}</strong>
          <p>예상 {reservation.estimatedMinutes}분</p>
        </article>
        <article className="admin-kpi-card">
          <span>예상 금액</span>
          <strong>{formatMoney(reservation.estimatedCost)}</strong>
          <p>인증코드 {reservation.authCode}</p>
        </article>
      </div>

      <div className="admin-grid admin-grid-2-1">
        <div className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>예약 기본 정보</strong>
              <p>기존 백엔드 상세 API가 JSON으로 전환되면 같은 필드 구조로 바로 연동됩니다.</p>
            </div>
          </div>

          <div className="admin-info-grid">
            <article>
              <span>회원명</span>
              <strong>{reservation.memberName}</strong>
            </article>
            <article>
              <span>회원번호</span>
              <strong>{reservation.memberId}</strong>
            </article>
            <article>
              <span>이메일</span>
              <strong>{reservation.email}</strong>
            </article>
            <article>
              <span>연락처</span>
              <strong>{reservation.phone}</strong>
            </article>
            <article>
              <span>차량</span>
              <strong>{reservation.vehicleName}</strong>
            </article>
            <article>
              <span>차량번호</span>
              <strong>{reservation.plateNumber}</strong>
            </article>
            <article>
              <span>충전소</span>
              <strong>{reservation.stationName}</strong>
            </article>
            <article>
              <span>충전소 주소</span>
              <strong>{reservation.stationAddress}</strong>
            </article>
            <article>
              <span>충전기</span>
              <strong>{reservation.chargerName}</strong>
            </article>
            <article>
              <span>충전기 유형</span>
              <strong>{reservation.chargerType} / {reservation.connectorType}</strong>
            </article>
          </div>
        </div>

        <div className="admin-panel">
          <div className="admin-panel-title">
            <div>
              <strong>처리 메모</strong>
              <p>예약 상태 변경 사유와 운영자가 확인할 내용을 남깁니다.</p>
            </div>
          </div>
          <div className="admin-note-box">
            <p>{reservation.memo}</p>
          </div>
          <div className="admin-mini-info">
            <span>등록일</span>
            <strong>{reservation.createdAt}</strong>
          </div>
          <div className="admin-mini-info">
            <span>수정일</span>
            <strong>{reservation.updatedAt}</strong>
          </div>
        </div>
      </div>

      <div className="admin-panel">
        <div className="admin-panel-title">
          <div>
            <strong>예약 처리 흐름</strong>
            <p>예약 생성부터 인증, 충전, 완료/취소/노쇼까지의 운영 흐름입니다.</p>
          </div>
        </div>
        <div className="admin-timeline">
          {timelineSteps.map((step, index) => (
            <article key={step.key} className={index === 0 || reservation.status !== "예약완료" ? "active" : ""}>
              <span>{index + 1}</span>
              <strong>{step.label}</strong>
              <p>{index === 0 ? reservation.createdAt : "처리 상태에 따라 업데이트"}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
};

export default AdminReservationDetailPage;
