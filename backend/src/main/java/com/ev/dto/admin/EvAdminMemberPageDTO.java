package com.ev.dto.admin;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/*
 * 관리자 회원 목록 화면 DTO
 */
@Data
public class EvAdminMemberPageDTO {

    private Long totalCount;
    private Long activeCount;
    private Long inactiveCount;

    private Long searchCount;

    private Integer page;
    private Integer size;
    private Integer totalPage;

    private List<EvAdminMemberRowDTO> memberList = new ArrayList<>();
}