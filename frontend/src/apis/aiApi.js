import api from "./api";

// AI 채팅 메시지 전송
export const sendMessage = (messageData) => {
  console.log("AI 채팅 메시지 전송 요청", messageData);
  return api.post("/ai-chat/message", messageData);
};

// AI 채팅방 조회
export const getRoom = () => {
  console.log("AI 채팅방 조회 요청");
  return api.get("/ai-chat/room");
};

// AI 채팅 이력 조회
export const getMessages = () => {
  console.log("AI 채팅 이력 조회 요청");
  return api.get("/ai-chat/messages");
};

// AI 채팅 이력 초기화
export const clearMessages = () => {
  console.log("AI 채팅 이력 초기화 요청");
  return api.delete("/ai-chat/messages");
};
