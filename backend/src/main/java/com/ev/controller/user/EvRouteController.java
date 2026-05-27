package com.ev.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.route.EvRouteSimulationDTO;
import com.ev.service.user.EvRouteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
public class EvRouteController {

    private final EvRouteService routeService;

    /**
     * 길찾기 시뮬레이션 API
     *
     * 요청 예:
     * /route/simulation?startLat=35.1631&startLng=128.9842&endLat=35.1577&endLng=129.0592
     *
     * 출발지:
     * 사용자가 주소 등록에서 선택한 위치
     *
     * 도착지:
     * 사용자가 선택한 충전소
     */
    @GetMapping("/simulation")
    public EvRouteSimulationDTO routeSimulation(
            @RequestParam("startLat") Double startLat,
            @RequestParam("startLng") Double startLng,
            @RequestParam("endLat") Double endLat,
            @RequestParam("endLng") Double endLng) {

        log.info("@# EvRouteController.routeSimulation()");
        log.info("@# startLat => {}", startLat);
        log.info("@# startLng => {}", startLng);
        log.info("@# endLat => {}", endLat);
        log.info("@# endLng => {}", endLng);

        return routeService.getRouteSimulation(
                startLat,
                startLng,
                endLat,
                endLng
        );
    }
}