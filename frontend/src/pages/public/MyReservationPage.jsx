import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import * as reservationApi from "../../apis/reservationApi";

const mockReservationList = [];

const MyReservationPage = () => {
  console.log("MyReservationPage 렌더링");

  const [month, setMonth] = useState("");
  const [activeStatus, setActiveStatus] = useState("전체");
  const [reservationList, setReservationList] = useState([]);
  const [issuedCodeInfo, setIssuedCodeInfo] = useState(null);

  const getReservationList = async () => {
    console.log("getReservationList 실행", month);

    try {
      const response = await reservationApi.myList(month);
      console.log("내 예약 응답", response.data);
      setReservationList(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.log("내 예약 조회 실패", error);
      alert(error.response?.data?.message || "내 예약 목록을 불러오지 못했습니다.");
      setReservationList([]);
    }
  };

  useEffect(() => {
    getReservationList();
  }, []);

  const searchByMonth = (e) => {
    e.preventDefault();
    console.log("예약 월 검색", month);
    getReservationList();
  };

  const cancelReservation = async (reservationId) => {
    console.log("예약 취소 클릭", reservationId);

    if (!window.confirm("예약을 취소하시겠습니까?")) {
      return;
    }

    try {
      const response = await reservationApi.cancel(reservationId);
      alert(response.data?.message || "예약이 취소되었습니다.");
      setReservationList((prev) =>
        prev.map((item) =>
          item.reservationId === reservationId ? { ...item, status: "취소" } : item
        )
      );
    } catch (error) {
      console.log("예약 취소 오류", error);
      alert("예약 취소 중 오류가 발생했습니다.");
    }
  };

  const issueAuthCode = async (reservationId) => {
    console.log("인증코드 발급 클릭", reservationId);

    try {
      const response = await reservationApi.issueAuthCode(reservationId);
      console.log("인증코드 발급 응답", response.data);

      const authCode = response.data?.authCode;
      setIssuedCodeInfo({ reservationId, authCode });
      alert(authCode ? `인증코드: ${authCode}` : response.data?.message || "인증코드가 발급되었습니다.");
    } catch (error) {
      console.log("인증코드 발급 오류", error);
      alert(error.response?.data?.message || "인증코드 발급 중 오류가 발생했습니다.");
    }
  };

  const verifyReservation = async (reservationId) => {
    console.log("예약 인증 클릭", reservationId);

    const defaultCode = issuedCodeInfo?.reservationId === reservationId ? issuedCodeInfo.authCode : "";
    const authCode = window.prompt("인증코드를 입력해 주세요.", defaultCode || "");

    if (!authCode) {
      return;
    }

    try {
      const response = await reservationApi.verify(reservationId, authCode);
      alert(response.data?.message || "예약 인증이 완료되었습니다.");
      setReservationList((prev) =>
        prev.map((item) =>
          item.reservationId === reservationId ? { ...item, status: "충전중" } : item
        )
      );
    } catch (error) {
      console.log("예약 인증 오류", error);
      alert(error.response?.data?.message || "예약 인증 중 오류가 발생했습니다.");
    }
  };

  const getStatusClass = (status) => {
    if (status === "완료") return "done";
    if (status === "취소" || status === "노쇼") return "cancel";
    if (status === "예약완료" || status === "인증완료") return "wait";
    return "";
  };

  const statusTabs = useMemo(() => {
    const total = reservationList.length;
    const reserved = reservationList.filter((item) => item.status === "예약완료" || item.status === "인증완료").length;
    const done = reservationList.filter((item) => item.status === "완료").length;
    const canceled = reservationList.filter((item) => item.status === "취소" || item.status === "노쇼").length;

    return [
      { label: "전체", title: "전체 예약", count: total },
      { label: "예약완료", title: "예약완료", count: reserved },
      { label: "완료", title: "완료", count: done },
      { label: "취소/노쇼", title: "취소/노쇼", count: canceled },
    ];
  }, [reservationList]);

  const filteredReservationList = reservationList.filter((item) => {
    if (activeStatus === "전체") return true;
    if (activeStatus === "예약완료") return item.status === "예약완료" || item.status === "인증완료";
    if (activeStatus === "취소/노쇼") return item.status === "취소" || item.status === "노쇼";
    return item.status === activeStatus;
  });

  return (
    <>
      <div className="mypage-page-header">
        <span>RESERVATION</span>
        <h1>내 예약</h1>
        <p>예약 현황을 확인하고 상세보기에서 인증코드와 충전 시작 시뮬레이션을 진행할 수 있습니다.</p>
      </div>

      <section className="mypage-status-tabs" aria-label="예약 상태 필터">
        {statusTabs.map((tab) => (
          <button
            key={tab.label}
            type="button"
            className={`mypage-status-tab ${activeStatus === tab.label ? "active" : ""}`}
            onClick={() => {
              console.log("예약 상태 탭 클릭", tab.label);
              setActiveStatus(tab.label);
            }}
          >
            <strong>{tab.title}</strong>
            <span>{tab.count}</span>
          </button>
        ))}
      </section>

      <form className="mypage-filter-row" onSubmit={searchByMonth}>
        <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
        <button type="submit" className="mypage-secondary-btn">조회</button>
      </form>

      {filteredReservationList.length === 0 ? (
        <div className="empty-mypage-box">예약 내역이 없습니다.</div>
      ) : (
        <section className="mypage-card-list">
          {filteredReservationList.map((item) => (
            <article className="mypage-list-card" key={item.reservationId}>
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
                  <span className={`status-badge ${getStatusClass(item.status)}`}>{item.status}</span>
                </div>

                <div className="mypage-info-grid reservation-info-grid">
                  <div>
                    <span>예약시간</span>
                    <strong>{item.startTimeText || item.startTime}</strong>
                  </div>
                  <div>
                    <span>예상금액</span>
                    <strong>{Number(item.estimatedCost || 0).toLocaleString()}원</strong>
                  </div>
                  <div>
                    <span>예약번호</span>
                    <strong>{item.reservationId}</strong>
                  </div>
                </div>

                <div className="mypage-list-actions">
                  {(item.status === "예약완료" || item.status === "인증완료") && (
                    <button
                      type="button"
                      className="mypage-outline-btn"
                      onClick={() => issueAuthCode(item.reservationId)}
                    >
                      인증코드
                    </button>
                  )}
                  {item.status === "예약완료" && (
                    <button
                      type="button"
                      className="mypage-outline-btn"
                      onClick={() => verifyReservation(item.reservationId)}
                    >
                      인증하기
                    </button>
                  )}
                  {item.status === "예약완료" && (
                    <button
                      type="button"
                      className="mypage-danger-outline-btn"
                      onClick={() => cancelReservation(item.reservationId)}
                    >
                      예약취소
                    </button>
                  )}
                  <Link to={`/my-reservations/${item.reservationId}`} className="mypage-outline-link">
                    상세보기
                  </Link>
                </div>
              </div>
            </article>
          ))}
        </section>
      )}
    </>
  );
};

export default MyReservationPage;
