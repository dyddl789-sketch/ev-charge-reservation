package com.ev.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvAiStationRecommendDTO {

    // 충전소 정보
    private Long stationId;
    private String stationName;
    private String address;

    // 충전기 정보
    private String chargerName;
    private String chargerType;
    private String connectorType;

    private Double chargingSpeedKw;
    private Double pricePerKwh;

    // 충전기 상태 및 거리
    private String status;
    private Double distanceKm;

 // 대표 차량 정보
    private String manufacturer;
    private String modelName;
    private String vehicleNickname;
    private String vehicleConnectorType;
}