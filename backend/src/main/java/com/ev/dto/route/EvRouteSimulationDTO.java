package com.ev.dto.route;

import java.util.List;

import lombok.Data;

/**
 * 프론트로 내려줄 길찾기 결과 DTO
 *
 * 이 DTO를 JSP에서 받아서
 * 거리, 소요시간 표시 + 지도 Polyline 그리기에 사용한다.
 */
@Data
public class EvRouteSimulationDTO {

    // 전체 거리 meter
    private Integer distanceMeter;

    // 전체 소요시간 second
    private Integer durationSecond;

    // 화면 표시용 거리
    private String distanceText;

    // 화면 표시용 소요시간
    private String durationText;

    // 지도에 그릴 경로 좌표 목록
    private List<EvRoutePointDTO> path;
}