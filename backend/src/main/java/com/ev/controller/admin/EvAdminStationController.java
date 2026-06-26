package com.ev.controller.admin;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;
import com.ev.service.admin.EvAdminStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/station")
@RequiredArgsConstructor
public class EvAdminStationController {

    private final EvAdminStationService evAdminStationService;

    // 충전소 목록 JSON API
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public EvAdminStationPageDTO stationList(
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "stationStatus", required = false) String stationStatus,
            @RequestParam(value = "chargerType", required = false) String chargerType,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        log.info("@# EvAdminStationController.stationList()");
        log.info("@# region => {}, status => {}, keyword => {}, page => {}, size => {}",
                region, stationStatus, keyword, page, size);

        EvAdminStationSearchDTO searchDTO = new EvAdminStationSearchDTO();
        searchDTO.setRegion(region);
        searchDTO.setStationStatus(stationStatus);
        searchDTO.setChargerType(chargerType);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        return evAdminStationService.getStationPage(searchDTO);
    }

    // 충전소 상세 JSON API
    @GetMapping("/detail")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public EvAdminStationFormDTO stationDetail(@RequestParam("stationId") Long stationId) {
        log.info("@# EvAdminStationController.stationDetail()");
        log.info("@# stationId => {}", stationId);
        return evAdminStationService.getStationForm(stationId);
    }

    // 충전소별 충전기 목록 JSON API
    @GetMapping("/chargers")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public Object stationChargers(@RequestParam("stationId") Long stationId) {
        log.info("@# EvAdminStationController.stationChargers()");
        log.info("@# stationId => {}", stationId);
        return evAdminStationService.getStationChargers(stationId);
    }

    // 충전소 신규 등록 JSON API
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminStationFormDTO registerStation(@RequestBody EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationController.registerStation()");
        log.info("@# stationName => {}", stationDTO.getStationName());

        return evAdminStationService.registerStation(stationDTO);
    }

    // 충전소 수정 JSON API
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminStationFormDTO updateStation(@RequestBody EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationController.updateStation()");
        log.info("@# stationId => {}", stationDTO.getStationId());

        return evAdminStationService.updateStation(stationDTO);
    }

    // 충전소 상태 변경 JSON API
    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String, Object> updateStationStatus(@RequestBody Map<String, Object> body) {

        Long stationId = toLong(body.get("stationId"));
        String stationStatus = String.valueOf(body.getOrDefault("stationStatus", body.get("status")));

        log.info("@# EvAdminStationController.updateStationStatus()");
        log.info("@# stationId => {}, stationStatus => {}", stationId, stationStatus);

        evAdminStationService.updateStationStatus(stationId, stationStatus);

        return Map.of(
                "success", true,
                "message", "충전소 상태가 변경되었습니다.",
                "stationId", stationId,
                "stationStatus", stationStatus
        );
    }

    private Long toLong(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("필수 번호 값이 없습니다.");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
