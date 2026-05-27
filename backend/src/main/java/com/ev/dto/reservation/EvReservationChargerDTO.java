package com.ev.dto.reservation;

import java.math.BigDecimal;

import lombok.Data;

/*
 * 예약 폼에서 보여줄 충전기 + 충전소 정보 DTO
 *
 * charger 테이블과 charging_station 테이블을 join해서 사용한다.
 */
@Data
public class EvReservationChargerDTO {

    /*
     * 충전기 정보
     */
    private Long chargerId;
    private Long stationId;

    private String chargerName;
    private String chargerType;
    private String connectorType;

    private Double  chargingSpeedKw;
    private Double  pricePerKwh;

    private String chargerStatus;

    /*
     * 충전소 정보
     */
    private String stationName;
    private String address;
    private String operatorName;
    private String stationStatus;
}