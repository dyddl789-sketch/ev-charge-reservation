package com.ev.dto.station;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EvChargerDTO {

    //충전기 고유 번호
    private Long chargerId;

    // 소속 충전소 번호
    private Long stationId;

    // 충전기 이름
    private String chargerName;

    // 충전기 타입
    private String chargerType;

    // 커넥터 타입
    private String connectorType;

    // 충전 속도
    private BigDecimal chargingSpeedKw;

    // kWh당 충전 요금
    private BigDecimal pricePerKwh;

    // 충전기 상태
    private String status;

    // 등록일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;
}