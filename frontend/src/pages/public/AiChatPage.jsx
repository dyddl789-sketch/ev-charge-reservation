import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";

import * as aiApi from "../../apis/aiApi";
import "../../styles/ai-chat.css";

const pad = (value) => String(value).padStart(2, "0");

const today = () => {
  const date = new Date();
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const nextHour = () => {
  const date = new Date(Date.now() + 60 * 60 * 1000);
  return `${pad(date.getHours())}:00`;
};

const guideMessages = [
  {
    messageId: 1,
    senderType: "AI",
    message:
      "안녕하세요. AI 충전 비서입니다. 차량 정보와 충전소 데이터를 바탕으로 충전소 추천, 충전 시간 계산, 예약 가능한 충전기 후보 조회까지 도와드릴 수 있습니다.",
    createdAt: "안내 메시지",
  },
];

const quickQuestions = [
  "내 차량에 맞는 근처 충전소 찾아줘",
  "30%에서 80%까지 충전하면 얼마나 걸려?",
  "오늘 예약 가능한 급속 충전소 찾아줘",
  "가장 저렴한 충전소로 예약 후보 보여줘",
];

const isReservationRequest = (text) => {
  const value = text.replaceAll(" ", "");
  return value.includes("예약") || value.includes("예약가능") || value.includes("예약해줘");
};

const AiChatPage = () => {
  console.log("AiChatPage 렌더링");

  const navigate = useNavigate();
  const bottomRef = useRef(null);
  const [messages, setMessages] = useState(guideMessages);
  const [inputMessage, setInputMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [reservationCandidates, setReservationCandidates] = useState([]);
  const [reservationForm, setReservationForm] = useState({
    reservationDate: today(),
    startTime: nextHour(),
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
    setLoading(true);

    try {
      if (isReservationRequest(text)) {
        const response = await aiApi.prepareReservation({
          message: text,
          reservationDate: reservationForm.reservationDate,
          startTime: reservationForm.startTime,
          currentSoc: reservationForm.currentSoc,
          targetSoc: reservationForm.targetSoc,
        });

        const data = response.data;
        const candidates = Array.isArray(data.candidates) ? data.candidates : [];
        setReservationCandidates(candidates);

        addAiMessage(data.message || "선택한 날짜와 시간 기준으로 예약 가능한 충전기 후보를 조회했습니다.", {
          intent: "AI_RESERVATION_PREPARE",
          candidates,
        });
        return;
      }

      const response = await aiApi.sendMessage({ message: text });
      console.log("AI 채팅 응답", response);

      const data = response.data;

      addAiMessage(
        data.message ||
          data.answer ||
          "요청을 분석했습니다. 백엔드 AI 응답 형식에 맞춰 message 또는 answer 필드를 연결하세요.",
        { intent: data.intent }
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

  const confirmAiReservation = async (candidateNo) => {
    console.log("AI 예약 확정", candidateNo);

    if (!window.confirm(`${candidateNo}번 후보로 실제 예약을 진행할까요?`)) {
      return;
    }

    setLoading(true);

    try {
      const response = await aiApi.confirmReservation({
        candidateNo,
        reservationDate: reservationForm.reservationDate,
        startTime: reservationForm.startTime,
        currentSoc: reservationForm.currentSoc,
        targetSoc: reservationForm.targetSoc,
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
    setReservationForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleClear = async () => {
    console.log("AI 채팅 초기화 클릭");

    const confirmClear = window.confirm("AI 대화 이력을 초기화할까요?");

    if (!confirmClear) {
      return;
    }

    try {
      await aiApi.clearMessages();
    } catch (error) {
      console.log("AI 채팅 이력 초기화 실패 - 화면만 초기화", error);
    }

    setReservationCandidates([]);
    setMessages(guideMessages);
  };

  return (
    <section className="ai-chat-page">
      <div className="ai-chat-inner">
        <div className="ai-chat-hero compact">
          <div>
            <p>AI Charging Assistant</p>
            <h1>AI 충전 비서</h1>
            <span>
              차량 정보, 충전소 데이터, Redis 예약 후보 저장을 활용해 충전소 추천과 예약을 도와줍니다.
            </span>
          </div>
          <button type="button" onClick={handleClear}>
            대화 초기화
          </button>
        </div>

        <div className="ai-chat-layout chat-window-layout">
          <aside className="ai-guide-panel ai-guide-panel-compact">
            <h2>빠른 질문</h2>
            <p>
              예약 질문은 아래 예약 조건의 날짜와 시간을 기준으로 후보를 조회합니다.
            </p>

            <div className="quick-question-list">
              {quickQuestions.map((question) => (
                <button type="button" key={question} onClick={() => handleQuickQuestion(question)}>
                  {question}
                </button>
              ))}
            </div>

            <div className="ai-reservation-box">
              <strong>AI 예약 조건</strong>
              <label>
                예약 날짜
                <input
                  type="date"
                  name="reservationDate"
                  value={reservationForm.reservationDate}
                  onChange={handleReservationFormChange}
                />
              </label>
              <label>
                시작 시간
                <input
                  type="time"
                  name="startTime"
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
            </div>

            <div className="ai-info-box">
              <strong>연동 데이터</strong>
              <ul>
                <li>대표 차량 / 커넥터 타입</li>
                <li>충전소 / 충전기 상태</li>
                <li>예약 가능 충전기 후보</li>
                <li>Redis AI 대화 캐시 / 예약 후보 TTL</li>
              </ul>
            </div>
          </aside>

          <div className="ai-chat-card ai-chat-window-card">
            <div className="chat-window-header">
              <div>
                <strong>AI 대화창</strong>
                <span>채팅 내용은 이 영역 안에서만 스크롤됩니다.</span>
              </div>
              {reservationCandidates.length > 0 && <em>{reservationCandidates.length}개 후보 조회됨</em>}
            </div>

            <div className="chat-message-list chat-window-message-list">
              {messages.map((item) => (
                <div key={item.messageId} className={item.senderType === "USER" ? "chat-message user" : "chat-message ai"}>
                  <div className="chat-bubble">
                    {item.intent && <span className="intent-badge">{item.intent}</span>}
                    <p>{item.message}</p>

                    {Array.isArray(item.candidates) && item.candidates.length > 0 && (
                      <div className="ai-candidate-list">
                        {item.candidates.map((candidate) => (
                          <div className="ai-candidate-card" key={`${candidate.candidateNo}-${candidate.chargerId}`}>
                            <strong>
                              {candidate.candidateNo}번. {candidate.stationName}
                            </strong>
                            <span>{candidate.chargerName} · {candidate.connectorType} · {candidate.chargingSpeedKw}kW</span>
                            <span>요금 {Number(candidate.pricePerKwh || 0).toLocaleString()}원/kWh</span>
                            <button type="button" onClick={() => confirmAiReservation(candidate.candidateNo)}>
                              이 후보로 예약
                            </button>
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
                    <p>AI가 데이터를 조회하고 답변을 생성하고 있습니다...</p>
                  </div>
                </div>
              )}

              {reservationCandidates.length > 0 && (
                <div className="chat-message ai">
                  <div className="chat-bubble reservation-help">
                    <p>후보 카드의 “이 후보로 예약” 버튼을 누르면 실제 예약이 생성됩니다.</p>
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
