package com.ev.service.user;

import java.util.List;

import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.dto.station.EvStationMapDTO;


public interface EvStationService {

	// 충전소 목록 조회
    List<EvChargingStationDTO> getStationList(String keyword);

    // 충전소 상세 조회
    EvChargingStationDTO getStationDetail(Long stationId);

    // 특정 충전소의 충전기 목록 조회
    List<EvChargerDTO> getChargerList(Long stationId);
    
    // 카카오맵에 표시할 충전소 목록 조회
    List<EvStationMapDTO> getStationMapList(String keyword);
}