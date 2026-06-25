package com.ev.dto.admin.reservation;

import java.time.LocalDate;

import lombok.Data;

/*
 * 관리자 예약 현황 검색 조건 DTO
 */
@Data
public class EvAdminReservationSearchDTO {

    private String status;
    private String searchType;
    private String keyword;

    private LocalDate startDate;
    private LocalDate endDate;

    private String startTime;
    private String endTime;

    private Integer page = 1;
    private Integer size = 10;

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }

    public Integer getPage() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public Integer getSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return size;
    }
}