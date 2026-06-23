import { useEffect, useMemo, useState } from "react";
import * as reservationApi from "../../apis/reservationApi";

const numberValue = (value) => {
  const number = Number(value ?? 0);
  return Number.isNaN(number) ? 0 : number;
};

const getChargingKwh = (item) =>
  numberValue(item.actualKwh ?? item.totalKwh ?? item.requiredKwh ?? item.chargedKwh);

const getChargingMinutes = (item) =>
  numberValue(item.actualMinutes ?? item.totalMinutes ?? item.estimatedMinutes);

const getChargingCost = (item) =>
  numberValue(item.actualCost ?? item.totalCost ?? item.estimatedCost);

const normalizeHistoryList = (data) => {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.historyList)) {
    return data.historyList;
  }

  if (Array.isArray(data?.content)) {
    return data.content;
  }

  return [];
};

const ChargingHistoryPage = () => {
  console.log("ChargingHistoryPage 렌더링");

  const [month, setMonth] = useState("");
  const [historyList, setHistoryList] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const getHistoryList = async (searchMonth = month) => {
    console.log("getHistoryList 실행", searchMonth);

    setIsLoading(true);
    setErrorMessage("");

    try {
      const response = await reservationApi.historyList(searchMonth);
      console.log("충전내역 응답", response.data);
      setHistoryList(normalizeHistoryList(response.data));
    } catch (error) {
      console.log("충전내역 조회 실패", error);
      setHistoryList([]);
      setErrorMessage(
        error.response?.data?.message ||
          "충전 이용 내역을 불러오지 못했습니다. 로그인 상태와 백엔드 실행 상태를 확인해 주세요."
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getHistoryList("");
  }, []);

  const searchByMonth = (e) => {
    e.preventDefault();
    console.log("충전내역 월 검색", month);
    getHistoryList(month);
  };

  const sendReceipt = async (reservationId) => {
    console.log("영수증 이메일 발송 클릭", reservationId);

    try {
      await reservationApi.sendReceiptEmail(reservationId);
      alert("영수증 이메일 발송을 요청했습니다.");
    } catch (error) {
      console.log("영수증 이메일 발송 오류", error);
      alert(error.response?.data?.message || "영수증 이메일 발송 중 오류가 발생했습니다.");
    }
  };

  const summary = useMemo(() => {
    const totalKwh = historyList.reduce((sum, item) => sum + getChargingKwh(item), 0);
    const totalCost = historyList.reduce((sum, item) => sum + getChargingCost(item), 0);

    return { totalKwh, totalCost };
  }, [historyList]);

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
            <strong>{summary.totalKwh.toFixed(1)}kWh</strong>
          </div>
        </div>
        <div className="mypage-summary-card">
          <span className="summary-icon orange">￦</span>
          <div>
            <p>총 이용금액</p>
            <strong>{summary.totalCost.toLocaleString()}원</strong>
          </div>
        </div>
      </section>

      <section className="mypage-list-section">
        <div className="mypage-list-section-header">
          <div>
            <h2>충전 이용 목록</h2>
            <p>백엔드 완료 예약 데이터를 기준으로 충전량과 금액을 표시합니다.</p>
          </div>

          <form className="mypage-filter-inline" onSubmit={searchByMonth}>
            <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
            <button type="submit" className="mypage-secondary-btn">조회</button>
          </form>
        </div>

        {errorMessage && <div className="empty-mypage-box">{errorMessage}</div>}

        {isLoading ? (
          <div className="empty-mypage-box">충전 이용 내역을 불러오는 중입니다.</div>
        ) : historyList.length === 0 && !errorMessage ? (
          <div className="empty-mypage-box">충전 이용 내역이 없습니다.</div>
        ) : (
          <div className="mypage-card-list inside-section">
            {historyList.map((item) => {
              const chargingKwh = getChargingKwh(item);
              const chargingMinutes = getChargingMinutes(item);
              const chargingCost = getChargingCost(item);

              return (
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
                        <strong>{chargingKwh > 0 ? `${chargingKwh.toFixed(1)}kWh` : "-"}</strong>
                      </div>
                      <div>
                        <span>충전시간</span>
                        <strong>{chargingMinutes > 0 ? `${chargingMinutes}분` : "-"}</strong>
                      </div>
                      <div>
                        <span>금액</span>
                        <strong>{chargingCost.toLocaleString()}원</strong>
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
              );
            })}
          </div>
        )}

        <div className="mypage-info-notice">
          충전량과 이용금액은 예약 생성 시 계산된 예상 충전량/비용 또는 실제 충전 세션 데이터를 기준으로 표시됩니다.
        </div>
      </section>
    </>
  );
};

export default ChargingHistoryPage;
