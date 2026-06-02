package com.ev.dto.admin.reservation;

import lombok.Data;

/*
 * 오늘 예약 이슈 DTO
 */
@Data
public class EvAdminReservationIssueDTO {

    private Long waitingAuthCount;
    private Long cancelRequestCount;
    private Long expectedNoShowCount;
    private Long delayedStartCount;
}