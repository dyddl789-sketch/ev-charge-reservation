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

    // 사용가능 충전기 중 하나를 고장 상태로 변경한다.
    @PostMapping("/simulation/fault")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public EvAdminOperationResultDTO triggerFaultSimulation() {
        log.info("@# EvAdminOperationController.triggerFaultSimulation()");
        return evAdminOperationService.triggerFaultSimulation();
    }

    // 시연/테스트용 초기화: 고장 충전기를 사용가능으로 돌리고 이력은 남기지 않는다.
    @PostMapping("/simulation/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public EvAdminOperationResultDTO resetSimulation() {
        log.info("@# EvAdminOperationController.resetSimulation()");
        return evAdminOperationService.resetSimulation();
    }
}
