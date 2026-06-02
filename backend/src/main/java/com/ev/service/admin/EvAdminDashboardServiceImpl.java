package com.ev.service.admin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ev.dao.admin.EvAdminDashboardDAO;
import com.ev.dto.admin.EvAdminDashboardDTO;
import com.ev.dto.admin.EvAdminNoticeDTO;

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
                createNoticeList(
                        memberIncreaseCount,
                        todayReservationCount,
                        troubleChargerCount,
                        todaySalesAmount
                )
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

    // 최근 알림 생성
    private List<EvAdminNoticeDTO> createNoticeList(
            Long memberIncreaseCount,
            Long todayReservationCount,
            Long troubleChargerCount,
            Long todaySalesAmount) {

        List<EvAdminNoticeDTO> noticeList = new ArrayList<>();

        if (troubleChargerCount > 0) {
            noticeList.add(
                    new EvAdminNoticeDTO(
                            "점검중 또는 고장 상태의 충전기가 " + troubleChargerCount + "대 있습니다.",
                            "방금 전"
                    )
            );
        }

        if (todayReservationCount > 0) {
            noticeList.add(
                    new EvAdminNoticeDTO(
                            "오늘 예약 건수가 " + todayReservationCount + "건 등록되었습니다.",
                            "10분 전"
                    )
            );
        }

        if (memberIncreaseCount > 0) {
            noticeList.add(
                    new EvAdminNoticeDTO(
                            "신규 회원 " + memberIncreaseCount + "명이 가입했습니다.",
                            "1시간 전"
                    )
            );
        }

        if (todaySalesAmount > 0) {
            noticeList.add(
                    new EvAdminNoticeDTO(
                            "오늘 매출이 " + todaySalesAmount + "원을 달성했습니다.",
                            "2시간 전"
                    )
            );
        }

        if (noticeList.isEmpty()) {
            noticeList.add(
                    new EvAdminNoticeDTO(
                            "현재 확인된 신규 운영 알림이 없습니다.",
                            "오늘"
                    )
            );
        }

        return noticeList;
    }
}