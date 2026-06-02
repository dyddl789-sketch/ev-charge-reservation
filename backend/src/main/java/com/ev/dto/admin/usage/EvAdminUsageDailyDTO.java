package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 일별 이용 현황 DTO
 */
@Data
public class EvAdminUsageDailyDTO {

    private String usageDate;
    private Long usageCount;
}