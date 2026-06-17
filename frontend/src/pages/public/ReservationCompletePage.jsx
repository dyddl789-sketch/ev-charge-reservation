import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import * as reservationApi from "../../apis/reservationApi";
import "../../styles/station-reservation.css";

const ReservationCompletePage = () => {
  console.log("ReservationCompletePage 렌더링");

  const [searchParams] = useSearchParams();
  const reservationId = searchParams.get("reservationId");
  const [reservation, setReservation] = useState(null);

  useEffect(() => {
    const getComplete = async () => {
      console.log("예약 완료 조회", reservationId);

      if (!reservationId) {
        return;
      }

      try {
        const response = await reservationApi.complete(reservationId);
        console.log("예약 완료 응답", response.data);
        setReservation(response.data);
      } catch (error) {
        console.log("예약 완료 조회 실패", error);
      }
    };

    getComplete();
  }, [reservationId]);

  return (
    <section className="reservation-page">
      <div className="reservation-inner complete-wrap">
        <div className="complete-card">
          <div className="complete-icon">✓</div>
          <h1>예약이 완료되었습니다</h1>
          <p className="complete-desc">예약 시간에 충전소에서 인증 코드를 입력하면 충전이 가능합니다.</p>

          {reservation && (
            <div className="complete-summary">
              <div><span>예약 상태</span><strong>{reservation.status}</strong></div>
              <div><span>예약 차량</span><strong>{reservation.vehicleNickname || reservation.modelName}</strong></div>
              <div><span>충전소</span><strong>{reservation.stationName}</strong></div>
              <div><span>충전기</span><strong>{reservation.chargerName} · {reservation.connectorType}</strong></div>
              <div><span>예약 날짜</span><strong>{reservation.reservationDate}</strong></div>
              <div><span>배터리 잔량</span><strong>{reservation.currentSoc}% → {reservation.targetSoc}%</strong></div>
              <div><span>예상 충전 시간</span><strong>{reservation.estimatedMinutes}분</strong></div>
              <div className="complete-cost-row"><span>예상 충전 비용</span><strong>{Number(reservation.estimatedCost || 0).toLocaleString("ko-KR")}원</strong></div>
            </div>
          )}

          <div className="complete-buttons">
            <Link to="/my-reservations" className="complete-main-btn">내 예약 보기</Link>
            <Link to="/stations" className="complete-outline-btn">충전소 탐색</Link>
            <Link to="/" className="complete-outline-btn">메인으로</Link>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ReservationCompletePage;
