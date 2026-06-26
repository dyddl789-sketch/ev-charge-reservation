package com.ev.dao.admin;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.EvAdminNoticeDTO;
import com.ev.dto.admin.EvAdminReservationRowDTO;
import com.ev.dto.admin.EvAdminStationRowDTO;
import com.ev.dto.admin.simulation.EvAdminSimulationChargerDTO;

@Mapper
public interface EvAdminDashboardDAO {

    Long countTotalMembers();

    Long countNewMembers(@Param("date") LocalDate date);

    Long countTotalStations();

    Long countNewStations(@Param("date") LocalDate date);

    Long countTotalChargers();

    Long countNewChargers(@Param("date") LocalDate date);

    Long countReservationsByDate(@Param("date") LocalDate date);

    Long countYesterdayReservations(@Param("date") LocalDate date);

    Long countAvailableChargers();

    Long countChargingChargers();

    Long countReservedChargers();

    Long countTroubleChargers();

    Long sumTodaySales(@Param("date") LocalDate date);

    List<EvAdminReservationRowDTO> findTodayReservations(@Param("date") LocalDate date);

    List<EvAdminStationRowDTO> findStationStatusList();
    
    // 최근 운영 알림 조회
    List<EvAdminNoticeDTO> findRecentNotices();

    // 최근 장애 발생 충전기 목록
    List<EvAdminSimulationChargerDTO> findRecentBrokenChargers();
}