package com.ev.dto.admin.reservation	;

import java.util.List;

import lombok.Data;

/*
 * 관리자 예약 현황 페이지 DTO
 */
@Data
public class EvAdminReservationPageDTO {

    private EvAdminReservationSummaryDTO summary;
    private EvAdminReservationIssueDTO issue;
    private List<EvAdminReservationListDTO> reservationList;

    private Long totalCount;
    private Integer page;
    private Integer size;
    private Integer totalPage;

    public boolean isFirstPage() {
        return page <= 1;
    }

    public boolean isLastPage() {
        return page >= totalPage;
    }
}