import { useEffect, useMemo, useState } from "react";
import * as reservationApi from "../../apis/reservationApi";

const mockReservationList = [
  {
    reservationId: 101,
    stationName: "부산시청 공공충전소",
    chargerName: "급속 01",
    vehicleNickname: "출퇴근용 아이오닉",
    startTimeText: "2026-06-18 14:00",
    endTimeText: "2026-06-18 14:40",
    estimatedCost: 12800,
    status: "예약완료",
    stationImageUrl: "/images/station/station-default.jpg",
  },
  {
    reservationId: 102,
    stationName: "해운대 공영주차장 충전소",
    chargerName: "초급속 02",
    vehicleNickname: "주말용 EV6",
    startTimeText: "2026-06-15 19:20",
    endTimeText: "2026-06-15 20:10",
    estimatedCost: 15400,
    status: "완료",
    stationImageUrl: "/images/station/station-default.jpg",
  },
  {
    reservationId: 103,
    stationName: "센텀시티 공영주차장 충전소",
    chargerName: "급속 03",
    vehicleNickname: "출퇴근용 아이오닉",
    startTimeText: "2026-06-10 10:00",
    endTimeText: "2026-06-10 10:40",
    estimatedCost: 11900,
    status: "취소",
    stationImageUrl: "/images/station/station-default.jpg",
  },
];

const MyReservationPage = () => {
  console.log("MyReservationPage 렌더링");

  const [month, setMonth] = useState("");
  const [activeStatus, setActiveStatus] = useState("전체");
  const [reservationList, setReservationList] = useState([]);

  const getReservationList = async () => {
    console.log("getReservationList 실행", month);

    try {
      const response = await reservationApi.myList(month);
      console.log("내 예약 응답", response.data);
      setReservationList(Array.isArray(response.data) ? response.data : mockReservationList);
    } catch (error) {
      console.log("내 예약 조회 실패 - 목업 데이터 사용", error);
      setReservationList(mockReservationList);
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
      await reservationApi.cancel(reservationId);
      alert("예약이 취소되었습니다.");
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
      alert("인증코드 발급 요청이 완료되었습니다.");
    } catch (error) {
      console.log("인증코드 발급 오류", error);
      alert("인증코드 발급 중 오류가 발생했습니다.");
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
        <p>예약 현황을 확인하고 예약 취소 또는 현장 인증코드를 발급할 수 있습니다.</p>
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
                      className="mypage-danger-outline-btn"
                      onClick={() => cancelReservation(item.reservationId)}
                    >
                      예약취소
                    </button>
                  )}
                  {item.status !== "예약완료" && item.status !== "인증완료" && (
                    <button type="button" className="mypage-outline-btn">
                      상세보기
                    </button>
                  )}
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
