package com.ev.dto.complaint;

import lombok.Data;

/*
 * 관리자 민원 검색 조건 DTO
 */
@Data
public class EvComplaintSearchDTO {

    private String status;
    private String complaintType;
    private String priority;
    private String keyword;
    private Long stationId;
    private Long chargerId;

    private String createdFrom;
    private String createdTo;

    private int page = 1;
    private int size = 10;

    public int getOffset() {
        int safePage = page < 1 ? 1 : page;
        int safeSize = size < 1 ? 10 : size;
        return (safePage - 1) * safeSize;
    }

    public int getLimit() {
        if (size < 1) {
            return 10;
        }
        if (size > 100) {
            return 100;
        }
        return size;
    }
}
