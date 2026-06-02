package com.ev.controller.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;
import com.ev.service.admin.EvAdminStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/station")
@RequiredArgsConstructor
public class EvAdminStationController {

    private final EvAdminStationService evAdminStationService;

    @Value("${kakao.javascript.key}")
    private String kakaoJavascriptKey;

    // 충전소 목록 화면
    @GetMapping("/list")
    public String stationList(
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "stationStatus", required = false) String stationStatus,
            @RequestParam(value = "chargerType", required = false) String chargerType,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Model model) {

        log.info("@# EvAdminStationController.stationList()");

        EvAdminStationSearchDTO searchDTO = new EvAdminStationSearchDTO();

        searchDTO.setRegion(region);
        searchDTO.setStationStatus(stationStatus);
        searchDTO.setChargerType(chargerType);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        EvAdminStationPageDTO stationPage =
                evAdminStationService.getStationPage(searchDTO);

        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("stationPage", stationPage);

        return "admin/station/station_list";
    }

    // 충전소 등록/수정 화면
    @GetMapping("/manage")
    public String stationManage(
            @RequestParam(value = "stationId", required = false) Long stationId,
            Model model) {

        log.info("@# EvAdminStationController.stationManage()");
        log.info("@# stationId => {}", stationId);

        EvAdminStationFormDTO stationDTO = new EvAdminStationFormDTO();

        if (stationId != null) {
            stationDTO = evAdminStationService.getStationForm(stationId);
        }

        model.addAttribute("station", stationDTO);
        model.addAttribute("kakaoJavascriptKey", kakaoJavascriptKey);

        return "admin/station/station_manage";
    }

    // 충전소 신규 등록
    @PostMapping("/register")
    public String registerStation(
            EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationController.registerStation()");
        log.info("@# stationName => {}", stationDTO.getStationName());

        evAdminStationService.registerStation(stationDTO);

        return "redirect:/admin/station/list";
    }

    // 충전소 수정
    @PostMapping("/update")
    public String updateStation(
            EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationController.updateStation()");
        log.info("@# stationId => {}", stationDTO.getStationId());

        evAdminStationService.updateStation(stationDTO);

        return "redirect:/admin/station/list";
    }
}