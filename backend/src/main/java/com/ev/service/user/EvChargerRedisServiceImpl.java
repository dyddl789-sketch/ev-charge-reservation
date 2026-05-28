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
}