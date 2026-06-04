package com.ev.service.user;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 충전기 Redis 관리 Service 구현체
 *
 * Redis key 구조:
 *
 * 1. 충전기 인증코드
 * ev:charger:{chargerId}:auth-code
 *
 * 2. 충전기 현재 상태
 * ev:charger:{chargerId}:status
 *
 * 3. 인증 실패 횟수
 * ev:verify:attempt:{memberId}:{reservationId}
 *
 * 4. 예약 입력 중 충전기 임시 점유
 * ev:reservation:hold:charger:{chargerId}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvChargerRedisServiceImpl implements EvChargerRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    /*
     * 충전기 인증코드 유효 시간
     *
     * 5분 동안만 유효하다.
     */
    private static final Duration AUTH_CODE_TTL = Duration.ofMinutes(5);

    /*
     * 인증 실패 횟수 제한 시간
     *
     * 10분 동안 실패 횟수를 기억한다.
     */
    private static final Duration VERIFY_ATTEMPT_TTL = Duration.ofMinutes(10);

    /*
     * 예약 입력 중 충전기 임시 점유 시간
     *
     * 사용자가 예약 화면에서 정보를 입력하는 동안
     * 다른 사용자가 같은 충전기를 예약하지 못하게 막는다.
     *
     * 사용자가 창을 닫거나 이탈해도 TTL이 지나면 자동 해제된다.
     */
    private static final Duration RESERVATION_HOLD_TTL = Duration.ofSeconds(20);

    /*
     * 충전기 인증코드 Redis key
     */
    private String getAuthCodeKey(Long chargerId) {
        return "ev:charger:" + chargerId + ":auth-code";
    }

    /*
     * 충전기 상태 Redis key
     */
    private String getChargerStatusKey(Long chargerId) {
        return "ev:charger:" + chargerId + ":status";
    }

    /*
     * 인증 실패 횟수 Redis key
     */
    private String getVerifyAttemptKey(Long memberId, Long reservationId) {
        return "ev:verify:attempt:" + memberId + ":" + reservationId;
    }

    /*
     * 예약 입력 중 충전기 임시 점유 Redis key
     */
    private String getReservationHoldKey(Long chargerId) {
        return "ev:reservation:hold:charger:" + chargerId;
    }

    /*
     * 충전기 인증코드 생성
     */
    @Override
    public String generateAuthCode(Long chargerId) {
        log.info("@# EvChargerRedisServiceImpl.generateAuthCode()");
        log.info("@# chargerId => {}", chargerId);

        /*
         * 100000 ~ 999999 사이의 6자리 숫자 생성
         */
        String authCode = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );

        String redisKey = getAuthCodeKey(chargerId);

        /*
         * Redis 저장
         *
         * key   = ev:charger:{chargerId}:auth-code
         * value = 6자리 인증코드
         * TTL   = 5분
         */
        stringRedisTemplate.opsForValue().set(
                redisKey,
                authCode,
                AUTH_CODE_TTL
        );

        log.info("@# redisKey => {}", redisKey);
        log.info("@# authCode => {}", authCode);

        return authCode;
    }

    /*
     * 충전기 인증코드 조회
     */
    @Override
    public String getAuthCode(Long chargerId) {
        log.info("@# EvChargerRedisServiceImpl.getAuthCode()");
        log.info("@# chargerId => {}", chargerId);

        String redisKey = getAuthCodeKey(chargerId);
        String authCode = stringRedisTemplate.opsForValue().get(redisKey);

        log.info("@# redisKey => {}", redisKey);
        log.info("@# authCode => {}", authCode);

        return authCode;
    }

    /*
     * 충전기 인증코드 삭제
     */
    @Override
    public void deleteAuthCode(Long chargerId) {
        log.info("@# EvChargerRedisServiceImpl.deleteAuthCode()");
        log.info("@# chargerId => {}", chargerId);

        String redisKey = getAuthCodeKey(chargerId);

        stringRedisTemplate.delete(redisKey);

        log.info("@# deleted redisKey => {}", redisKey);
    }

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
    @Override
    public void setChargerStatus(Long chargerId, String status) {
        log.info("@# EvChargerRedisServiceImpl.setChargerStatus()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# status => {}", status);

        String redisKey = getChargerStatusKey(chargerId);

        /*
         * 상태값은 현재 상태 캐시이므로 TTL 없이 저장한다.
         * Redis가 초기화되면 DB 상태를 기준으로 다시 복구할 수 있다.
         */
        stringRedisTemplate.opsForValue().set(redisKey, status);

        log.info("@# redisKey => {}", redisKey);
    }

    /*
     * 충전기 현재 상태 조회
     */
    @Override
    public String getChargerStatus(Long chargerId) {
        log.info("@# EvChargerRedisServiceImpl.getChargerStatus()");
        log.info("@# chargerId => {}", chargerId);

        String redisKey = getChargerStatusKey(chargerId);
        String status = stringRedisTemplate.opsForValue().get(redisKey);

        log.info("@# redisKey => {}", redisKey);
        log.info("@# status => {}", status);

        return status;
    }

    /*
     * 충전기 현재 상태 삭제
     */
    @Override
    public void deleteChargerStatus(Long chargerId) {
        log.info("@# EvChargerRedisServiceImpl.deleteChargerStatus()");
        log.info("@# chargerId => {}", chargerId);

        String redisKey = getChargerStatusKey(chargerId);

        stringRedisTemplate.delete(redisKey);

        log.info("@# deleted redisKey => {}", redisKey);
    }

    /*
     * 인증 실패 횟수 증가
     *
     * 첫 실패 시 TTL 10분을 설정한다.
     * 이후 10분 안에 계속 실패하면 count가 증가한다.
     */
    @Override
    public Long increaseVerifyAttempt(Long memberId, Long reservationId) {
        log.info("@# EvChargerRedisServiceImpl.increaseVerifyAttempt()");
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationId => {}", reservationId);

        String redisKey = getVerifyAttemptKey(memberId, reservationId);

        Long attemptCount = stringRedisTemplate.opsForValue().increment(redisKey);

        /*
         * 처음 실패한 경우에만 TTL을 설정한다.
         */
        if (attemptCount != null && attemptCount == 1L) {
            stringRedisTemplate.expire(redisKey, VERIFY_ATTEMPT_TTL);
        }

        log.info("@# redisKey => {}", redisKey);
        log.info("@# attemptCount => {}", attemptCount);

        return attemptCount;
    }

    /*
     * 인증 실패 횟수 조회
     */
    @Override
    public Long getVerifyAttempt(Long memberId, Long reservationId) {
        log.info("@# EvChargerRedisServiceImpl.getVerifyAttempt()");
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationId => {}", reservationId);

        String redisKey = getVerifyAttemptKey(memberId, reservationId);

        String value = stringRedisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            return 0L;
        }

        try {
            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            log.info("@# verify attempt parse error => {}", value);
            return 0L;
        }
    }

    /*
     * 인증 실패 횟수 초기화
     */
    @Override
    public void clearVerifyAttempt(Long memberId, Long reservationId) {
        log.info("@# EvChargerRedisServiceImpl.clearVerifyAttempt()");
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationId => {}", reservationId);

        String redisKey = getVerifyAttemptKey(memberId, reservationId);

        stringRedisTemplate.delete(redisKey);

        log.info("@# deleted redisKey => {}", redisKey);
    }

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
    @Override
    public boolean holdChargerForReservation(Long chargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.holdChargerForReservation()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        String redisKey = getReservationHoldKey(chargerId);
        String value = String.valueOf(memberId);

        /*
         * Redis SETNX
         *
         * key가 없을 때만 저장한다.
         * 성공하면 현재 사용자가 해당 충전기를 임시 점유한다.
         */
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, value, RESERVATION_HOLD_TTL);

        if (Boolean.TRUE.equals(success)) {
            log.info("@# reservation hold success");
            log.info("@# redisKey => {}", redisKey);
            return true;
        }

        /*
         * 이미 lock이 있는 경우:
         * 같은 사용자가 새로고침하거나 다시 접근한 상황이면
         * TTL만 연장하고 예약 화면 진입을 허용한다.
         */
        String savedMemberId = stringRedisTemplate.opsForValue().get(redisKey);

        if (value.equals(savedMemberId)) {
            stringRedisTemplate.expire(redisKey, RESERVATION_HOLD_TTL);

            log.info("@# reservation hold extended");
            log.info("@# redisKey => {}", redisKey);

            return true;
        }

        /*
         * 다른 사용자가 이미 점유 중인 경우
         */
        log.info("@# reservation hold failed");
        log.info("@# redisKey => {}", redisKey);
        log.info("@# locked by memberId => {}", savedMemberId);

        return false;
    }

    /*
     * 예약 입력 중 충전기 임시 점유 소유자 확인
     *
     * 예약 등록 시
     * 현재 사용자가 해당 충전기의 임시 점유자인지 확인한다.
     */
    @Override
    public boolean isReservationHoldOwner(Long chargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.isReservationHoldOwner()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        String redisKey = getReservationHoldKey(chargerId);

        String savedMemberId = stringRedisTemplate.opsForValue().get(redisKey);

        boolean isOwner = String.valueOf(memberId).equals(savedMemberId);

        log.info("@# redisKey => {}", redisKey);
        log.info("@# savedMemberId => {}", savedMemberId);
        log.info("@# isOwner => {}", isOwner);

        return isOwner;
    }

    /*
     * 예약 입력 중 충전기 임시 점유 해제
     *
     * 본인이 점유한 lock만 삭제한다.
     * 다른 사용자의 lock을 삭제하지 않기 위한 안전장치다.
     */
    @Override
    public void releaseChargerReservationHold(Long chargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.releaseChargerReservationHold()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        String redisKey = getReservationHoldKey(chargerId);

        String savedMemberId = stringRedisTemplate.opsForValue().get(redisKey);

        if (String.valueOf(memberId).equals(savedMemberId)) {
            stringRedisTemplate.delete(redisKey);

            log.info("@# reservation hold released");
            log.info("@# redisKey => {}", redisKey);

            return;
        }

        log.info("@# reservation hold release skipped");
        log.info("@# redisKey => {}", redisKey);
        log.info("@# savedMemberId => {}", savedMemberId);
    }
    
    /*
     * 다른 사용자가 해당 충전기를 예약 폼에서 선택 중인지 확인
     *
     * true:
     * - Redis key가 존재함
     * - 저장된 memberId가 현재 로그인 memberId와 다름
     */
    @Override
    public boolean isReservationSelectedByOther(Long chargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.isReservationSelectedByOther()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        String redisKey = getReservationHoldKey(chargerId);

        String savedMemberId = stringRedisTemplate.opsForValue().get(redisKey);

        boolean selectedByOther =
                savedMemberId != null
                && !String.valueOf(memberId).equals(savedMemberId);

        log.info("@# redisKey => {}", redisKey);
        log.info("@# savedMemberId => {}", savedMemberId);
        log.info("@# selectedByOther => {}", selectedByOther);

        return selectedByOther;
    }
    
    @Override
    public boolean changeReservationHold(Long oldChargerId, Long newChargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.changeReservationHold()");
        log.info("@# oldChargerId => {}", oldChargerId);
        log.info("@# newChargerId => {}", newChargerId);
        log.info("@# memberId => {}", memberId);

        String oldKey = getReservationHoldKey(oldChargerId);
        String newKey = getReservationHoldKey(newChargerId);
        String currentMemberId = String.valueOf(memberId);

        log.info("@# oldKey => {}", oldKey);
        log.info("@# newKey => {}", newKey);

        /*
         * 같은 충전기를 다시 선택한 경우
         * 기존 key의 TTL만 연장한다.
         */
        if (oldChargerId.equals(newChargerId)) {
            Boolean extendResult = stringRedisTemplate.expire(newKey, Duration.ofSeconds(20));

            log.info("@# same charger hold extended => {}", extendResult);

            return Boolean.TRUE.equals(extendResult);
        }

        /*
         * 새로 선택한 충전기가 이미 다른 사용자에게 선점되어 있는지 확인
         */
        String newSavedMemberId = stringRedisTemplate.opsForValue().get(newKey);

        log.info("@# newSavedMemberId => {}", newSavedMemberId);

        if (newSavedMemberId != null && !newSavedMemberId.equals(currentMemberId)) {
            log.info("@# new charger already selected by other");
            return false;
        }

        /*
         * 기존 충전기 선점 해제
         * 단, 내가 선점한 key일 때만 삭제한다.
         */
        String oldSavedMemberId = stringRedisTemplate.opsForValue().get(oldKey);

        log.info("@# oldSavedMemberId => {}", oldSavedMemberId);

        if (oldSavedMemberId != null && oldSavedMemberId.equals(currentMemberId)) {
            stringRedisTemplate.delete(oldKey);
            log.info("@# old charger hold deleted");
        }

        /*
         * 새 충전기 선점
         */
        stringRedisTemplate.opsForValue()
                .set(newKey, currentMemberId, Duration.ofSeconds(20));

        log.info("@# new charger hold success");
        log.info("@# newKey => {}", newKey);

        return true;
    }
}