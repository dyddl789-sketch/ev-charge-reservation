package com.ev.controller.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ev.dto.map.EvSavedLocationDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.station.EvStationMapDTO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvReservationService;
import com.ev.service.user.EvSavedLocationService;
import com.ev.service.user.EvStationService;
import com.ev.service.user.EvVehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EvMainController {

    private static final int NEARBY_RADIUS_METER = 10000;
    private static final int MAIN_STATION_LIMIT = 3;

    private final EvVehicleService evVehicleService;
    private final EvSavedLocationService savedLocationService;
    private final EvStationService stationService;
    private final EvReservationService evReservationService;

    @GetMapping("/main")
    public String main(@AuthenticationPrincipal EvUserDetails userDetails,
                       Model model) {
        log.info("@# EvMainController.main()");

        /*
         * 비회원도 메인 화면은 볼 수 있게 둔다.
         * 다만 개인화 데이터는 로그인한 경우에만 조회한다.
         */
        if (userDetails == null) {
            log.info("@# userDetails is null");

            model.addAttribute("nearbyStationCount", 0);
            model.addAttribute("nearbyAvailableChargerCount", 0);
            model.addAttribute("thisMonthChargingCost", 0);
            model.addAttribute("nearbyStationList", new ArrayList<EvStationMapDTO>());
            model.addAttribute("savedLocationList", new ArrayList<EvSavedLocationDTO>());

            return "user/main/main";
        }

        Long memberId = userDetails.getMemberId();

        log.info("@# 로그인 사용자 ID => {}", userDetails.getUsername());
        log.info("@# 로그인 사용자 이름 => {}", userDetails.getMemberName());
        log.info("@# 로그인 사용자 권한 => {}", userDetails.getUserType());
        log.info("@# memberId => {}", memberId);

        model.addAttribute("loginMemberName", userDetails.getMemberName());

        /*
         * 1. 내 기본 차량 조회
         *
         * getVehicleList()는 Mapper에서 기본 차량 우선 정렬되어 있으므로
         * 첫 번째 차량을 메인 차량으로 사용한다.
         */
        List<EvVehicleDTO> vehicleList = evVehicleService.getVehicleList(memberId);
        EvVehicleDTO mainVehicle = null;

        if (vehicleList != null && !vehicleList.isEmpty()) {
            mainVehicle = vehicleList.get(0);
        }

        model.addAttribute("mainVehicle", mainVehicle);

        /*
         * 2. 저장 위치 / 기본 출발지 조회
         */
        List<EvSavedLocationDTO> savedLocationList =
                savedLocationService.findSavedLocationList(memberId);

        EvSavedLocationDTO defaultLocation = null;

        if (savedLocationList != null) {
            for (EvSavedLocationDTO location : savedLocationList) {
                if (Boolean.TRUE.equals(location.getIsDefault())) {
                    defaultLocation = location;
                    break;
                }
            }
        }

        model.addAttribute("savedLocationList", savedLocationList);
        model.addAttribute("defaultLocation", defaultLocation);

        /*
         * 3. 기본 출발지 주변 충전소 조회
         */
        int nearbyStationCount = 0;
        int nearbyAvailableChargerCount = 0;
        List<EvStationMapDTO> nearbyStationList = new ArrayList<>();

        if (defaultLocation != null) {
            nearbyStationCount = stationService.countNearbyStationByDefaultLocation(
                    memberId,
                    NEARBY_RADIUS_METER
            );

            nearbyAvailableChargerCount = stationService.countAvailableChargerByDefaultLocation(
                    memberId,
                    NEARBY_RADIUS_METER
            );

            nearbyStationList = stationService.getNearbyStationListByDefaultLocation(
                    memberId,
                    NEARBY_RADIUS_METER,
                    MAIN_STATION_LIMIT
            );
        }

        model.addAttribute("nearbyRadiusKm", NEARBY_RADIUS_METER / 1000);
        model.addAttribute("nearbyStationCount", nearbyStationCount);
        model.addAttribute("nearbyAvailableChargerCount", nearbyAvailableChargerCount);
        model.addAttribute("nearbyStationList", nearbyStationList);

        /*
         * 4. 다음 예약 / 이번 달 충전 비용 조회
         */
        EvReservationDTO nextReservation =
                evReservationService.getNextReservation(memberId);

        Integer thisMonthChargingCost =
                evReservationService.getThisMonthChargingCost(memberId);

        model.addAttribute("nextReservation", nextReservation);
        model.addAttribute("thisMonthChargingCost", thisMonthChargingCost == null ? 0 : thisMonthChargingCost);

        return "user/main/main";
    }
}
