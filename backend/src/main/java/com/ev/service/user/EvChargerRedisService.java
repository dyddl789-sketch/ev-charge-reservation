package com.ev.service.user;

/*
 * 충전기 Redis 관리 Service
 *
 * Redis에서 관리할 데이터:
 * 1. 충전기별 임시 인증코드
 * 2. 충전기 현재 상태
 * 3. 예약 인증 실패 횟수
 * 4. 예약 입력 중 충전기 임시 점유
 */
public interface EvChargerRedisService {

    /*
     * 충전기 인증코드 생성
     *
     * chargerId 기준으로 6자리 인증코드를 생성하고
     * Redis에 TTL과 함께 저장한다.
     */
    String generateAuthCode(Long chargerId);

    /*
     * 예약별 인증코드 생성
     * 예약 생성 시점에 코드를 만들고, 예약 시작 + 5분까지 Redis TTL을 유지한다.
     */
    String generateReservationAuthCode(Long reservationId, Long chargerId, java.time.LocalDateTime expiresAt);

    /*
     * 충전기 인증코드 조회
     *
     * Redis에 저장된 현재 인증코드를 조회한다.
     */
    String getAuthCode(Long chargerId);

    /* 예약별 인증코드 조회 */
    String getReservationAuthCode(Long reservationId);

    /*
     * 충전기 인증코드 삭제
     *
     * 인증 성공 후 재사용을 막고 싶을 때 사용한다.
     */
    void deleteAuthCode(Long chargerId);

    /* 예약별 인증코드 삭제 */
    void deleteReservationAuthCode(Long reservationId);

    /*
     * 충전기 현재 상태 저장
     *
     * 예:
     * AVAILABLE
     * RESERVED
     * VERIFIED
     * CHARGING
     * OFFLINE
     */
    void setChargerStatus(Long chargerId, String status);

    /*
     * 충전기 현재 상태 조회
     */
    String getChargerStatus(Long chargerId);

    /*
     * 충전기 현재 상태 삭제
     */
    void deleteChargerStatus(Long chargerId);

    /*
     * 인증 실패 횟수 증가
     *
     * memberId + reservationId 기준으로 실패 횟수를 관리한다.
     */
    Long increaseVerifyAttempt(Long memberId, Long reservationId);

    /*
     * 인증 실패 횟수 조회
     */
    Long getVerifyAttempt(Long memberId, Long reservationId);

    /*
     * 인증 실패 횟수 초기화
     *
     * 인증 성공 시 삭제한다.
     */
    void clearVerifyAttempt(Long memberId, Long reservationId);

    /*
     * 예약 입력 중 충전기 임시 점유 시도
     *
     * true:
     * - 아무도 점유하지 않은 충전기
     * - 또는 같은 사용자가 이미 점유 중인 충전기
     *
     * false:
     * - 다른 사용자가 이미 점유 중인 충전기
     */
    boolean holdChargerForReservation(Long chargerId, Long memberId);

    /*
     * 예약 입력 중 충전기 임시 점유 소유자 확인
     *
     * 예약 등록 시
     * 현재 사용자가 해당 충전기의 임시 점유자인지 확인한다.
     */
    boolean isReservationHoldOwner(Long chargerId, Long memberId);

    /*
     * 예약 입력 중 충전기 임시 점유 해제
     *
     * 예약 성공, 예약 화면 이탈 시 사용한다.
     */
    void releaseChargerReservationHold(Long chargerId, Long memberId);
    
    /*
     * 다른 사용자가 해당 충전기를 예약 폼에서 선택 중인지 확인
     */
    boolean isReservationSelectedByOther(Long chargerId, Long memberId);
    
    /*
     * 예약폼에서 충전기 선택 변경
     *
     * 기존 선점 충전기 key를 해제하고,
     * 새 충전기 key를 Redis에 선점한다.
     */
    boolean changeReservationHold(Long oldChargerId, Long newChargerId, Long memberId);

    /*
     * 예약 입력 중 선택 시간 구간 임시 선점
     *
     * 선점 기준:
     * - 충전기 + 예약 시작 시간 + 예상 종료 시간(+5분 버퍼)
     */
    boolean holdReservationTimeSlot(Long chargerId, Long memberId, java.time.LocalDateTime startTime, java.time.LocalDateTime holdEndTime);

    /* 선택 시간 구간 임시 선점 소유자 확인 */
    boolean isReservationTimeSlotHoldOwner(Long chargerId, Long memberId, java.time.LocalDateTime startTime, java.time.LocalDateTime holdEndTime);

    /* 다른 사용자가 선택 시간 구간을 임시 선점 중인지 확인 */
    boolean isReservationTimeSlotSelectedByOther(Long chargerId, Long memberId, java.time.LocalDateTime startTime, java.time.LocalDateTime holdEndTime);

    /* 선택 시간 구간 임시 선점 해제 */
    void releaseReservationTimeSlotHold(Long chargerId, Long memberId, java.time.LocalDateTime startTime, java.time.LocalDateTime holdEndTime);

    /* 현재 회원이 잡은 모든 예약 시간 구간 임시 선점 해제 */
    void releaseAllReservationTimeSlotHolds(Long memberId);
}