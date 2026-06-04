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
    private String stationStatus;

    private Long chargerCount;
    private Long availableChargerCount;

    private String chargerTypes;

    public String getStationStatusClass() {
        if ("운영중".equals(stationStatus)) {
            return "active";
        }

        if ("점검중".equals(stationStatus)) {
            return "check";
        }

        return "closed";
    }

    public boolean isFast() {
        return chargerTypes != null && chargerTypes.contains("급속");
    }

    public boolean isSlow() {
        return chargerTypes != null && chargerTypes.contains("완속");
    }

    public boolean isUltra() {
        return chargerTypes != null && chargerTypes.contains("초급속");
    }
}