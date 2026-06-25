package com.ev.dto.admin.fault;

import lombok.Data;

/*
 * 관리자 장애·점검 검색 조건 DTO
 */
@Data
public class EvAdminFaultSearchDTO {

    private String status;
    private String keyword;
    private String reportedFrom;
    private String reportedTo;
    private String resolvedFrom;
    private String resolvedTo;

    private int page = 1;
    private int size = 10;

    private String requesterRole;
    private Long requesterEmployeeId;

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
