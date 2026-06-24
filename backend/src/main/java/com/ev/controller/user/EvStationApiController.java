package com.ev.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.service.user.EvStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/station/api")
@RequiredArgsConstructor
public class EvStationApiController {

    private final EvStationService stationService;

    @GetMapping("/list")
    public List<EvChargingStationDTO> getStationList(
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("@# EvStationApiController.getStationList() keyword => {}", keyword);
        return stationService.getStationList(keyword);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getStationDetailByParam(@RequestParam("stationId") Long stationId) {
        return stationDetailResponse(stationId);
    }

    @GetMapping("/detail/{stationId}")
    public ResponseEntity<?> getStationDetailByPath(@PathVariable("stationId") Long stationId) {
        return stationDetailResponse(stationId);
    }

    @GetMapping("/chargers")
    public List<EvChargerDTO> getChargerList(@RequestParam("stationId") Long stationId) {
        log.info("@# EvStationApiController.getChargerList() stationId => {}", stationId);
        return stationService.getChargerList(stationId);
    }

    private ResponseEntity<?> stationDetailResponse(Long stationId) {
        log.info("@# EvStationApiController.stationDetailResponse() stationId => {}", stationId);

        EvChargingStationDTO station = stationService.getStationDetail(stationId);
        List<EvChargerDTO> chargerList = stationService.getChargerList(stationId);

        if (station == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("station", station);
        result.put("chargerList", chargerList);

        return ResponseEntity.ok(result);
    }
}
