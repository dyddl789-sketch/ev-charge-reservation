package com.ev.controller.user;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.service.user.EvStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/station")
@RequiredArgsConstructor
public class EvStationController {

    private final EvStationService stationService;

    /*
     * application.properties에 있는 카카오 JavaScript 키를 가져온다.
     *
     * kakao.javascript.key=카카오_JS_KEY
     */

    // 충전소 목록 / 검색
    @GetMapping("/list")
    public String stationList(@RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        log.info("@# EvStationController.stationList()");
        log.info("@# keyword => {}", keyword);

        List<EvChargingStationDTO> stationList = stationService.getStationList(keyword);

        log.info("@# stationList size => {}", stationList.size());

        model.addAttribute("stationList", stationList);
        model.addAttribute("keyword", keyword);

        return "user/station/station_list";
    }

    // 상세 조회
    @GetMapping("/detail")
    public String stationDetail(@RequestParam("stationId") Long stationId,
                                Model model) {
        log.info("@# EvStationController.stationDetail()");
        log.info("@# stationId => {}", stationId);

        // 충전소 기본 정보 조회
        EvChargingStationDTO station = stationService.getStationDetail(stationId);

        // 해당 충전소에 속한 충전기 목록 조회
        List<EvChargerDTO> chargerList = stationService.getChargerList(stationId);

        log.info("@# station => {}", station);
        log.info("@# chargerList size => {}", chargerList.size());

        model.addAttribute("station", station);
        model.addAttribute("chargerList", chargerList);

        return "user/station/station_detail";
    }


}