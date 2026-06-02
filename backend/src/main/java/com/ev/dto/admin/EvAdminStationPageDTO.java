package com.ev.dto.admin;

import java.util.List;

import lombok.Data;

/*
 * 관리자 충전소 목록 페이지 DTO
 */
@Data
public class EvAdminStationPageDTO {

    private Long totalCount;
    private Long activeCount;
    private Long checkCount;

    private Long searchCount;

    private Integer page;
    private Integer size;
    private Integer totalPage;

    private List<EvAdminStationListDTO> stationList;

    public boolean isFirstPage() {
        return page == null || page <= 1;
    }

    public boolean isLastPage() {
        return page == null || totalPage == null || page >= totalPage;
    }
}