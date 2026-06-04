package com.ev.service.user;

import java.time.Duration;
import java.util.Set;
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
     */
    private static final Duration AUTH_CODE_TTL = Duration.ofMinutes(5);

    /*
     * 인증 실패 횟수 제한 시간
     */
    private static final Duration VERIFY_ATTEMPT_TTL = Duration.ofMinutes(10);

    /*
     * 예약 입력 중 충전기 임시 점유 시간
     *
     * 주의:
     * - 현재 20초로 되어 있으므로, 화면에서 오래 머물면 key가 만료될 수 있다.
     * - 화면에서 주기적으로 hold 연장 요청을 보내지 않는다면 1~5분 정도로 늘려도 된다.
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

        String authCode = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );

        String redisKey = getAuthCodeKey(chargerId);

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
     */
    @Override
    public void setChargerStatus(Long chargerId, String status) {
        log.info("@# EvChargerRedisServiceImpl.setChargerStatus()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# status => {}", status);

        String redisKey = getChargerStatusKey(chargerId);

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
     */
    @Override
    public Long increaseVerifyAttempt(Long memberId, Long reservationId) {
        log.info("@# EvChargerRedisServiceImpl.increaseVerifyAttempt()");
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationId => {}", reservationId);

        String redisKey = getVerifyAttemptKey(memberId, reservationId);

        Long attemptCount = stringRedisTemplate.opsForValue().increment(redisKey);

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
     * 핵심 수정:
     * - 같은 회원이 기존에 잡고 있던 다른 충전기 key를 정리한다.
     * - 그래서 한 회원이 여러 충전기를 동시에 "선택중"으로 만드는 문제를 막는다.
     */
    @Override
    public boolean holdChargerForReservation(Long chargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.holdChargerForReservation()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        String redisKey = getReservationHoldKey(chargerId);
        String currentMemberId = String.valueOf(memberId);

        log.info("@# redisKey => {}", redisKey);

        /*
         * 1. 해당 충전기가 이미 다른 사용자에게 잡혀 있는지 확인한다.
         */
        String savedMemberId = stringRedisTemplate.opsForValue().get(redisKey);

        log.info("@# savedMemberId => {}", savedMemberId);

        if (savedMemberId != null && !currentMemberId.equals(savedMemberId)) {
            log.info("@# reservation hold failed - selected by other");
            log.info("@# locked by memberId => {}", savedMemberId);

            return false;
        }

        /*
         * 2. key가 없으면 SETNX로 선점한다.
         */
        if (savedMemberId == null) {
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(redisKey, currentMemberId, RESERVATION_HOLD_TTL);

            if (!Boolean.TRUE.equals(success)) {
                /*
                 * 동시에 다른 사용자가 잡았을 수 있으므로 다시 확인한다.
                 */
                String savedAfterSet = stringRedisTemplate.opsForValue().get(redisKey);

                log.info("@# savedAfterSet => {}", savedAfterSet);

                if (!currentMemberId.equals(savedAfterSet)) {
                    log.info("@# reservation hold failed after setIfAbsent");
                    return false;
                }
            }
        }

        /*
         * 3. 이미 내가 잡고 있거나, 방금 내가 잡은 경우 TTL을 갱신한다.
         */
        stringRedisTemplate.expire(redisKey, RESERVATION_HOLD_TTL);

        /*
         * 4. 현재 회원이 기존에 잡고 있던 다른 충전기 key를 정리한다.
         *
         * 예:
         * - memberId=2가 charger:1, charger:3을 동시에 잡고 있던 상태
         * - 현재 charger:2를 잡으면 charger:1, charger:3은 삭제
         * - charger:2만 유지
         */
        deleteMyReservationHoldsExcept(memberId, chargerId);

        log.info("@# reservation hold success or extended");
        log.info("@# redisKey => {}", redisKey);

        return true;
    }

    /*
     * 예약 입력 중 충전기 임시 점유 소유자 확인
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

    /*
     * 예약폼에서 충전기 선택 변경
     *
     * 처리 흐름:
     * 1. 새 충전기가 다른 사용자에게 선점되어 있으면 실패
     * 2. 새 충전기를 내가 선점
     * 3. 내가 기존에 잡고 있던 다른 충전기 key 전부 삭제
     * 4. 새 충전기 key만 유지
     */
    @Override
    public boolean changeReservationHold(Long oldChargerId, Long newChargerId, Long memberId) {
        log.info("@# EvChargerRedisServiceImpl.changeReservationHold()");
        log.info("@# oldChargerId => {}", oldChargerId);
        log.info("@# newChargerId => {}", newChargerId);
        log.info("@# memberId => {}", memberId);

        String newKey = getReservationHoldKey(newChargerId);
        String currentMemberId = String.valueOf(memberId);

        log.info("@# newKey => {}", newKey);

        /*
         * 같은 충전기를 다시 선택한 경우도
         * holdChargerForReservation()을 태워서 TTL 갱신 + 기존 key 정리를 같이 처리한다.
         */
        if (oldChargerId != null && oldChargerId.equals(newChargerId)) {
            return holdChargerForReservation(newChargerId, memberId);
        }

        /*
         * 1. 새 충전기가 이미 다른 사용자에게 선점되어 있는지 확인한다.
         */
        String newSavedMemberId = stringRedisTemplate.opsForValue().get(newKey);

        log.info("@# newSavedMemberId => {}", newSavedMemberId);

        if (newSavedMemberId != null && !newSavedMemberId.equals(currentMemberId)) {
            log.info("@# new charger already selected by other");
            return false;
        }

        /*
         * 2. 새 충전기 key가 없으면 SETNX로 선점한다.
         */
        if (newSavedMemberId == null) {
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(newKey, currentMemberId, RESERVATION_HOLD_TTL);

            if (!Boolean.TRUE.equals(success)) {
                /*
                 * 동시에 다른 사용자가 잡았을 수 있으므로 다시 확인한다.
                 */
                String savedAfterSet = stringRedisTemplate.opsForValue().get(newKey);

                log.info("@# savedAfterSet => {}", savedAfterSet);

                if (!currentMemberId.equals(savedAfterSet)) {
                    log.info("@# new charger hold failed after setIfAbsent");
                    return false;
                }
            }
        }

        /*
         * 3. 새 충전기 TTL 갱신
         */
        stringRedisTemplate.expire(newKey, RESERVATION_HOLD_TTL);

        /*
         * 4. 내가 기존에 잡고 있던 다른 충전기 key 전부 삭제
         *
         * oldChargerId 하나만 삭제하면,
         * 이전 테스트나 pagehide 실패로 남은 stale key가 계속 선택중으로 보일 수 있다.
         */
        deleteMyReservationHoldsExcept(memberId, newChargerId);

        log.info("@# new charger hold success");
        log.info("@# newKey => {}", newKey);

        return true;
    }

    /*
     * 현재 회원이 잡고 있는 예약 임시 점유 key를 정리한다.
     *
     * 목적:
     * - 한 회원이 여러 충전기를 동시에 선택중으로 만드는 문제 방지
     *
     * excludeChargerId:
     * - 이 충전기 key는 유지하고 나머지만 삭제한다.
     */
    private void deleteMyReservationHoldsExcept(Long memberId, Long excludeChargerId) {
        log.info("@# EvChargerRedisServiceImpl.deleteMyReservationHoldsExcept()");
        log.info("@# memberId => {}", memberId);
        log.info("@# excludeChargerId => {}", excludeChargerId);

        String pattern = "ev:reservation:hold:charger:*";

        Set<String> keys = stringRedisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            log.info("@# reservation hold keys empty");
            return;
        }

        String currentMemberId = String.valueOf(memberId);
        String excludeKey = getReservationHoldKey(excludeChargerId);

        for (String key : keys) {
            String savedMemberId = stringRedisTemplate.opsForValue().get(key);

            if (!currentMemberId.equals(savedMemberId)) {
                continue;
            }

            if (key.equals(excludeKey)) {
                continue;
            }

            stringRedisTemplate.delete(key);

            log.info("@# deleted my old reservation hold key => {}", key);
        }
    }
}