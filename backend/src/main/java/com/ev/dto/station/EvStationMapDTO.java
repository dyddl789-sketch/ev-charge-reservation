package com.ev.dto.station;

import lombok.Data;

// 카카오맵 마커 표시용 DTO
 
@Data
public class EvStationMapDTO {

    private Long stationId;

    private String stationName;
    private String address;
    private String operatorName;
    private String stationStatus;

    private Double latitude;
    private Double longitude;

    private Integer chargerCount;
    private Integer availableChargerCount;
}