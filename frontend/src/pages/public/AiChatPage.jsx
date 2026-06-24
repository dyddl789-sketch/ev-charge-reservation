import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import * as aiApi from "../../apis/aiApi";
import "../../styles/ai-chat.css";

const pad = (value) => String(value).padStart(2, "0");

const getTodayText = () => {
  const date = new Date();
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const getCurrentTimeText = () => {
  const date = new Date();
  return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const getNextHourText = () => {
  const date = new Date(Date.now() + 60 * 60 * 1000);
  return `${pad(date.getHours())}:00`;
};

const buildDateTime = (dateText, timeText) => {
  if (!dateText || !timeText) {
    return null;
  }

  return new Date(`${dateText}T${timeText}:00`);
};

const guideMessages = [
  {
    messageId: 1,
    senderType: "AI",
    message:
      "안녕하세요. AI 충전 비서입니다. DB에 저장된 대표 차량과 기본 출발지를 기준으로 주변 충전소 추천, 가격순/충전빠른순 후보 조회, 예약 확정까지 도와드릴 수 있습니다.",
    createdAt: "안내 메시지",
  },
];

const quickQuestions = [
  "내 위치 알려줘",
  "내 예약 보여줘",
  "내 차량에 맞는 가까운 충전소 찾아줘",
  "오늘 예약 가능한 충전소 찾아줘",
  "가장 저렴한 충전소로 예약 후보 보여줘",
  "충전이 가장 빠른 순으로 예약 후보 보여줘",
  "예약 취소는 어떻게 해?",
];

const sortLabels = {
  DISTANCE: "가까운순",
  COST: "가격순",
  SPEED: "충전빠른순",
};

const valueOf = (item, ...keys) => {
  if (!item) {
    return null;
  }

  for (const key of keys) {
    if (item[key] !== undefined && item[key] !== null) {
      return item[key];
    }
  }

  return null;
};

const isMyReservationRequest = (text) => {
  const value = text.replaceAll(" ", "").toLowerCase();
  const reservationWord =
    value.includes("내예약") ||
    value.includes("예약내역") ||
    value.includes("예약목록") ||
    value.includes("예약조회") ||
    value.includes("예약한곳") ||
    value.includes("내가예약");

  const guideWord = value.includes("취소") || value.includes("변경") || value.includes("방법") || value.includes("어떻게");

  return reservationWord && !guideWord;
};

const isReservationGuideRequest = (text) => {
  const value = text.replaceAll(" ", "").toLowerCase();
  const reservationWord = value.includes("예약");
  const guideWord =
    value.includes("취소") ||
    value.includes("변경") ||
    value.includes("방법") ||
    value.includes("어떻게") ||
    value.includes("조회");
  const recommendWord = value.includes("가능") || value.includes("후보") || value.includes("추천") || value.includes("찾") || value.includes("해줘");

  return reservationWord && guideWord && !recommendWord;
};

const isReservationRequest = (text) => {
  const value = text.replaceAll(" ", "").toLowerCase();

  if (isMyReservationRequest(text) || isReservationGuideRequest(text)) {
    return false;
  }

  const reservationWord = value.includes("예약") || value.includes("후보");
  const stationWord = value.includes("충전소") || value.includes("충전기") || value.includes("후보");
  const actionWord = value.includes("가능") || value.includes("찾") || value.includes("추천") || value.includes("도와") || value.includes("보여줘") || value.includes("해줘");

  return (reservationWord && (stationWord || actionWord)) || (stationWord && actionWord);
};

const resolveSortFromText = (text) => {
  const value = text.replaceAll(" ", "").toLowerCase();

  if (value.includes("저렴") || value.includes("싼") || value.includes("가격") || value.includes("요금") || value.includes("비용")) {
    return "COST";
  }

  if (value.includes("빠른") || value.includes("급속") || value.includes("초급속") || value.includes("속도") || value.includes("시간")) {
    return "SPEED";
  }

  return "DISTANCE";
};

const buildStationMapUrl = ({ stationId, latitude, longitude, name, address, focusType = "station" }) => {
  const params = new URLSearchParams();

  if (focusType) {
    params.set("focusType", focusType);
  }
  if (stationId) {
    params.set("stationId", stationId);
  }
  if (latitude !== undefined && latitude !== null && latitude !== "") {
    params.set("lat", latitude);
  }
  if (longitude !== undefined && longitude !== null && longitude !== "") {
    params.set("lng", longitude);
  }
  if (name) {
    params.set("name", name);
  }
  if (address) {
    params.set("address", address);
  }

  return `/stations?${params.toString()}`;
};

const AiChatPage = () => {
  console.log("AiChatPage 렌더링");

  const navigate = useNavigate();
  const bottomRef = useRef(null);
  const [messages, setMessages] = useState(guideMessages);
  const [inputMessage, setInputMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [reservationCandidates, setReservationCandidates] = useState([]);
  const [activeSort, setActiveSort] = useState("DISTANCE");
  const [reservationForm, setReservationForm] = useState({
    reservationDate: getTodayText(),
    startTime: getNextHourText(),
    currentSoc: 30,
    targetSoc: 80,
  });

  useEffect(() => {
    loadMessages();
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, loading]);

  const loadMessages = async () => {
    console.log("AI 채팅 이력 로딩");

    try {
      const response = await aiApi.getMessages();
      console.log("AI 채팅 이력 응답", response);

      const data = response.data;
      if (Array.isArray(data) && data.length > 0) {
        setMessages(data);
      }
    } catch (error) {
      console.log("AI 채팅 이력 로딩 실패 - 기본 안내 메시지 사용", error);
      setMessages(guideMessages);
    }
  };

  const addAiMessage = (message, extra = {}) => {
    const aiMessage = {
      messageId: Date.now() + Math.floor(Math.random() * 1000),
      senderType: "AI",
      message,
      createdAt: new Date().toLocaleString(),
      ...extra,
    };

    setMessages((prev) => [...prev, aiMessage]);
  };

  const validateReservationCondition = () => {
    console.log("AI 예약 조건 검증", reservationForm);

    const startDateTime = buildDateTime(reservationForm.reservationDate, reservationForm.startTime);
    const now = new Date();

    if (!startDateTime) {
      alert("예약 날짜와 시작 시간을 선택해 주세요.");
      return false;
    }

    if (startDateTime.getTime() < now.getTime()) {
      alert("현재 시간보다 이전 시간으로 AI 예약을 진행할 수 없습니다.");
      return false;
    }

    const currentSoc = Number(reservationForm.currentSoc);
    const targetSoc = Number(reservationForm.targetSoc);

    if (Number.isNaN(currentSoc) || Number.isNaN(targetSoc)) {
      alert("현재 SOC와 목표 SOC를 입력해 주세요.");
      return false;
    }

    if (currentSoc < 0 || currentSoc > 100 || targetSoc < 0 || targetSoc > 100) {
      alert("SOC는 0~100 사이로 입력해 주세요.");
      return false;
    }

    if (targetSoc <= currentSoc) {
      alert("목표 SOC는 현재 SOC보다 커야 합니다.");
      return false;
    }

    return true;
  };

  const prepareAiReservation = async ({ messageText, sort }) => {
    console.log("AI 예약 후보 조회 실행", messageText, sort);

    if (!validateReservationCondition()) {
      return;
    }

    setLoading(true);

    try {
      const response = await aiApi.prepareReservation({
        message: messageText,
        sort,
        reservationDate: reservationForm.reservationDate,
        startTime: reservationForm.startTime,
        currentSoc: Number(reservationForm.currentSoc),
        targetSoc: Number(reservationForm.targetSoc),
      });

      const data = response.data;
      console.log("AI 예약 후보 조회 응답", data);

      const candidates = Array.isArray(data.candidates) ? data.candidates : [];
      setReservationCandidates(candidates);
      setActiveSort(data.sort || sort);

      addAiMessage(data.message || "선택한 날짜와 시간 기준으로 예약 가능한 충전기 후보를 조회했습니다.", {
        intent: "AI_RESERVATION_PREPARE",
        candidates,
        location: data.location,
        vehicle: data.vehicle,
      });
    } catch (error) {
      console.log("AI 예약 후보 조회 실패", error);
      addAiMessage(error.response?.data?.message || "AI 예약 후보 조회 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const sendChatMessage = async (messageText) => {
    const text = messageText.trim();
    console.log("AI 채팅 전송 실행", text);

    if (!text) {
      alert("메시지를 입력해 주세요.");
      return;
    }

    const userMessage = {
      messageId: Date.now(),
      senderType: "USER",
      message: text,
      createdAt: new Date().toLocaleString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage("");

    if (isReservationRequest(text)) {
      await prepareAiReservation({ messageText: text, sort: resolveSortFromText(text) });
      return;
    }

    setLoading(true);

    try {
      const response = await aiApi.sendMessage({ message: text });
      console.log("AI 채팅 응답", response);

      const data = response.data;
      addAiMessage(
        data.message ||
          data.answer ||
          "요청을 분석했습니다. 백엔드 AI 응답 형식에 맞춰 message 또는 answer 필드를 연결하세요.",
        {
          intent: data.intent,
          location: data.location,
          reservations: Array.isArray(data.reservations) ? data.reservations : [],
        }
      );
    } catch (error) {
      console.log("AI 채팅 전송 실패", error);
      addAiMessage(
        error.response?.data?.message ||
          "AI 처리 중 오류가 발생했습니다. 로그인 상태, Gemini API Key, Redis 실행 여부를 확인해 주세요."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSortButton = async (sort) => {
    console.log("AI 예약 정렬 버튼 클릭", sort);

    const label = sortLabels[sort];
    const userMessage = {
      messageId: Date.now(),
      senderType: "USER",
      message: `${label}으로 예약 후보 보여줘`,
      createdAt: new Date().toLocaleString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    await prepareAiReservation({ messageText: `${label} 예약 후보`, sort });
  };

  const confirmAiReservation = async (candidateNo) => {
    console.log("AI 예약 확정", candidateNo);

    if (!validateReservationCondition()) {
      return;
    }

    if (!window.confirm(`${candidateNo}번 후보로 실제 예약을 진행할까요?`)) {
      return;
    }

    setLoading(true);

    try {
      const response = await aiApi.confirmReservation({
        candidateNo,
        reservationDate: reservationForm.reservationDate,
        startTime: reservationForm.startTime,
        currentSoc: Number(reservationForm.currentSoc),
        targetSoc: Number(reservationForm.targetSoc),
      });

      const data = response.data;
      addAiMessage(data.message || "AI 예약이 완료되었습니다.", {
        intent: "AI_RESERVATION_CONFIRM",
      });

      setReservationCandidates([]);

      if (data.reservationId && window.confirm("예약 완료 화면으로 이동할까요?")) {
        navigate(`/reservation/complete?reservationId=${data.reservationId}`);
      }
    } catch (error) {
      console.log("AI 예약 확정 실패", error);
      addAiMessage(error.response?.data?.message || "AI 예약 확정 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const moveLocationToMap = (location) => {
    console.log("AI 위치 지도 보기", location);

    const latitude = valueOf(location, "latitude", "locationLatitude", "location_latitude");
    const longitude = valueOf(location, "longitude", "locationLongitude", "location_longitude");
    const name = valueOf(location, "locationName", "location_name") || "내 위치";
    const address = valueOf(location, "address", "locationAddress", "location_address");

    if (!latitude || !longitude) {
      alert("지도에서 볼 수 있는 좌표가 없습니다.");
      return;
    }

    navigate(buildStationMapUrl({ latitude, longitude, name, address, focusType: "origin" }));
  };

  const moveStationToMap = (station) => {
    console.log("AI 충전소 지도 보기", station);

    const stationId = valueOf(station, "stationId", "station_id");
    const latitude = valueOf(station, "latitude");
    const longitude = valueOf(station, "longitude");
    const name = valueOf(station, "stationName", "station_name");
    const address = valueOf(station, "address", "stationAddress", "station_address");

    if (!stationId && (!latitude || !longitude)) {
      alert("지도에서 볼 수 있는 충전소 정보가 없습니다.");
      return;
    }

    navigate(buildStationMapUrl({ stationId, latitude, longitude, name, address, focusType: "station" }));
  };

  const moveReservationList = () => {
    console.log("내 예약 화면 이동");
    navigate("/my-reservations");
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendChatMessage(inputMessage);
  };

  const handleQuickQuestion = (question) => {
    console.log("빠른 질문 클릭", question);
    sendChatMessage(question);
  };

  const handleReservationFormChange = (e) => {
    const { name, value } = e.target;
    console.log("AI 예약 조건 변경", name, value);

    setReservationForm((prev) => {
      const next = { ...prev, [name]: value };

      // 오늘 날짜에서 과거 시간이 선택되면 현재 시각 이후로 보정한다.
      if (name === "reservationDate" && value === getTodayText()) {
        const selectedDateTime = buildDateTime(value, next.startTime);
        if (selectedDateTime && selectedDateTime.getTime() < Date.now()) {
          next.startTime = getNextHourText();
        }
      }

      return next;
    });
  };

  const handleClear = async () => {
    console.log("AI 채팅 초기화 클릭");

    const confirmClear = window.confirm(
      "AI 대화 내용을 모두 초기화하시겠습니까?\nDB에 저장된 대화 이력과 Redis 예약 후보가 삭제되며 복구할 수 없습니다."
    );
    if (!confirmClear) {
      return;
    }

    try {
      await aiApi.clearMessages();
    } catch (error) {
      console.log("AI 채팅 이력 초기화 실패 - 화면만 초기화", error);
      alert("서버 대화 이력 초기화에 실패했습니다. 화면만 초기화합니다.");
    }

    setReservationCandidates([]);
    setMessages(guideMessages);
  };

  const minTime = reservationForm.reservationDate === getTodayText() ? getCurrentTimeText() : undefined;

  return (
    <section className="ai-chat-page">
      <div className="ai-chat-inner">
        <div className="ai-chat-hero compact">
          <div>
            <p>AI Charging Assistant</p>
            <h1>AI 충전 비서</h1>
            <span>
              대표 차량, 기본 출발지, 예약 가능 시간, FAQ/공지/민원 데이터를 활용해 충전소 추천과 예약을 도와줍니다.
            </span>
          </div>
          <button type="button" onClick={handleClear}>
            대화 초기화
          </button>
        </div>

        <div className="ai-chat-layout chat-window-layout">
          <aside className="ai-guide-panel ai-guide-panel-compact">
            <div className="ai-reservation-box ai-reservation-box-top">
              <strong>AI 예약 조건</strong>
              <label>
                예약 날짜
                <input
                  type="date"
                  name="reservationDate"
                  min={getTodayText()}
                  value={reservationForm.reservationDate}
                  onChange={handleReservationFormChange}
                />
              </label>
              <label>
                시작 시간
                <input
                  type="time"
                  name="startTime"
                  min={minTime}
                  value={reservationForm.startTime}
                  onChange={handleReservationFormChange}
                />
              </label>
              <div className="ai-soc-row">
                <label>
                  현재 SOC
                  <input
                    type="number"
                    name="currentSoc"
                    min="0"
                    max="100"
                    value={reservationForm.currentSoc}
                    onChange={handleReservationFormChange}
                  />
                </label>
                <label>
                  목표 SOC
                  <input
                    type="number"
                    name="targetSoc"
                    min="1"
                    max="100"
                    value={reservationForm.targetSoc}
                    onChange={handleReservationFormChange}
                  />
                </label>
              </div>

              <div className="ai-sort-box">
                <span>예약 후보 정렬</span>
                <div>
                  <button type="button" className={activeSort === "DISTANCE" ? "active" : ""} onClick={() => handleSortButton("DISTANCE")}>
                    가까운순
                  </button>
                  <button type="button" className={activeSort === "COST" ? "active" : ""} onClick={() => handleSortButton("COST")}>
                    가격순
                  </button>
                  <button type="button" className={activeSort === "SPEED" ? "active" : ""} onClick={() => handleSortButton("SPEED")}>
                    충전빠른순
                  </button>
                </div>
              </div>
            </div>

            <h2>빠른 질문</h2>

            <div className="quick-question-list">
              {quickQuestions.map((question) => (
                <button type="button" key={question} onClick={() => handleQuickQuestion(question)}>
                  {question}
                </button>
              ))}
            </div>

            <div className="ai-info-box">
              <strong>연동 데이터</strong>
              <ul>
                <li>DB 기본 출발지 기준 내 위치</li>
                <li>대표 차량 / 커넥터 타입</li>
                <li>사용가능 충전기 / 예약 시간 중복 제외</li>
                <li>공지사항 / FAQ / 내 민원 RAG</li>
                <li>내 예약 내역 / 지도 이동 버튼</li>
                <li>Redis AI 대화 캐시 / 예약 후보 TTL</li>
              </ul>
            </div>
          </aside>

          <div className="ai-chat-card ai-chat-window-card">
            <div className="chat-window-header">
              <div>
                <strong>AI 대화창</strong>
                <span>AI 답변은 DB에 저장된 사용자 차량, 기본 출발지, 충전소 데이터를 우선 기준으로 사용합니다.</span>
              </div>
              {reservationCandidates.length > 0 && <em>{reservationCandidates.length}개 후보 조회됨</em>}
            </div>

            <div className="chat-message-list chat-window-message-list">
              {messages.map((item) => (
                <div key={item.messageId} className={item.senderType === "USER" ? "chat-message user" : "chat-message ai"}>
                  <div className="chat-bubble">
                    {item.intent && <span className="intent-badge">{item.intent}</span>}
                    <p>{item.message}</p>

                    {item.location && (valueOf(item.location, "latitude", "locationLatitude", "location_latitude") || valueOf(item.location, "longitude", "locationLongitude", "location_longitude")) && (
                      <div className="ai-action-row">
                        <button type="button" className="ai-map-action-btn" onClick={() => moveLocationToMap(item.location)}>
                          지도에서 내 위치 보기
                        </button>
                      </div>
                    )}

                    {Array.isArray(item.candidates) && item.candidates.length > 0 && (
                      <div className="ai-candidate-list">
                        {item.candidates.map((candidate) => (
                          <div className="ai-candidate-card" key={`${candidate.candidateNo}-${candidate.chargerId}`}>
                            <strong>
                              {candidate.candidateNo}번. {candidate.stationName}
                            </strong>
                            <span>거리 {Number(candidate.distanceKm || 0).toFixed(2)}km · {candidate.chargerName}</span>
                            <span>{candidate.connectorType} · 출력 {candidate.chargingSpeedKw}kW · 적용 {candidate.effectiveChargingSpeedKw || candidate.chargingSpeedKw}kW</span>
                            <span>요금 {Number(candidate.pricePerKwh || 0).toLocaleString()}원/kWh</span>
                            <span>예상 {candidate.estimatedMinutes}분 · {Number(candidate.estimatedCost || 0).toLocaleString()}원</span>
                            <div className="ai-card-button-row">
                              <button type="button" onClick={() => confirmAiReservation(candidate.candidateNo)}>
                                이 후보로 예약
                              </button>
                              <button type="button" className="secondary" onClick={() => moveStationToMap(candidate)}>
                                지도에서 보기
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}

                    {Array.isArray(item.reservations) && item.reservations.length > 0 && (
                      <div className="ai-reservation-result-list">
                        {item.reservations.map((reservation) => (
                          <div className="ai-reservation-result-card" key={valueOf(reservation, "reservationId", "reservation_id")}>
                            <strong>{valueOf(reservation, "stationName", "station_name")}</strong>
                            <span>예약시간 {valueOf(reservation, "startTime", "start_time")} ~ {valueOf(reservation, "endTime", "end_time")}</span>
                            <span>충전기 {valueOf(reservation, "chargerName", "charger_name")} · {valueOf(reservation, "connectorType", "connector_type")}</span>
                            <span>차량 {valueOf(reservation, "manufacturer")} {valueOf(reservation, "modelName", "model_name")} · 상태 {valueOf(reservation, "status")}</span>
                            <div className="ai-card-button-row">
                              <button type="button" className="secondary" onClick={() => moveStationToMap(reservation)}>
                                지도에서 보기
                              </button>
                              <button type="button" onClick={moveReservationList}>
                                내 예약 화면
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}

                    <small>{item.createdAt}</small>
                  </div>
                </div>
              ))}

              {loading && (
                <div className="chat-message ai">
                  <div className="chat-bubble loading">
                    <p>AI가 DB와 Redis 데이터를 조회하고 답변을 생성하고 있습니다...</p>
                  </div>
                </div>
              )}

              {reservationCandidates.length > 0 && (
                <div className="chat-message ai">
                  <div className="chat-bubble reservation-help">
                    <p>후보 카드의 “이 후보로 예약” 버튼을 누르면 Redis에 저장된 후보 기준으로 실제 예약이 생성됩니다.</p>
                  </div>
                </div>
              )}

              <div ref={bottomRef} />
            </div>

            <form className="chat-input-form chat-window-input-form" onSubmit={handleSubmit}>
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder="예: 오늘 18시에 예약 가능한 급속 충전소 찾아줘"
              />
              <button type="submit" disabled={loading}>
                전송
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  );
};

export default AiChatPage;
