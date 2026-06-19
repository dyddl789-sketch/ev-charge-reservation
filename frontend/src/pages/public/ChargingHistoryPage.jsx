import { useEffect, useState } from "react";
import * as reservationApi from "../../apis/reservationApi";

const mockHistoryList = [
  {
    reservationId: 201,
    stationName: "부산역 공공충전소",
    chargerName: "급속 03",
    vehicleNickname: "출퇴근용 아이오닉",
    startTimeText: "2026-06-10 09:00",
    actualKwh: 32.4,
    actualMinutes: 42,
    actualCost: 12100,
    status: "완료",
    stationImageUrl: "/images/station/station-default.jpg",
  },
  {
    reservationId: 202,
    stationName: "센텀시티 공영주차장",
    chargerName: "완속 01",
    vehicleNickname: "주말용 EV6",
    startTimeText: "2026-06-03 18:30",
    actualKwh: 21.8,
    actualMinutes: 75,
    actualCost: 8300,
    status: "완료",
    stationImageUrl: "/images/station/station-default.jpg",
  },
];

const ChargingHistoryPage = () => {
  console.log("ChargingHistoryPage 렌더링");

  const [month, setMonth] = useState("");
  const [historyList, setHistoryList] = useState([]);

  const getHistoryList = async () => {
    console.log("getHistoryList 실행", month);

    try {
      const response = await reservationApi.historyList(month);
      console.log("충전내역 응답", response.data);
      setHistoryList(Array.isArray(response.data) ? response.data : mockHistoryList);
    } catch (error) {
      console.log("충전내역 조회 실패 - 목업 데이터 사용", error);
      setHistoryList(mockHistoryList);
    }
  };

  useEffect(() => {
    getHistoryList();
  }, []);

  const searchByMonth = (e) => {
    e.preventDefault();
    console.log("충전내역 월 검색", month);
    getHistoryList();
  };

  const sendReceipt = async (reservationId) => {
    console.log("영수증 이메일 발송 클릭", reservationId);

    try {
      await reservationApi.sendReceiptEmail(reservationId);
      alert("영수증 이메일 발송을 요청했습니다.");
    } catch (error) {
      console.log("영수증 이메일 발송 오류", error);
      alert("영수증 이메일 발송 중 오류가 발생했습니다.");
    }
  };

  const totalKwh = historyList.reduce((sum, item) => sum + Number(item.actualKwh || item.totalKwh || 0), 0);
  const totalCost = historyList.reduce((sum, item) => sum + Number(item.actualCost || item.totalCost || 0), 0);

  return (
    <>
      <div className="mypage-page-header">
        <span>CHARGING HISTORY</span>
        <h1>충전 이용 내역</h1>
        <p>완료된 충전 이용 내역과 충전량, 이용 금액을 확인할 수 있습니다.</p>
      </div>

      <section className="mypage-summary-grid">
        <div className="mypage-summary-card">
          <span className="summary-icon blue">▣</span>
          <div>
            <p>이용 건수</p>
            <strong>{historyList.length}건</strong>
          </div>
        </div>
        <div className="mypage-summary-card">
          <span className="summary-icon green">⚡</span>
          <div>
            <p>총 충전량</p>
            <strong>{totalKwh.toFixed(1)}kWh</strong>
          </div>
        </div>
        <div className="mypage-summary-card">
          <span className="summary-icon orange">￦</span>
          <div>
            <p>총 이용금액</p>
            <strong>{totalCost.toLocaleString()}원</strong>
          </div>
        </div>
      </section>

      <section className="mypage-list-section">
        <div className="mypage-list-section-header">
          <div>
            <h2>충전 이용 목록</h2>
            <p>기존 백엔드 /reservation/history 기준으로 연동합니다.</p>
          </div>

          <form className="mypage-filter-inline" onSubmit={searchByMonth}>
            <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
            <button type="submit" className="mypage-secondary-btn">조회</button>
          </form>
        </div>

        {historyList.length === 0 ? (
          <div className="empty-mypage-box">충전 이용 내역이 없습니다.</div>
        ) : (
          <div className="mypage-card-list inside-section">
            {historyList.map((item) => (
              <article className="mypage-list-card history-card" key={item.reservationId}>
                <div className="mypage-list-image">
                  <img
                    src={item.stationImageUrl || item.imageUrl || "/images/station/station-default.jpg"}
                    alt={item.stationName}
                    onError={(e) => {
                      e.currentTarget.src = "/images/station/station-default.jpg";
                    }}
                  />
                </div>

                <div className="mypage-list-body">
                  <div className="mypage-list-top">
                    <div>
                      <h2>{item.stationName}</h2>
                      <p>{item.chargerName} · {item.vehicleNickname || item.modelName || "등록 차량"}</p>
                    </div>
                    <span className="status-badge done">{item.status || "완료"}</span>
                  </div>

                  <div className="mypage-info-grid history-info-grid">
                    <div>
                      <span>시작시간</span>
                      <strong>{item.startTimeText || item.startTime}</strong>
                    </div>
                    <div>
                      <span>충전량</span>
                      <strong>{item.actualKwh || item.totalKwh || "-"}kWh</strong>
                    </div>
                    <div>
                      <span>충전시간</span>
                      <strong>{item.actualMinutes || item.estimatedMinutes || "-"}분</strong>
                    </div>
                    <div>
                      <span>금액</span>
                      <strong>{Number(item.actualCost || item.totalCost || 0).toLocaleString()}원</strong>
                    </div>
                  </div>

                  <div className="mypage-list-actions">
                    <button
                      type="button"
                      className="mypage-outline-btn"
                      onClick={() => sendReceipt(item.reservationId)}
                    >
                      영수증
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}

        <div className="mypage-info-notice">
          영수증은 전자세금계산서/현금영수증 발행 내역을 확인할 수 있습니다.
        </div>
      </section>
    </>
  );
};

export default ChargingHistoryPage;
