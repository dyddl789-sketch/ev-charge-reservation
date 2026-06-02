package com.ev.dto.admin;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/*
 * 관리자 충전소 등록/수정 폼 DTO
 */
@Data
public class EvAdminStationFormDTO {

    private Long stationId;

    private String stationName;
    private String address;

    private Double latitude;
    private Double longitude;

    private String operatorName;

    private String openTime;
    private String closeTime;

    private String stationStatus;

    private Long chargerCount;
    private Long availableChargerCount;

    private List<EvAdminChargerFormDTO> chargerList = new ArrayList<>();

    // 수정 화면에서 삭제 요청된 충전기 ID 목록
    private List<Long> deleteChargerIds = new ArrayList<>();

    public boolean isEditMode() {
        return stationId != null;
    }
}