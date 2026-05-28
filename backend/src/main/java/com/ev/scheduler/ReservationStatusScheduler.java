package com.ev.scheduler;

import com.ev.service.user.EvReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationStatusScheduler {

    private final EvReservationService evReservationService;

    /*
     * 예약 상태 자동 변경 스케줄러
     *
     * 1분마다 실행
     *
     * 처리 내용:
     * 1. 예약완료 → 노쇼
     * 2. 인증완료 → 충전중
     * 3. 인증완료 → 완료
     * 4. 충전중 → 완료
     */
    @Scheduled(fixedDelay = 60000)
    public void updateReservationStatus() {
        log.info("@# ReservationStatusScheduler.updateReservationStatus()");

        int updateCount = evReservationService.updateReservationStatusAutomatically();

        log.info("@# reservation auto update count => {}", updateCount);
    }
}