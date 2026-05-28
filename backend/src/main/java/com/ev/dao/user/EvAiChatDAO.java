package com.ev.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatRoomDTO;

@Mapper
public interface EvAiChatDAO {

    // 회원의 최근 AI 채팅방 조회
    EvAiChatRoomDTO findRoomByMemberId(Long memberId);

    // AI 채팅방 생성
    void insertRoom(EvAiChatRoomDTO roomDTO);

    // AI 채팅 메시지 저장
    void insertMessage(EvAiChatMessageDTO messageDTO);

    // 채팅방 메시지 목록 조회
    List<EvAiChatMessageDTO> findMessagesByRoomId(Long roomId);
    
 // 최근 채팅 메시지 조회
    List<EvAiChatMessageDTO> findRecentMessagesByRoomId(Long roomId);

}