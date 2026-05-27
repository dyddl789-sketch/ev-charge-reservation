package com.ev.dto.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지도 위 경로선을 그릴 때 사용할 좌표 DTO
 *
 * Kakao Mobility API 응답의 vertexes는
 * 경도, 위도 순서로 내려온다.
 *
 * 프론트 Kakao Map Polyline에서는
 * new kakao.maps.LatLng(위도, 경도) 순서로 사용한다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvRoutePointDTO {

    // 위도
    private Double lat;

    // 경도
    private Double lng;
}