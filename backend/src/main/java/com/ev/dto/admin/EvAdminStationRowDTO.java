package com.ev.dto.admin;

import lombok.Data;

/*
 * 관리자 대시보드 충전소 운영 현황 DTO
 */
@Data
public class EvAdminStationRowDTO {

    private Long stationId;
    private String stationName;
    private String address;

    private Long totalChargerCount;
    private Long availableChargerCount;
    private Long troubleChargerCount;

    private String stationStatus;

    public String getOperationText() {
        if (!"운영중".equals(stationStatus)) {
            return stationStatus;
        }

        if (troubleChargerCount != null && troubleChargerCount > 0) {
            return "일부 점검";
        }

        return "정상 운영";
    }

    public String getDotClass() {
        if (!"운영중".equals(stationStatus)) {
            return "red-dot";
        }

        if (troubleChargerCount != null && troubleChargerCount > 0) {
            return "orange-dot";
        }

        return "green-dot";
    }
}