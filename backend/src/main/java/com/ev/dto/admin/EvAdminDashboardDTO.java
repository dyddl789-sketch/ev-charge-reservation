package com.ev.dto.admin;

import java.util.ArrayList;
import java.util.List;

import com.ev.dto.admin.simulation.EvAdminSimulationChargerDTO;

import lombok.Data;

/*
 * 관리자 대시보드 전체 데이터 DTO
 */
@Data
public class EvAdminDashboardDTO {

    private Long totalMemberCount;
    private Long memberIncreaseCount;

    private Long totalStationCount;
    private Long stationIncreaseCount;

    private Long totalChargerCount;
    private Long chargerIncreaseCount;

    private Long todayReservationCount;
    private Long reservationIncreaseCount;

    private Long availableChargerCount;
    private Long chargingChargerCount;
    private Long reservedChargerCount;
    private Long troubleChargerCount;

    private Double availableChargerRate;
    private Double chargingChargerRate;
    private Double reservedChargerRate;
    private Double troubleChargerRate;

    private Long todaySalesAmount;

    private List<EvAdminReservationRowDTO> todayReservationList = new ArrayList<>();
    private List<EvAdminStationRowDTO> stationStatusList = new ArrayList<>();
    private List<EvAdminNoticeDTO> noticeList = new ArrayList<>();

    // 대시보드 최근 장애 발생 충전기 목록
    private List<EvAdminSimulationChargerDTO> faultChargerList = new ArrayList<>();
}