package com.ev.service.admin;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.ev.dao.admin.EvAdminDashboardDAO;
import com.ev.dto.admin.EvAdminDashboardDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 대시보드 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminDashboardServiceImpl implements EvAdminDashboardService {

    private final EvAdminDashboardDAO evAdminDashboardDAO;

    @Override
    public EvAdminDashboardDTO getDashboard(LocalDate date) {

        log.info("@# EvAdminDashboardServiceImpl.getDashboard()");
        log.info("@# dashboard date => {}", date);

        EvAdminDashboardDTO dashboardDTO = new EvAdminDashboardDTO();

        Long totalMemberCount = safeLong(evAdminDashboardDAO.countTotalMembers());
        Long memberIncreaseCount = safeLong(evAdminDashboardDAO.countNewMembers(date));

        Long totalStationCount = safeLong(evAdminDashboardDAO.countTotalStations());
        Long stationIncreaseCount = safeLong(evAdminDashboardDAO.countNewStations(date));

        Long totalChargerCount = safeLong(evAdminDashboardDAO.countTotalChargers());
        Long chargerIncreaseCount = safeLong(evAdminDashboardDAO.countNewChargers(date));

        Long todayReservationCount = safeLong(evAdminDashboardDAO.countReservationsByDate(date));
        Long yesterdayReservationCount = safeLong(evAdminDashboardDAO.countYesterdayReservations(date));

        Long availableChargerCount = safeLong(evAdminDashboardDAO.countAvailableChargers());
        Long chargingChargerCount = safeLong(evAdminDashboardDAO.countChargingChargers());
        Long reservedChargerCount = safeLong(evAdminDashboardDAO.countReservedChargers());
        Long troubleChargerCount = safeLong(evAdminDashboardDAO.countTroubleChargers());

        Long todaySalesAmount = safeLong(evAdminDashboardDAO.sumTodaySales(date));

        dashboardDTO.setTotalMemberCount(totalMemberCount);
        dashboardDTO.setMemberIncreaseCount(memberIncreaseCount);

        dashboardDTO.setTotalStationCount(totalStationCount);
        dashboardDTO.setStationIncreaseCount(stationIncreaseCount);

        dashboardDTO.setTotalChargerCount(totalChargerCount);
        dashboardDTO.setChargerIncreaseCount(chargerIncreaseCount);

        dashboardDTO.setTodayReservationCount(todayReservationCount);
        dashboardDTO.setReservationIncreaseCount(todayReservationCount - yesterdayReservationCount);

        dashboardDTO.setAvailableChargerCount(availableChargerCount);
        dashboardDTO.setChargingChargerCount(chargingChargerCount);
        dashboardDTO.setReservedChargerCount(reservedChargerCount);
        dashboardDTO.setTroubleChargerCount(troubleChargerCount);

        dashboardDTO.setAvailableChargerRate(calcRate(availableChargerCount, totalChargerCount));
        dashboardDTO.setChargingChargerRate(calcRate(chargingChargerCount, totalChargerCount));
        dashboardDTO.setReservedChargerRate(calcRate(reservedChargerCount, totalChargerCount));
        dashboardDTO.setTroubleChargerRate(calcRate(troubleChargerCount, totalChargerCount));

        dashboardDTO.setTodaySalesAmount(todaySalesAmount);

        dashboardDTO.setTodayReservationList(
                evAdminDashboardDAO.findTodayReservations(date)
        );

        dashboardDTO.setStationStatusList(
                evAdminDashboardDAO.findStationStatusList()
        );

        dashboardDTO.setNoticeList(
                evAdminDashboardDAO.findRecentNotices()
        );

        dashboardDTO.setFaultChargerList(
                evAdminDashboardDAO.findRecentBrokenChargers()
        );

        log.info("@# totalMemberCount => {}", totalMemberCount);
        log.info("@# totalStationCount => {}", totalStationCount);
        log.info("@# totalChargerCount => {}", totalChargerCount);
        log.info("@# todayReservationCount => {}", todayReservationCount);

        return dashboardDTO;
    }

    // null 방지용
    private Long safeLong(Long value) {
        if (value == null) {
            return 0L;
        }

        return value;
    }

    // 퍼센트 계산
    private Double calcRate(Long count, Long total) {
        if (total == null || total == 0) {
            return 0.0;
        }

        return Math.round((count * 1000.0 / total)) / 10.0;
    }
    
}