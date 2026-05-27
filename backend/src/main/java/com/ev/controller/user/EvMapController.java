package com.ev.controller.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ev.dto.map.EvSavedLocationDTO;
import com.ev.dto.station.EvStationMapDTO;
import com.ev.service.user.EvSavedLocationService;
import com.ev.service.user.EvStationService;

import jakarta.servlet.http.HttpSession;
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
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("@# EvMapController.stationMapData()");
        log.info("@# keyword => {}", keyword);

        List<EvStationMapDTO> stationMapList = stationService.getStationMapList(keyword);

        log.info("@# stationMapList size => {}", stationMapList.size());

        return stationMapList;
    }

    // 저장 위치 목록 조회
    @ResponseBody
    @GetMapping("/saved-locations")
    public List<EvSavedLocationDTO> savedLocationList(HttpSession session) {
        log.info("@# EvMapController.savedLocationList()");

        Object loginMemberId = session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            log.info("@# loginMemberId is null");
            return List.of();
        }

        Long memberId = Long.valueOf(String.valueOf(loginMemberId));

        log.info("@# memberId => {}", memberId);

        return savedLocationService.findSavedLocationList(memberId);
    }

    // 저장 위치 등록
    @ResponseBody
    @PostMapping("/saved-locations")
    public String saveSavedLocation(EvSavedLocationDTO savedLocationDTO,
                                    HttpSession session) {
        log.info("@# EvMapController.saveSavedLocation()");
        log.info("@# savedLocationDTO => {}", savedLocationDTO);

        Object loginMemberId = session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            return "login_required";
        }

        Long memberId = Long.valueOf(String.valueOf(loginMemberId));

        savedLocationDTO.setMemberId(memberId);

        savedLocationService.saveSavedLocation(savedLocationDTO);

        return "success";
    }

    // 기본 위치 설정
    @ResponseBody
    @PostMapping("/saved-locations/default")
    public String setDefaultLocation(@RequestParam("locationId") Long locationId,
                                     HttpSession session) {
        log.info("@# EvMapController.setDefaultLocation()");
        log.info("@# locationId => {}", locationId);

        Object loginMemberId = session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            return "login_required";
        }

        Long memberId = Long.valueOf(String.valueOf(loginMemberId));

        savedLocationService.setDefaultLocation(memberId, locationId);

        return "success";
    }

    // 저장 위치 삭제
    @ResponseBody
    @PostMapping("/saved-locations/delete")
    public String deleteSavedLocation(@RequestParam("locationId") Long locationId,
                                      HttpSession session) {
        log.info("@# EvMapController.deleteSavedLocation()");
        log.info("@# locationId => {}", locationId);

        Object loginMemberId = session.getAttribute("loginMemberId");

        if (loginMemberId == null) {
            return "login_required";
        }

        Long memberId = Long.valueOf(String.valueOf(loginMemberId));

        savedLocationService.deleteSavedLocation(memberId, locationId);

        return "success";
    }
}