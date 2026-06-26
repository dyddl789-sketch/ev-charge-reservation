package com.ev.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.simulation.EvAdminOperationResultDTO;
import com.ev.service.admin.EvAdminOperationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * MIS 대시보드 운영 시뮬레이션 API
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EvAdminOperationController {

    private final EvAdminOperationService evAdminOperationService;

    // 환경부 공공데이터 전국 시도별 샘플 적재
    @PostMapping("/public-api/chargers/sync-sample")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminOperationResultDTO syncPublicDataSample(
            @RequestParam(value = "limitPerRegion", defaultValue = "10") int limitPerRegion) {

        log.info("@# EvAdminOperationController.syncPublicDataSample()");
        log.info("@# limitPerRegion => {}", limitPerRegion);

        return evAdminOperationService.syncPublicDataSample(limitPerRegion);
    }

    // 이용/매출 통계가 실제 화면에 차도록 예약/충전완료 샘플 데이터를 생성한다.
    @PostMapping("/statistics/sample-data")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminOperationResultDTO generateStatisticsSampleData(
            @RequestParam(value = "days", defaultValue = "120") int days,
            @RequestParam(value = "count", defaultValue = "6000") int count) {

        log.info("@# EvAdminOperationController.generateStatisticsSampleData()");
        log.info("@# days => {}, count => {}", days, count);

        return evAdminOperationService.generateStatisticsSampleData(days, count);
    }

    // 사용가능 충전기 중 하나를 고장 상태로 변경한다.
    @PostMapping("/simulation/fault")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public EvAdminOperationResultDTO triggerFaultSimulation() {
        log.info("@# EvAdminOperationController.triggerFaultSimulation()");
        return evAdminOperationService.triggerFaultSimulation();
    }

    // 발표 시작용 초기화: PUBLIC_API 충전소/충전기와 demo/stat 시연 회원 데이터를 정리한다.
    @PostMapping("/simulation/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public EvAdminOperationResultDTO resetSimulation() {
        log.info("@# EvAdminOperationController.resetSimulation()");
        return evAdminOperationService.resetSimulation();
    }
}
