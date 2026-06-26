package com.ev.controller.admin;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.EvAdminChargerFormDTO;
import com.ev.service.admin.EvAdminStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/charger")
@RequiredArgsConstructor
public class EvAdminChargerController {

    private final EvAdminStationService evAdminStationService;

    // 충전기 등록 JSON API
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminChargerFormDTO registerCharger(@RequestBody EvAdminChargerFormDTO chargerDTO) {

        log.info("@# EvAdminChargerController.registerCharger()");
        log.info("@# stationId => {}, chargerName => {}", chargerDTO.getStationId(), chargerDTO.getChargerName());

        return evAdminStationService.registerCharger(chargerDTO.getStationId(), chargerDTO);
    }

    // 충전기 상태 변경 JSON API
    @PostMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public Map<String, Object> updateChargerStatus(@RequestBody Map<String, Object> body) {

        Long chargerId = toLong(body.get("chargerId"));
        String status = String.valueOf(body.get("status"));

        log.info("@# EvAdminChargerController.updateChargerStatus()");
        log.info("@# chargerId => {}, status => {}", chargerId, status);

        evAdminStationService.updateChargerStatus(chargerId, status);

        return Map.of(
                "success", true,
                "message", "충전기 상태가 변경되었습니다.",
                "chargerId", chargerId,
                "status", status
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
