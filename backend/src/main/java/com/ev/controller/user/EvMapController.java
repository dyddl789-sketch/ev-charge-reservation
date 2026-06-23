package com.ev.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ev.dto.map.EvKakaoAddressDTO;
import com.ev.dto.map.EvSavedLocationDTO;
import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvStationMapDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvKakaoAddressSearchService;
import com.ev.service.user.EvSavedLocationService;
import com.ev.service.user.EvStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/station")
@RequiredArgsConstructor
public class EvMapController {

    private final EvStationService stationService;

    // 저장 위치 기능에서 사용할 Service
    private final EvSavedLocationService savedLocationService;

    // 주소를 위도/경도로 변환하기 위한 Kakao Local API Service
    private final EvKakaoAddressSearchService kakaoAddressSearchService;

    @Value("${kakao.javascript.key}")
    private String kakaoJavascriptKey;

    // 충전소 지도 화면
    @GetMapping("/map")
    public String stationMap(Model model) {
        log.info("@# EvMapController.stationMap()");

        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);

        return "user/station/station_map";
    }

    // 충전소 마커 데이터
    @ResponseBody
    @GetMapping("/map-data")
    public List<EvStationMapDTO> stationMapData(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "connectorType", required = false) String connectorType,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {

        log.info("@# EvMapController.stationMapData()");
        log.info("@# keyword => {}", keyword);
        log.info("@# connectorType => {}", connectorType);
        log.info("@# latitude => {}", latitude);
        log.info("@# longitude => {}", longitude);
        log.info("@# limit => {}", limit);

        List<EvStationMapDTO> stationMapList;

        if (latitude != null && longitude != null) {
            stationMapList = stationService.getStationMapListByCoordinate(
                    keyword,
                    connectorType,
                    latitude,
                    longitude,
                    limit
            );
        } else {
            stationMapList = stationService.getStationMapList(keyword, connectorType);
        }

        log.info("@# stationMapList size => {}", stationMapList.size());

        return stationMapList;
    }

    // 저장 위치 목록 조회
    @ResponseBody
    @GetMapping("/saved-locations")
    public List<EvSavedLocationDTO> savedLocationList(
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvMapController.savedLocationList()");

        if (userDetails == null) {
            log.info("@# userDetails is null");
            return List.of();
        }

        Long memberId = userDetails.getMemberId();

        log.info("@# memberId => {}", memberId);

        return savedLocationService.findSavedLocationList(memberId);
    }

    // 저장 위치 등록
    @ResponseBody
    @PostMapping("/saved-locations")
    public String saveSavedLocation(EvSavedLocationDTO savedLocationDTO,
                                    @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvMapController.saveSavedLocation()");
        log.info("@# savedLocationDTO => {}", savedLocationDTO);

        if (userDetails == null) {
            return "login_required";
        }

        Long memberId = userDetails.getMemberId();
        savedLocationDTO.setMemberId(memberId);

        /*
         * React 화면에서는 사용자가 주소만 입력한다.
         * saved_location 테이블은 위도/경도와 PostGIS location 컬럼이 필요하므로,
         * 위도/경도가 비어 있으면 Kakao Local API로 주소를 좌표로 변환한다.
         */
        if (savedLocationDTO.getLatitude() == null || savedLocationDTO.getLongitude() == null) {
            EvKakaoAddressDTO coordinate = kakaoAddressSearchService.searchCoordinate(
                    savedLocationDTO.getAddress(),
                    savedLocationDTO.getLocationName()
            );

            if (coordinate == null || coordinate.getLatitude() == null || coordinate.getLongitude() == null) {
                log.warn("@# saved location coordinate not found => {}", savedLocationDTO.getAddress());
                return "주소 좌표를 찾지 못했습니다. 도로명 주소를 조금 더 정확히 입력해 주세요.";
            }

            savedLocationDTO.setLatitude(coordinate.getLatitude().doubleValue());
            savedLocationDTO.setLongitude(coordinate.getLongitude().doubleValue());
        }

        savedLocationService.saveSavedLocation(savedLocationDTO);

        return "success";
    }

    // 기본 위치 설정
    @ResponseBody
    @PostMapping("/saved-locations/default")
    public String setDefaultLocation(@RequestParam("locationId") Long locationId,
                                     @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvMapController.setDefaultLocation()");
        log.info("@# locationId => {}", locationId);

        if (userDetails == null) {
            return "login_required";
        }

        Long memberId = userDetails.getMemberId();

        savedLocationService.setDefaultLocation(memberId, locationId);

        return "success";
    }

    // 저장 위치 삭제
    @ResponseBody
    @PostMapping("/saved-locations/delete")
    public String deleteSavedLocation(@RequestParam("locationId") Long locationId,
                                      @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvMapController.deleteSavedLocation()");
        log.info("@# locationId => {}", locationId);

        if (userDetails == null) {
            return "login_required";
        }

        Long memberId = userDetails.getMemberId();

        savedLocationService.deleteSavedLocation(memberId, locationId);

        return "success";
    }
    
    /*
     * 특정 충전소의 충전기 목록 조회
     *
     * 요청 URL:
     * GET /station/chargers?stationId=1
     */
    @ResponseBody
    @GetMapping("/chargers")
    public List<EvChargerDTO> chargerList(@RequestParam("stationId") Long stationId) {
        log.info("@# EvMapController.chargerList()");
        log.info("@# stationId => {}", stationId);

        return stationService.getChargerList(stationId);
    }
}


