package com.ev.dao.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.chat.EvAiChargeInfoDTO;
import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatRoomDTO;
import com.ev.dto.chat.EvAiStationRecommendDTO;

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

    // 채팅방 메시지 전체 삭제
    int deleteMessagesByRoomId(@Param("roomId") Long roomId);

    // 최근 채팅 메시지 조회
    List<EvAiChatMessageDTO> findRecentMessagesByRoomId(Long roomId);

    // AI 챗봇용 주변 충전소 추천 조회
    List<EvAiStationRecommendDTO> findRecommendStations(Long memberId);

    // AI 챗봇용 충전 시간/비용 계산 정보 조회
    EvAiChargeInfoDTO findChargeCalculationInfo(Long memberId);

    // AI가 사용자의 기본 출발지를 답변하거나 예약 추천 기준으로 사용할 때 조회
    Map<String, Object> findDefaultLocationForAi(@Param("memberId") Long memberId);

    // AI가 사용자의 대표차량을 답변하거나 충전 계산 기준으로 사용할 때 조회
    Map<String, Object> findDefaultVehicleForAi(@Param("memberId") Long memberId);

    // AI 예약 후보 조회: 대표 차량 + 기본 출발지 + 예약 가능 시간 + 사용가능 충전기 기준
    List<Map<String, Object>> findAiReservationCandidates(@Param("memberId") Long memberId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("currentSoc") Integer currentSoc,
                                                          @Param("targetSoc") Integer targetSoc,
                                                          @Param("radiusMeter") Integer radiusMeter,
                                                          @Param("limit") Integer limit,
                                                          @Param("sort") String sort);

    // RAG용 공지사항 조회
    List<Map<String, Object>> findAiRagNotices(@Param("keyword") String keyword,
                                               @Param("limit") Integer limit);

    // RAG용 FAQ 조회
    List<Map<String, Object>> findAiRagFaqs(@Param("keyword") String keyword,
                                            @Param("limit") Integer limit);

    // RAG용 내 민원 조회
    List<Map<String, Object>> findAiRagComplaints(@Param("memberId") Long memberId,
                                                  @Param("keyword") String keyword,
                                                  @Param("limit") Integer limit);

    // AI 챗봇의 내 예약 조회용 최근 예약 목록
    List<Map<String, Object>> findMyReservationsForAi(@Param("memberId") Long memberId,
                                                       @Param("limit") Integer limit);

}
