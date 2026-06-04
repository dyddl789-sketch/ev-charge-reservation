package com.ev.dto.chat;

import lombok.Data;

@Data
public class EvAiChargeInfoDTO {

    // 차량 정보
    private String manufacturer;
    private String modelName;
    private String vehicleNickname;
    private String vehicleConnectorType;

    // 배터리 용량
    private Double batteryCapacityKwh;

    // 충전소 정보
    private Long stationId;
    private String stationName;
    private String stationAddress;

    // 충전기 정보
    private Long chargerId;
    private String chargerName;
    private String chargerType;
    private Double chargingSpeedKw;
    private Double pricePerKwh;
    private String chargerConnectorType;

    // 충전소 거리
    private Double distanceKm;

    // 사용자 입력 SOC
    private Integer currentSoc;
    private Integer targetSoc;

    // 충전 계산 결과
    private Double requiredKwh;
    private Integer estimatedMinutes;
    private Integer estimatedCost;
}