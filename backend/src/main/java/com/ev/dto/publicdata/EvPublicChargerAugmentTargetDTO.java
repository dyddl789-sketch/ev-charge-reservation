package com.ev.dto.publicdata;

import java.math.BigDecimal;

import lombok.Data;

/*
 * PUBLIC_API 샘플 충전소의 보강 충전기 생성 대상 정보
 */
@Data
public class EvPublicChargerAugmentTargetDTO {

    private Long stationId;
    private String externalStationId;
    private String connectorType;
    private BigDecimal chargingSpeedKw;
    private int activeChargerCount;
}
