package com.ev.dto.admin.simulation;

import lombok.Data;

/*
 * 장애 시뮬레이션 대상/최근 장애 충전기 DTO
 */
@Data
public class EvAdminSimulationChargerDTO {

    private Long chargerId;
    private Long stationId;
    private String stationName;
    private String chargerName;
    private String chargerType;
    private String connectorType;
    private Double chargingSpeedKw;
    private String status;
    private String address;
    private Double latitude;
    private Double longitude;
    private String regionName;
    private String updatedAtText;
}
