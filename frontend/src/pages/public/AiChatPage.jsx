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
  "내 차량 뭐야",
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

const AI_CHAT_CACHE_KEY = "ev_ai_chat_messages_v2";
const AI_CHAT_MAP_CONTEXT_KEY = "ev_ai_chat_map_origin_context_v1";
const AI_CHAT_LOCATION_REFRESH_KEY = "ev_ai_chat_location_refresh_v1";
const AI_CHAT_LOCATION_REFRESH_MAX_AGE_MS = 10 * 60 * 1000;

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

const parseJsonValue = (value, fallback) => {
  if (value === undefined || value === null || value === "") {
    return fallback;
  }

  if (typeof value !== "string") {
    return value;
  }

  try {
    return JSON.parse(value);
  } catch (error) {
    console.log("AI 메시지 JSON 파싱 실패", error);
    return fallback;
  }
};

const normalizeCreatedAt = (createdAt) => {
  if (!createdAt) {
    return new Date().toLocaleString();
  }

  if (Array.isArray(createdAt)) {
    const [year, month, day, hour = 0, minute = 0] = createdAt;
    return `${year}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}`;
  }

  return String(createdAt).replace("T", " ").slice(0, 16);
};

const normalizeChatMessage = (item) => {
  const candidates = parseJsonValue(item.candidates ?? item.candidatesJson, []);
  const reservations = parseJsonValue(item.reservations ?? item.reservationsJson, []);
  const location = parseJsonValue(item.location ?? item.locationJson, null);

  return {
    ...item,
    messageId: item.messageId || `${item.senderType || "AI"}-${Date.now()}-${Math.random()}`,
    senderType: item.senderType || item.sender_type || "AI",
    message: item.message || item.answer || "",
    intent: item.intent,
    actionType: item.actionType || item.action_type,
    buttonText: item.buttonText || item.button_text,
    actionUrl: item.actionUrl || item.action_url,
    location,
    candidates: Array.isArray(candidates) ? candidates : [],
    reservations: Array.isArray(reservations) ? reservations : [],
    createdAt: normalizeCreatedAt(item.createdAt || item.created_at),
  };
};

const loadCachedMessages = () => {
  if (typeof window === "undefined") {
    return guideMessages;
  }

  try {
    const cached = window.sessionStorage.getItem(AI_CHAT_CACHE_KEY);
    if (!cached) {
      return guideMessages;
    }

    const parsed = JSON.parse(cached);
    return Array.isArray(parsed) && parsed.length > 0 ? parsed.map(normalizeChatMessage) : guideMessages;
  } catch (error) {
    console.log("AI 채팅 세션 캐시 로딩 실패", error);
    return guideMessages;
  }
};

const mergeCachedMessageExtras = (serverMessages) => {
  const cachedMessages = loadCachedMessages();

  return serverMessages.map((serverMessage) => {
    const cachedMessage = cachedMessages.find(
      (cached) =>
        cached.senderType === serverMessage.senderType &&
        cached.message === serverMessage.message
    );

    if (!cachedMessage) {
      return serverMessage;
    }

    return {
      ...serverMessage,
      location: serverMessage.location || cachedMessage.location,
      candidates: serverMessage.candidates?.length > 0 ? serverMessage.candidates : cachedMessage.candidates || [],
      reservations: serverMessage.reservations?.length > 0 ? serverMessage.reservations : cachedMessage.reservations || [],
      actionType: serverMessage.actionType || cachedMessage.actionType,
      buttonText: serverMessage.buttonText || cachedMessage.buttonText,
      actionUrl: serverMessage.actionUrl || cachedMessage.actionUrl,
    };
  });
};

const saveAiMapContext = (context) => {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.sessionStorage.setItem(
      AI_CHAT_MAP_CONTEXT_KEY,
      JSON.stringify({
        ...context,
        createdAt: Date.now(),
      })
    );
  } catch (error) {
    console.log("AI 지도 이동 컨텍스트 저장 실패", error);
  }
};

const readPendingLocationRefresh = () => {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const rawValue = window.sessionStorage.getItem(AI_CHAT_LOCATION_REFRESH_KEY);

    if (!rawValue) {
      return null;
    }

    const parsed = JSON.parse(rawValue);
    const createdAt = Number(parsed.createdAt || 0);

    if (createdAt && Date.now() - createdAt > AI_CHAT_LOCATION_REFRESH_MAX_AGE_MS) {
      console.log("AI 위치 변경 재검색 요청 만료", parsed);
      window.sessionStorage.removeItem(AI_CHAT_LOCATION_REFRESH_KEY);
      window.sessionStorage.removeItem(AI_CHAT_MAP_CONTEXT_KEY);
      return null;
    }

    return parsed;
  } catch (error) {
    console.log("AI 위치 변경 재검색 요청 파싱 실패", error);
    window.sessionStorage.removeItem(AI_CHAT_LOCATION_REFRESH_KEY);
    return null;
  }
};

