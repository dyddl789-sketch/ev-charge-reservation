import { useEffect, useRef, useState } from "react";

import * as aiApi from "../../apis/aiApi";
import "../../styles/ai-chat.css";

const mockMessages = [
  {
    messageId: 1,
    senderType: "AI",
    message:
      "안녕하세요. AI 충전 비서입니다. 충전소 추천, 예상 충전 시간, 예상 비용을 도와드릴 수 있습니다.",
    createdAt: "2026-06-17 10:00",
  },
];

const quickQuestions = [
  "내 차량에 맞는 근처 충전소 찾아줘",
  "30%에서 80%까지 충전하면 얼마나 걸려?",
  "오늘 예약 가능한 급속 충전소 알려줘",
  "충전 비용을 예상해줘",
];

const AiChatPage = () => {
  console.log("AiChatPage 렌더링");

  const bottomRef = useRef(null);
  const [messages, setMessages] = useState(mockMessages);
  const [inputMessage, setInputMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadMessages();
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

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
      console.log("AI 채팅 이력 로딩 실패 - 목업 데이터 사용", error);
      setMessages(mockMessages);
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
    setLoading(true);

    try {
      const response = await aiApi.sendMessage({ message: text });
      console.log("AI 채팅 응답", response);

      const data = response.data;

      const aiMessage = {
        messageId: data.messageId || Date.now() + 1,
        senderType: "AI",
        message:
          data.message ||
          data.answer ||
          "요청을 분석했습니다. 백엔드 AI 응답 형식에 맞춰 message 또는 answer 필드를 연결하세요.",
        intent: data.intent,
        createdAt: data.createdAt || new Date().toLocaleString(),
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      console.log("AI 채팅 전송 실패 - 임시 응답 표시", error);

      const fallbackMessage = {
        messageId: Date.now() + 1,
        senderType: "AI",
        message:
          "현재 백엔드 AI API 연결 전입니다. 화면 확인용 임시 응답입니다. 실제 연결 후 Gemini 기반 답변이 표시됩니다.",
        createdAt: new Date().toLocaleString(),
      };

      setMessages((prev) => [...prev, fallbackMessage]);
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

    setMessages(mockMessages);
  };

  return (
    <section className="ai-chat-page">
      <div className="ai-chat-inner">
        <div className="ai-chat-hero">
          <div>
            <p>AI Charging Assistant</p>
            <h1>AI 충전 비서</h1>
            <span>
              차량 정보와 충전소 데이터를 바탕으로 충전소 추천, 충전 시간, 충전 비용을 안내합니다.
            </span>
          </div>
          <button type="button" onClick={handleClear}>
            대화 초기화
          </button>
        </div>

        <div className="ai-chat-layout">
          <aside className="ai-guide-panel">
            <h2>무엇을 도와드릴까요?</h2>
            <p>
              자연어로 질문하면 Intent 분석 후 충전소 추천, 시간 계산, 비용 계산 흐름으로 처리합니다.
            </p>

            <div className="quick-question-list">
              {quickQuestions.map((question) => (
                <button
                  type="button"
                  key={question}
                  onClick={() => handleQuickQuestion(question)}
                >
                  {question}
                </button>
              ))}
            </div>

            <div className="ai-info-box">
              <strong>연동 예정 데이터</strong>
              <ul>
                <li>대표 차량 배터리 용량</li>
                <li>커넥터 타입</li>
                <li>저장 위치 / 주변 충전소</li>
                <li>충전기 출력 / 요금</li>
              </ul>
            </div>
          </aside>

          <div className="ai-chat-card">
            <div className="chat-message-list">
              {messages.map((item) => (
                <div
                  key={item.messageId}
                  className={
                    item.senderType === "USER"
                      ? "chat-message user"
                      : "chat-message ai"
                  }
                >
                  <div className="chat-bubble">
                    {item.intent && <span className="intent-badge">{item.intent}</span>}
                    <p>{item.message}</p>
                    <small>{item.createdAt}</small>
                  </div>
                </div>
              ))}

              {loading && (
                <div className="chat-message ai">
                  <div className="chat-bubble loading">
                    <p>AI가 답변을 생성하고 있습니다...</p>
                  </div>
                </div>
              )}

              <div ref={bottomRef} />
            </div>

            <form className="chat-input-form" onSubmit={handleSubmit}>
              <input
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                placeholder="예: 30%에서 80%까지 충전하면 얼마나 걸려?"
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
