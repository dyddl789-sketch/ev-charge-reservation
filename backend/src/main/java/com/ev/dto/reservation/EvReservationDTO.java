package com.ev.dto.reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;


@Data
public class EvReservationDTO {
    private Long reservationId;
    private Long memberId;
    private Long vehicleId;
    private Long chargerId;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer currentSoc;
    private Integer targetSoc;
    private Double  requiredKwh;
    private Integer estimatedMinutes;
    private Double  estimatedCost;
    private String status;
    private String authCode;
    private LocalDateTime verifiedAt;
    private LocalDateTime noShowAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}