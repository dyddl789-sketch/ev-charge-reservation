package com.ev.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.service.user.EvStationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/station/detail")
@RequiredArgsConstructor
public class EvStationDetailAliasController {

    private final EvStationService stationService;

    /**
     * React 기존 코드가 GET /station/detail/{stationId} 형태로 호출하고 있어서 추가한 JSON 별칭 API.
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<?> getStationDetail(@PathVariable("stationId") Long stationId) {
        EvChargingStationDTO station = stationService.getStationDetail(stationId);

        if (station == null) {
            return ResponseEntity.notFound().build();
        }

        List<EvChargerDTO> chargerList = stationService.getChargerList(stationId);

        Map<String, Object> result = new HashMap<>();
        result.put("station", station);
        result.put("chargerList", chargerList);

        return ResponseEntity.ok(result);
    }
}
