package com.ev.dto.admin;

import lombok.Data;

/*
 * 관리자 충전소 검색 조건 DTO
 */
@Data
public class EvAdminStationSearchDTO {

    private String region;
    private String stationStatus;
    private String chargerType;
    private String searchType;
    private String keyword;

    private Integer page = 1;
    private Integer size = 10;

    public int getOffset() {
        if (page == null || page < 1) {
            page = 1;
        }

        if (size == null || size < 1) {
            size = 10;
        }

        return (page - 1) * size;
    }
}