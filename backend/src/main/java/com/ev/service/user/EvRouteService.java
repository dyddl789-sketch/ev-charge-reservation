package com.ev.service.user;

import com.ev.dto.route.EvRouteSimulationDTO;

public interface EvRouteService {

    /**
     * 출발지 좌표와 도착지 좌표를 기준으로
     * Kakao Mobility 자동차 길찾기 API를 호출한다.
     */
    EvRouteSimulationDTO getRouteSimulation(
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng
    );
}