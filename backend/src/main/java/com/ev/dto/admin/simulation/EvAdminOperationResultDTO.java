package com.ev.dto.admin.simulation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/*
 * MIS 대시보드 운영 버튼 처리 결과 DTO
 */
@Data
public class EvAdminOperationResultDTO {

    private boolean success;
    private String message;

    private int saveCount;
    private int updateCount;
    private int resetCount;
    private int limitPerRegion;

    private Long chargerId;
    private Long stationId;
    private String stationName;
    private String chargerName;
    private String beforeStatus;
    private String afterStatus;

    private LocalDateTime processedAt;

    private List<String> regionList = new ArrayList<>();

    // 장애 시뮬레이션으로 변경된 충전기 목록
    private List<EvAdminSimulationChargerDTO> faultChargerList = new ArrayList<>();
}
