package com.ev.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * 관리자 대시보드 최근 알림 DTO
 */
@Data
@AllArgsConstructor
public class EvAdminNoticeDTO {

    private String message;
    private String timeText;
}