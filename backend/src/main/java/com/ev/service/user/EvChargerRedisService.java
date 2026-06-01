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
     * 충전기 인증코드 조회
     *
     * Redis에 저장된 현재 인증코드를 조회한다.
     */
    String getAuthCode(Long chargerId);

    /*
     * 충전기 인증코드 삭제
     *
     * 인증 성공 후 재사용을 막고 싶을 때 사용한다.
     */
    void deleteAuthCode(Long chargerId);

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
}