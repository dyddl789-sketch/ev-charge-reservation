package com.ev.dto.admin;

import lombok.Data;

/*
 * 관리자 충전소 목록 행 DTO
 */
@Data
public class EvAdminStationListDTO {

    private Long stationId;
    private String stationName;
    private String address;
    private String operatorName;
    private String openTime;
    private String closeTime;
    private String stationStatus;

    private Long chargerCount;
    private Long availableChargerCount;
    private Long usingChargerCount;
    private Long reservedChargerCount;
    private Long checkChargerCount;
    private Long brokenChargerCount;

    private String chargerTypes;

    public String getStationStatusClass() {
        if ("운영중".equals(stationStatus)) return "active";
        if ("점검중".equals(stationStatus)) return "check";
        return "closed";
    }
}
