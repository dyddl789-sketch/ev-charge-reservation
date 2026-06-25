package com.ev.dto.admin;

import java.time.LocalDate;

import lombok.Data;

/*
 * 관리자 회원 검색 조건 DTO
 */
@Data
public class EvAdminMemberSearchDTO {

    private String status;
    private String userType;
    private LocalDate joinStart;
    private LocalDate joinEnd;

    private String searchType;
    private String keyword;

    private Integer page = 1;
    private Integer size = 20;

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
            return 20;
        }

        return size;
    }
}
