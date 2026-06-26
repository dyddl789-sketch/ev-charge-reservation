import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import * as reservationApi from "../../apis/reservationApi";

const formatMoney = (value) => `${Number(value || 0).toLocaleString()}원`;

const formatDateTime = (value) => {
  if (!value) {
    return "-";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return String(value).replace("T", " ");
  }

  return date.toLocaleString("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const CHARGE_SIMULATION_SECONDS = 20;
const CHARGE_PROGRESS_STEP = 5;
const CHARGE_PROGRESS_INTERVAL_MS = (CHARGE_SIMULATION_SECONDS * 1000) / (100 / CHARGE_PROGRESS_STEP);

const addSeconds = (date, seconds) => {
  return new Date(date.getTime() + Number(seconds || 0) * 1000);
};

const MyReservationDetailPage = () => {
  console.log("MyReservationDetailPage 렌더링");

  const { reservationId } = useParams();
  const navigate = useNavigate();

  const moveTimerRef = useRef(null);
  const chargeTimerRef = useRef(null);

  const [reservation, setReservation] = useState(null);
  const [authCode, setAuthCode] = useState("");
  const [simulationOpen, setSimulationOpen] = useState(false);
  const [simulationStep, setSimulationStep] = useState("READY");
  const [progress, setProgress] = useState(0);
  const [chargeProgress, setChargeProgress] = useState(0);
  const [inputCode, setInputCode] = useState("");
  const [actualStartTime, setActualStartTime] = useState(null);
  const [expectedActualEndTime, setExpectedActualEndTime] = useState(null);
  const [isCompleting, setIsCompleting] = useState(false);

  const verifyWindowText = useMemo(() => {
    if (!reservation?.startTime) {
      return "예약 시작 5분 전부터 시작 후 5분까지 인증할 수 있습니다.";
    }

    const start = new Date(reservation.startTime);
    const from = new Date(start.getTime() - 5 * 60 * 1000);
    const to = new Date(start.getTime() + 5 * 60 * 1000);

    return `${formatDateTime(from)} ~ ${formatDateTime(to)}`;
  }, [reservation]);

  const canCancel = reservation?.status === "예약완료";
  const canStartSimulation = ["예약완료", "충전중"].includes(reservation?.status);

  const clearTimers = () => {
    if (moveTimerRef.current) {
      window.clearInterval(moveTimerRef.current);
      moveTimerRef.current = null;
    }

    if (chargeTimerRef.current) {
      window.clearInterval(chargeTimerRef.current);
      chargeTimerRef.current = null;
    }
  };

  const loadReservation = async () => {
    console.log("내 예약 상세 조회", reservationId);

    try {
      const response = await reservationApi.myDetail(reservationId);
      console.log("내 예약 상세 응답", response.data);
      setReservation(response.data);
      setAuthCode(response.data?.authCode || "");
    } catch (error) {
      console.log("내 예약 상세 조회 실패", error);
      alert(error.response?.data?.message || "예약 상세 정보를 불러오지 못했습니다.");
      navigate("/my-reservations");
    }
  };

  useEffect(() => {
    loadReservation();

    return () => {
      clearTimers();
    };
  }, [reservationId]);

  const fetchAuthCodeSilently = async () => {
    console.log("예약 상세 인증코드 조용히 조회", reservationId);

    try {
      const response = await reservationApi.issueAuthCode(reservationId);
      const nextCode = response.data?.authCode || "";
      setAuthCode(nextCode);
      return nextCode;
    } catch (error) {
      console.log("인증코드 조용히 조회 실패", error);
      return "";
    }
  };

  const showAuthCode = async () => {
    console.log("예약 상세 인증코드 보기", reservationId);

    try {
      const response = await reservationApi.issueAuthCode(reservationId);
      const nextCode = response.data?.authCode;
      setAuthCode(nextCode || "");
      alert(nextCode ? `인증코드: ${nextCode}` : response.data?.message || "인증코드를 확인했습니다.");
    } catch (error) {
      console.log("인증코드 조회 실패", error);
      alert(error.response?.data?.message || "인증코드 조회 중 오류가 발생했습니다.");
    }
  };

  const moveMap = () => {
    if (!reservation?.stationId) {
      alert("충전소 위치 정보가 없습니다.");
      return;
    }

    const params = new URLSearchParams({
      focusType: "station",
      stationId: String(reservation.stationId),
      lat: String(reservation.stationLatitude || ""),
      lng: String(reservation.stationLongitude || ""),
      name: reservation.stationName || "",
      address: reservation.stationAddress || "",
    });

    navigate(`/stations?${params.toString()}`);
  };

  const moveComplaint = () => {
    if (!reservation?.stationId || !reservation?.chargerId) {
      alert("민원 접수에 필요한 충전소/충전기 정보가 없습니다.");
      return;
    }

    navigate(`/complaint?type=충전기고장&stationId=${reservation.stationId}&chargerId=${reservation.chargerId}`);
  };

  const cancelReservation = async () => {
    if (!window.confirm("예약을 취소하시겠습니까?")) {
      return;
    }

    try {
      const response = await reservationApi.cancel(reservationId);
      alert(response.data?.message || "예약이 취소되었습니다.");
      await loadReservation();
    } catch (error) {
      console.log("예약 취소 실패", error);
      alert(error.response?.data?.message || "예약 취소 중 오류가 발생했습니다.");
    }
  };

  const startSimulation = async () => {
    console.log("충전 시작 시뮬레이션 시작");

    if (!canStartSimulation) {
      alert("예약완료 또는 충전중 상태에서만 충전 시작 시뮬레이션을 실행할 수 있습니다.");
      return;
    }

    clearTimers();
    setSimulationOpen(true);
    setInputCode("");
    setChargeProgress(0);
    setActualStartTime(null);
    setExpectedActualEndTime(null);

    if (!authCode) {
      await fetchAuthCodeSilently();
    }

    if (reservation?.status === "충전중") {
      const start = reservation?.actualStartTime ? new Date(reservation.actualStartTime) : new Date(reservation?.verifiedAt || Date.now());
      const end = addSeconds(new Date(), CHARGE_SIMULATION_SECONDS);
      setActualStartTime(start);
      setExpectedActualEndTime(end);
      setSimulationStep("CHARGING");
      setProgress(100);
      startChargeGauge();
      return;
    }

    setSimulationStep("MOVING");
    setProgress(0);

    let current = 0;
    moveTimerRef.current = window.setInterval(() => {
      current += 5;
      setProgress(current);

      if (current >= 100) {
        window.clearInterval(moveTimerRef.current);
        moveTimerRef.current = null;
        setSimulationStep("ARRIVED");
      }
    }, 650);
  };

  const startChargeGauge = () => {
    console.log("충전 게이지 시뮬레이션 시작");

    if (chargeTimerRef.current) {
      window.clearInterval(chargeTimerRef.current);
    }

    let current = 0;
    setChargeProgress(0);
    setIsCompleting(false);

    chargeTimerRef.current = window.setInterval(() => {
      current += CHARGE_PROGRESS_STEP;
      setChargeProgress(current);

      if (current >= 100) {
        window.clearInterval(chargeTimerRef.current);
        chargeTimerRef.current = null;
        completeChargingSimulation();
      }
    }, CHARGE_PROGRESS_INTERVAL_MS);
  };

  const verifyWithCode = async () => {
    console.log("시뮬레이션 인증", reservationId, inputCode);

    if (!inputCode.trim()) {
      alert("인증코드를 입력하세요.");
      return;
    }

    try {
      const response = await reservationApi.verify(reservationId, inputCode.trim());
      alert(response.data?.message || "예약 인증이 완료되었습니다.");

      const start = new Date();
      const end = addSeconds(start, CHARGE_SIMULATION_SECONDS);
      setActualStartTime(start);
      setExpectedActualEndTime(end);
      setSimulationStep("CHARGING");
      await loadReservation();
      startChargeGauge();
    } catch (error) {
      console.log("시뮬레이션 인증 실패", error);
      alert(error.response?.data?.message || "예약 인증 중 오류가 발생했습니다.");
    }
  };

  const completeChargingSimulation = async () => {
    console.log("충전 완료 시뮬레이션 API 요청", reservationId);

    try {
      setIsCompleting(true);
      const response = await reservationApi.completeChargingSimulation(reservationId);
      console.log("충전 완료 시뮬레이션 응답", response.data);
      setSimulationStep("COMPLETED");
      await loadReservation();
    } catch (error) {
      console.log("충전 완료 시뮬레이션 실패", error);
      alert(error.response?.data?.message || "충전 완료 처리 중 오류가 발생했습니다.");
      setSimulationStep("CHARGING");
    } finally {
      setIsCompleting(false);
    }
  };

  const closeSimulation = () => {
    clearTimers();
    setSimulationOpen(false);
  };

  if (!reservation) {
    return <div className="empty-mypage-box">예약 상세 정보를 불러오는 중입니다.</div>;
  }

  return (
    <>
      <div className="mypage-page-header">
        <span>RESERVATION DETAIL</span>
        <h1>예약 상세</h1>
        <p>예약 정보, 인증코드, 길찾기 시뮬레이션, 민원 접수를 한 화면에서 처리합니다.</p>
      </div>

      <section className="mypage-detail-card">
        <div className="mypage-detail-head">
          <div>
            <span className="status-badge wait">{reservation.status}</span>
            <h2>{reservation.stationName}</h2>
            <p>{reservation.stationAddress}</p>
          </div>
          <strong>예약번호 #{reservation.reservationId}</strong>
        </div>

        <div className="mypage-detail-grid">
          <article><span>예약 시간</span><strong>{reservation.startTimeText || formatDateTime(reservation.startTime)}</strong></article>
          <article><span>예약 종료 예정</span><strong>{reservation.endTimeText || formatDateTime(reservation.endTime)}</strong></article>
          <article><span>예상 금액</span><strong>{formatMoney(reservation.estimatedCost)}</strong></article>
          <article><span>예상 충전량</span><strong>{reservation.requiredKwh}kWh</strong></article>
          <article><span>배터리</span><strong>{reservation.currentSoc}% → {reservation.targetSoc}%</strong></article>
          <article><span>인증 가능 시간</span><strong>{verifyWindowText}</strong></article>
        </div>

        <div className="mypage-detail-grid two">
          <article><span>충전기</span><strong>{reservation.chargerName} / {reservation.connectorType}</strong></article>
          <article><span>충전기 상태</span><strong>{reservation.chargerStatus || "-"}</strong></article>
          <article><span>차량</span><strong>{reservation.vehicleNickname || reservation.modelName}</strong></article>
          <article><span>인증코드</span><strong>{authCode || "인증코드 보기 버튼을 눌러 확인"}</strong></article>
        </div>

        {(reservation.actualStartTime || reservation.actualEndTime || reservation.status === "완료") && (
          <div className="actual-session-box">
            <h3>실제 충전 세션</h3>
            <p>예약 예정 시간은 보존하고, 실제 충전 시작/완료 시각은 충전 시뮬레이션 실행 시각 기준으로 관리합니다.</p>
            <div className="mypage-detail-grid two">
              <article><span>실제 충전 시작</span><strong>{reservation.actualStartTimeText || formatDateTime(reservation.actualStartTime)}</strong></article>
              <article><span>실제 충전 완료</span><strong>{reservation.actualEndTimeText || formatDateTime(reservation.actualEndTime)}</strong></article>
              <article><span>실제 충전량</span><strong>{reservation.actualKwh || reservation.requiredKwh}kWh</strong></article>
              <article><span>실제 이용금액</span><strong>{formatMoney(reservation.actualCost || reservation.estimatedCost)}</strong></article>
            </div>
          </div>
        )}

        <div className="mypage-list-actions detail-actions">
          <button type="button" className="mypage-outline-btn" onClick={showAuthCode}>인증코드 보기</button>
          <button type="button" className="mypage-outline-btn" onClick={startSimulation} disabled={!canStartSimulation}>충전 시작 시뮬레이션</button>
          <button type="button" className="mypage-outline-btn" onClick={moveMap}>지도에서 보기</button>
          <button type="button" className="mypage-outline-btn" onClick={moveComplaint}>이 충전기 민원 접수</button>
          {canCancel && <button type="button" className="mypage-danger-outline-btn" onClick={cancelReservation}>예약 취소</button>}
          <Link to="/my-reservations" className="mypage-outline-link">목록으로</Link>
        </div>
      </section>

      {simulationOpen && (
        <section className="reservation-simulation-modal">
          <div className="reservation-simulation-box wide">
            <div className="simulation-head">
              <div>
                <h2>충전 시작 시뮬레이션</h2>
                <p>지도 이동 후 인증코드를 입력하면 충전 게이지가 20초 동안 진행됩니다.</p>
              </div>
              <button type="button" onClick={closeSimulation}>닫기</button>
            </div>

            <div className="simulation-route-card">
              <div className="simulation-map-placeholder">
                <div className="simulation-road">
                  <span className="simulation-car" style={{ left: `${progress}%` }}>🚗</span>
                </div>
                <div className="simulation-labels"><span>기본 출발지</span><span>{reservation.stationName}</span></div>
              </div>
              <div className="simulation-progress"><span style={{ width: `${progress}%` }} /></div>
              <p>{simulationStep === "MOVING" ? `충전소로 이동 중입니다. ${progress}%` : "충전소에 도착했습니다. 인증코드를 입력해 주세요."}</p>
            </div>

            {simulationStep === "ARRIVED" && (
              <div className="simulation-arrived-grid">
                <div className="simulation-auth-code-card">
                  <span>내 인증코드</span>
                  <strong>{authCode || "인증코드 조회 중"}</strong>
                  <p>예약자 본인에게 발급된 코드입니다. 아래 입력칸에 직접 입력해 주세요.</p>
                </div>
                <div className="simulation-auth-box">
                  <label>
                    인증코드 입력
                    <input value={inputCode} onChange={(e) => setInputCode(e.target.value)} placeholder="6자리 인증코드" maxLength={6} />
                  </label>
                  <button type="button" onClick={verifyWithCode}>인증하기</button>
                </div>
              </div>
            )}

            {simulationStep === "CHARGING" && (
              <div className="simulation-charge-card">
                <div className="simulation-charge-head">
                  <div>
                    <span>충전중...</span>
                    <h3>{reservation.currentSoc}% → {reservation.targetSoc}%</h3>
                  </div>
                  <strong>{chargeProgress}%</strong>
                </div>
                <div className="simulation-charge-progress"><span style={{ width: `${chargeProgress}%` }} /></div>
                <div className="simulation-charge-info">
                  <article><span>실제 충전 시작</span><strong>{formatDateTime(actualStartTime || reservation.actualStartTime || reservation.verifiedAt)}</strong></article>
                  <article><span>시연 완료 예정</span><strong>{formatDateTime(expectedActualEndTime)}</strong></article>
                  <article><span>예약 종료 예정</span><strong>{reservation.endTimeText || formatDateTime(reservation.endTime)}</strong></article>
                </div>
                <p>시연을 위해 충전 진행 과정은 20초로 고정해 표시합니다. 게이지 완료 시각이 실제 충전 완료 시각으로 저장됩니다.</p>
              </div>
            )}

            {simulationStep === "COMPLETED" && (
              <div className="simulation-complete-card">
                <strong>충전 완료</strong>
                <p>예약 상태가 완료로 변경되었고 충전기 상태가 사용가능으로 복구되었습니다.</p>
                <div className="simulation-complete-actions">
                  <button type="button" onClick={closeSimulation}>닫기</button>
                  <Link to="/charging-history">충전내역 보기</Link>
                </div>
              </div>
            )}

            {isCompleting && <p className="simulation-saving-message">충전 완료 정보를 저장하는 중입니다...</p>}
          </div>
        </section>
      )}
    </>
  );
};

export default MyReservationDetailPage;
