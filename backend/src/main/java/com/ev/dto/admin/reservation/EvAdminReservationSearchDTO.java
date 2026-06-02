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

    private Integer page = 1;
    private Integer size = 10;

    public int getOffset() {
        return (page - 1) * size;
    }
}