const clearPendingLocationRefresh = () => {
  if (typeof window === "undefined") {
    return;
  }

  window.sessionStorage.removeItem(AI_CHAT_LOCATION_REFRESH_KEY);
  window.sessionStorage.removeItem(AI_CHAT_MAP_CONTEXT_KEY);
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

const buildReservationCandidateMessage = (data, candidates) => {
  if (!Array.isArray(candidates) || candidates.length === 0) {
    return data?.message || "선택한 조건에 맞는 예약 후보를 찾지 못했습니다.";
  }

  const locationName =
    valueOf(data?.location, "locationName", "location_name") ||
    valueOf(data?.origin, "locationName", "location_name") ||
    "기본 출발지";

  return `${locationName} 기준으로 예약 가능한 충전소 후보 ${candidates.length}곳을 찾았습니다.\n거리, 요금, 예상시간은 아래 카드에서 비교해 주세요.`;
};

const AiChatPage = () => {
  console.log("AiChatPage 렌더링");

  const navigate = useNavigate();
  const messageListRef = useRef(null);
  const messageItemRefs = useRef({});
  const historyClearedRef = useRef(false);
  const locationRefreshHandledRef = useRef(null);
  const [messages, setMessages] = useState(loadCachedMessages);
  const [focusMessageId, setFocusMessageId] = useState(null);
  const [latestAnswerMessageId, setLatestAnswerMessageId] = useState(null);
  const [showLatestButton, setShowLatestButton] = useState(false);
  const [inputMessage, setInputMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [clearLoading, setClearLoading] = useState(false);
  const [reservationCandidates, setReservationCandidates] = useState([]);
  const [activeSort, setActiveSort] = useState("DISTANCE");
  const [reservationForm, setReservationForm] = useState({
    reservationDate: getTodayText(),
    startTime: getNextHourText(),
    currentSoc: 30,
    targetSoc: 80,
  });

  useEffect(() => {
    const initAiChatPage = async () => {
      await loadMessages();
      await checkPendingLocationRefresh();
    };

    initAiChatPage();
  }, []);

  useEffect(() => {
    const messageList = messageListRef.current;

    if (!messageList) {
      return undefined;
    }

    const handleScroll = () => {
      if (isMessageListNearBottom()) {
        setShowLatestButton(false);
      }
    };

    messageList.addEventListener("scroll", handleScroll);

    return () => {
      messageList.removeEventListener("scroll", handleScroll);
    };
  }, []);

  useEffect(() => {
    if (!focusMessageId) {
      return;
    }

    const timer = window.setTimeout(() => {
      scrollToMessageStart(focusMessageId);
      setFocusMessageId(null);
    }, 80);

    return () => window.clearTimeout(timer);
  }, [messages, focusMessageId]);

  useEffect(() => {
    if (!loading || !isMessageListNearBottom()) {
      return;
    }

    const messageList = messageListRef.current;

    if (!messageList) {
      return;
    }

    window.requestAnimationFrame(() => {
      messageList.scrollTop = messageList.scrollHeight;
    });
  }, [loading]);

  useEffect(() => {
    console.log("AI 채팅 세션 캐시 저장");

    try {
      window.sessionStorage.setItem(AI_CHAT_CACHE_KEY, JSON.stringify(messages));
    } catch (error) {
      console.log("AI 채팅 세션 캐시 저장 실패", error);
    }
  }, [messages]);

  const isMessageListNearBottom = () => {
    const messageList = messageListRef.current;

    if (!messageList) {
      return true;
    }

    const distanceFromBottom = messageList.scrollHeight - messageList.scrollTop - messageList.clientHeight;
    return distanceFromBottom < 140;
  };

  const scrollToMessageStart = (messageId) => {
    console.log("AI 최신 답변 시작 위치로 이동", messageId);

    const targetElement = messageItemRefs.current[messageId];

    if (!targetElement) {
      return;
    }

    targetElement.scrollIntoView({
      block: "start",
      behavior: "smooth",
    });
  };

  const moveToLatestAnswer = () => {
    console.log("최신 AI 답변으로 이동", latestAnswerMessageId);

    if (latestAnswerMessageId) {
      scrollToMessageStart(latestAnswerMessageId);
    } else if (messageListRef.current) {
      messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
    }

    setShowLatestButton(false);
  };

  const loadMessages = async () => {
    console.log("AI 채팅 이력 로딩");

    try {
      const response = await aiApi.getMessages();
      console.log("AI 채팅 이력 응답", response);

      const data = response.data;

      if (historyClearedRef.current) {
        console.log("AI 채팅 이력 로딩 응답 무시 - 초기화 직후");
        return;
      }

      if (Array.isArray(data) && data.length > 0) {
        setMessages(mergeCachedMessageExtras(data.map(normalizeChatMessage)));
      } else {
        setMessages(loadCachedMessages());
      }
    } catch (error) {
      console.log("AI 채팅 이력 로딩 실패 - 세션 캐시 또는 기본 안내 메시지 사용", error);
      setMessages(loadCachedMessages());
    }
  };

  const addAiMessage = (message, extra = {}) => {
    const { forceFocus = false, ...messageExtra } = extra;
    const messageId = Date.now() + Math.floor(Math.random() * 1000);
    const shouldFocusMessage = forceFocus || isMessageListNearBottom();

    const aiMessage = {
      messageId,
      senderType: "AI",
      message,
      createdAt: new Date().toLocaleString(),
      ...messageExtra,
    };

    setMessages((prev) => [...prev, aiMessage]);
    setLatestAnswerMessageId(messageId);

    if (shouldFocusMessage) {
      setFocusMessageId(messageId);
      setShowLatestButton(false);
    } else {
      setShowLatestButton(true);
    }
  };

  const checkPendingLocationRefresh = async () => {
    const refreshRequest = readPendingLocationRefresh();

    if (!refreshRequest) {
      return;
    }

    const refreshKey = refreshRequest.createdAt || refreshRequest.requestId;

    if (locationRefreshHandledRef.current === refreshKey) {
      return;
    }

    locationRefreshHandledRef.current = refreshKey;
    clearPendingLocationRefresh();

    const context = refreshRequest.context || {};
    const location = refreshRequest.location || {};
    const locationName = location.locationName || context.locationName || "변경한 출발지";
    const nextSort = context.sort || activeSort || "DISTANCE";
    const nextReservationForm = context.reservationForm || reservationForm;

    console.log("지도 출발지 변경 후 AI 자동 재검색 실행", refreshRequest);

    if (context.refreshMode !== "RESERVATION_CANDIDATES") {
      addAiMessage(`출발지가 “${locationName}”로 변경되었습니다. 충전소 추천을 다시 요청하면 새 위치 기준으로 조회하겠습니다.`, {
        intent: "AI_LOCATION_CHANGED",
        forceFocus: true,
      });
      return;
    }

    setReservationForm((prev) => ({
      ...prev,
      ...nextReservationForm,
    }));

    addAiMessage(`출발지가 “${locationName}”로 변경되었습니다. 새 출발지 기준으로 예약 가능한 충전소 후보를 다시 조회하겠습니다.`, {
      intent: "AI_LOCATION_REFRESH",
      forceFocus: true,
    });

    await prepareAiReservation({
      messageText: `${locationName} 기준으로 예약 가능한 충전소 후보 다시 보여줘`,
      sort: nextSort,
      reservationCondition: nextReservationForm,
    });
  };

  const validateReservationCondition = (condition = reservationForm) => {
    console.log("AI 예약 조건 검증", condition);

    const startDateTime = buildDateTime(condition.reservationDate, condition.startTime);
    const now = new Date();

    if (!startDateTime) {
      alert("예약 날짜와 시작 시간을 선택해 주세요.");
      return false;
    }

    if (startDateTime.getTime() < now.getTime()) {
      alert("현재 시간보다 이전 시간으로 AI 예약을 진행할 수 없습니다.");
      return false;
    }

    const currentSoc = Number(condition.currentSoc);
    const targetSoc = Number(condition.targetSoc);

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

  const prepareAiReservation = async ({ messageText, sort, reservationCondition = reservationForm }) => {
    console.log("AI 예약 후보 조회 실행", messageText, sort, reservationCondition);

    if (!validateReservationCondition(reservationCondition)) {
      return;
    }

    setLoading(true);

    try {
      const response = await aiApi.prepareReservation({
        message: messageText,
        sort,
        reservationDate: reservationCondition.reservationDate,
        startTime: reservationCondition.startTime,
        currentSoc: Number(reservationCondition.currentSoc),
        targetSoc: Number(reservationCondition.targetSoc),
      });

      const data = response.data;
      console.log("AI 예약 후보 조회 응답", data);

      const candidates = Array.isArray(data.candidates) ? data.candidates : [];
      setReservationCandidates(candidates);
      setActiveSort(data.sort || sort);

      addAiMessage(buildReservationCandidateMessage(data, candidates), {
        intent: "AI_RESERVATION_PREPARE",
        candidates,
        location: data.location,
        vehicle: data.vehicle,
      });
    } catch (error) {
      console.log("AI 예약 후보 조회 실패", error);
      const errorData = error.response?.data || {};
      addAiMessage(errorData.message || "AI 예약 후보 조회 중 오류가 발생했습니다.", {
        actionType: errorData.actionType,
        buttonText: errorData.buttonText,
        actionUrl: errorData.actionUrl,
      });
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
          candidates: Array.isArray(data.candidates) ? data.candidates : [],
          reservations: Array.isArray(data.reservations) ? data.reservations : [],
          actionType: data.actionType,
          buttonText: data.buttonText,
          actionUrl: data.actionUrl,
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

    const hasReservationCandidateContext =
      reservationCandidates.length > 0 ||
      messages.some((messageItem) => Array.isArray(messageItem.candidates) && messageItem.candidates.length > 0);

    // 지도에서 출발지를 바꾼 뒤 AI 화면으로 돌아오면
    // 변경된 기본 출발지 기준으로 후보를 자동 재조회하기 위한 컨텍스트를 저장한다.
    saveAiMapContext({
      refreshMode: hasReservationCandidateContext ? "RESERVATION_CANDIDATES" : "LOCATION_ONLY",
      sort: activeSort,
      reservationForm,
      locationName: name,
      source: "AI_LOCATION_VIEW",
    });

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

    const hasReservationCandidateContext =
      reservationCandidates.length > 0 ||
      messages.some((messageItem) => Array.isArray(messageItem.candidates) && messageItem.candidates.length > 0);

    // 후보 카드에서 지도 화면으로 이동한 경우에도 출발지 변경 후 AI 재검색이 가능해야 한다.
    saveAiMapContext({
      refreshMode: hasReservationCandidateContext ? "RESERVATION_CANDIDATES" : "LOCATION_ONLY",
      sort: activeSort,
      reservationForm,
      locationName: name,
      source: "AI_STATION_VIEW",
    });

    navigate(buildStationMapUrl({ stationId, latitude, longitude, name, address, focusType: "station" }));
  };

  const moveReservationList = () => {
    console.log("내 예약 화면 이동");
    navigate("/my-reservations");
  };

  const moveReservationDetail = (reservation) => {
    const reservationId = valueOf(reservation, "reservationId", "reservation_id");
    console.log("내 예약 상세 화면 이동", reservationId);

    if (!reservationId) {
      alert("예약 상세로 이동할 예약번호가 없습니다.");
      return;
    }

    navigate(`/my-reservations/${reservationId}`);
  };

  const moveAiAction = (item) => {
    console.log("AI 액션 버튼 클릭", item);

    if (item?.actionUrl) {
      navigate(item.actionUrl);
      return;
    }

    if (item?.actionType === "VEHICLE_REGISTER") {
      navigate("/vehicles/register");
      return;
    }

    if (item?.actionType === "MY_RESERVATION_HISTORY") {
      navigate("/my-reservations");
      return;
    }

    if (item?.actionType === "LOCATION_REGISTER") {
      navigate("/stations");
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

    historyClearedRef.current = true;
    setClearLoading(true);
    setReservationCandidates([]);
    window.sessionStorage.removeItem(AI_CHAT_CACHE_KEY);
    setMessages(guideMessages);

    try {
      const response = await aiApi.clearMessages();
      console.log("AI 채팅 이력 초기화 응답", response.data);
    } catch (error) {
      console.log("AI 채팅 이력 초기화 실패 - 화면만 초기화", error);
      alert("서버 대화 이력 초기화에 실패했습니다. 화면은 초기화했습니다.");
    } finally {
      setClearLoading(false);
    }
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
          <button type="button" onClick={handleClear} disabled={clearLoading}>
            {clearLoading ? "초기화 중..." : "대화 초기화"}
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

            <div ref={messageListRef} className="chat-message-list chat-window-message-list">
              {messages.map((item) => (
                <div
                  key={item.messageId}
                  ref={(element) => {
                    if (element) {
                      messageItemRefs.current[item.messageId] = element;
                    } else {
                      delete messageItemRefs.current[item.messageId];
                    }
                  }}
                  className={item.senderType === "USER" ? "chat-message user" : "chat-message ai"}
                >
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

                    {item.actionType && item.buttonText && (
                      <div className="ai-action-row">
                        <button type="button" className="ai-map-action-btn" onClick={() => moveAiAction(item)}>
                          {item.buttonText}
                        </button>
                      </div>
                    )}

                    {Array.isArray(item.candidates) && item.candidates.length > 0 && (
                      <div className="ai-candidate-list">
                        {item.candidates.map((candidate) => {
                          const distanceKm = Number(candidate.distanceKm || 0).toFixed(2);
                          const pricePerKwh = Number(candidate.pricePerKwh || 0).toLocaleString();
                          const estimatedMinutes = Number(candidate.estimatedMinutes || 0).toLocaleString();
                          const estimatedCost = Number(candidate.estimatedCost || 0).toLocaleString();
                          const effectiveSpeed = candidate.effectiveChargingSpeedKw || candidate.chargingSpeedKw;

                          return (
                            <div className="ai-candidate-card refined" key={`${candidate.candidateNo}-${candidate.chargerId}`}>
                              <div className="ai-candidate-head">
                                <span className="ai-candidate-rank">추천 {candidate.candidateNo}</span>
                                <div>
                                  <strong>{candidate.stationName}</strong>
                                  <em>{candidate.chargerName}</em>
                                </div>
                              </div>

                              <div className="ai-candidate-metrics">
                                <div>
                                  <span>거리</span>
                                  <strong>{distanceKm}km</strong>
                                </div>
                                <div>
                                  <span>요금</span>
                                  <strong>{pricePerKwh}원/kWh</strong>
                                </div>
                                <div>
                                  <span>예상시간</span>
                                  <strong>{estimatedMinutes}분</strong>
                                </div>
                              </div>

                              <div className="ai-candidate-sub-info">
                                <span>{candidate.connectorType}</span>
                                <span>출력 {candidate.chargingSpeedKw}kW</span>
                                <span>적용 {effectiveSpeed}kW</span>
                                <span>예상비용 {estimatedCost}원</span>
                              </div>

                              <div className="ai-card-button-row">
                                <button type="button" onClick={() => confirmAiReservation(candidate.candidateNo)}>
                                  이 후보로 예약
                                </button>
                                <button type="button" className="secondary" onClick={() => moveStationToMap(candidate)}>
                                  지도에서 보기
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}

                    {Array.isArray(item.reservations) && item.reservations.length > 0 && (
                      <div className="ai-reservation-result-list">
                        {item.reservations.map((reservation) => {
                          const reservationId = valueOf(reservation, "reservationId", "reservation_id");
                          const stationName = valueOf(reservation, "stationName", "station_name");
                          const startTime = valueOf(reservation, "startTime", "start_time");
                          const endTime = valueOf(reservation, "endTime", "end_time");
                          const chargerName = valueOf(reservation, "chargerName", "charger_name");
                          const connectorType = valueOf(reservation, "connectorType", "connector_type");
                          const status = valueOf(reservation, "status");

                          return (
                            <div className="ai-reservation-result-card refined" key={reservationId}>
                              <div className="ai-reservation-result-head">
                                <div>
                                  <strong>{stationName}</strong>
                                  <span>예약번호 {reservationId}</span>
                                </div>
                                <em>{status}</em>
                              </div>

                              <div className="ai-reservation-summary-grid">
                                <div>
                                  <span>예약시간</span>
                                  <strong>{startTime} ~ {endTime}</strong>
                                </div>
                                <div>
                                  <span>충전기</span>
                                  <strong>{chargerName}</strong>
                                </div>
                                <div>
                                  <span>커넥터</span>
                                  <strong>{connectorType}</strong>
                                </div>
                              </div>

                              <div className="ai-card-button-row">
                                <button type="button" onClick={() => moveReservationDetail(reservation)}>
                                  예약 상세
                                </button>
                                <button type="button" className="secondary" onClick={moveReservationList}>
                                  내 예약 내역
                                </button>
                              </div>
                            </div>
                          );
                        })}
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

            </div>

            {showLatestButton && (
              <button type="button" className="chat-latest-button" onClick={moveToLatestAnswer}>
                최신 답변으로 이동 ↓
              </button>
            )}

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
