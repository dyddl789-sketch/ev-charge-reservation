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

    // 카카오맵에 표시할 충전소 목록 조회 - 커넥터 타입 필터 포함
    List<EvStationMapDTO> getStationMapList(String keyword, String connectorType);

    // 출발지 좌표 기준 가까운 충전소 목록 조회
    List<EvStationMapDTO> getStationMapListByCoordinate(String keyword,
                                                        Double latitude,
                                                        Double longitude,
                                                        int limit);

    // 출발지 좌표 기준 가까운 충전소 목록 조회 - 커넥터 타입 필터 포함
    List<EvStationMapDTO> getStationMapListByCoordinate(String keyword,
                                                        String connectorType,
                                                        Double latitude,
                                                        Double longitude,
                                                        int limit);

    // 기본 출발지 주변 운영중 충전소 수 조회
    int countNearbyStationByDefaultLocation(Long memberId, int radiusMeter);

    // 기본 출발지 주변 사용 가능한 충전기 수 조회
    int countAvailableChargerByDefaultLocation(Long memberId, int radiusMeter);

    // 기본 출발지 주변 추천 충전소 목록 조회
    List<EvStationMapDTO> getNearbyStationListByDefaultLocation(Long memberId,
                                                                int radiusMeter,
                                                                int limit);
}