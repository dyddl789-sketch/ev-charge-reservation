package com.ev.dto.admin;

import lombok.Data;

/*
 * 관리자 충전기 등록/수정 폼 DTO
 */
@Data
public class EvAdminChargerFormDTO {

    private Long chargerId;

    private String chargerName;
    private String chargerType;
    private String connectorType;

    private Double chargingSpeedKw;
    private Integer pricePerKwh;

    private String status;
}