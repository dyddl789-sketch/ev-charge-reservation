package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ev.dao.user.EvStationDAO;
import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.dto.station.EvStationMapDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class EvStationServiceImpl implements EvStationService {

    private final EvStationDAO stationDAO;

    // 충전소 목록 조회
    @Override
    public List<EvChargingStationDTO> getStationList(String keyword) {
        log.info("@# StationServiceImpl.getStationList()");
        log.info("@# keyword => {}", keyword);

        return stationDAO.findStationList(keyword);
    }
    
    // 충전소 상세 조회
    @Override
    public EvChargingStationDTO getStationDetail(Long stationId) {
        log.info("@# StationServiceImpl.getStationDetail()");
        log.info("@# stationId => {}", stationId);

        return stationDAO.findStationById(stationId);
    }

    // 충전기 목록 조회
    @Override
    public List<EvChargerDTO> getChargerList(Long stationId) {
        log.info("@# StationServiceImpl.getChargerList()");
        log.info("@# stationId => {}", stationId);

        return stationDAO.findChargerListByStationId(stationId);
    }

    // 카카오맵 마커용 충전소 목록 조회
    @Override
    public List<EvStationMapDTO> getStationMapList(String keyword) {
        return getStationMapList(keyword, null);
    }

    // 카카오맵 마커용 충전소 목록 조회 - 커넥터 타입 필터 포함
    @Override
    public List<EvStationMapDTO> getStationMapList(String keyword, String connectorType) {
        log.info("@# StationServiceImpl.getStationMapList()");
        log.info("@# keyword => {}", keyword);
        log.info("@# connectorType => {}", connectorType);

        return stationDAO.findStationMapList(keyword, connectorType);
    }

    // 출발지 좌표 기준 가까운 충전소 목록 조회
    @Override
    public List<EvStationMapDTO> getStationMapListByCoordinate(String keyword,
                                                               Double latitude,
                                                               Double longitude,
                                                               int limit) {
        return getStationMapListByCoordinate(keyword, null, latitude, longitude, limit);
    }

    // 출발지 좌표 기준 가까운 충전소 목록 조회 - 커넥터 타입 필터 포함
    @Override
    public List<EvStationMapDTO> getStationMapListByCoordinate(String keyword,
                                                               String connectorType,
                                                               Double latitude,
                                                               Double longitude,
                                                               int limit) {
        log.info("@# StationServiceImpl.getStationMapListByCoordinate()");
        log.info("@# keyword => {}", keyword);
        log.info("@# connectorType => {}", connectorType);
        log.info("@# latitude => {}", latitude);
        log.info("@# longitude => {}", longitude);
        log.info("@# limit => {}", limit);

        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);

        return stationDAO.findStationMapListByCoordinate(keyword, connectorType, latitude, longitude, safeLimit);
    }

    /*
     * 기본 출발지 주변 운영중 충전소 수 조회
     */
    @Override
    public int countNearbyStationByDefaultLocation(Long memberId, int radiusMeter) {
        log.info("@# StationServiceImpl.countNearbyStationByDefaultLocation()");
        log.info("@# memberId => {}", memberId);
        log.info("@# radiusMeter => {}", radiusMeter);

        return stationDAO.countNearbyStationByDefaultLocation(memberId, radiusMeter);
    }

    /*
     * 기본 출발지 주변 사용 가능한 충전기 수 조회
     */
    @Override
    public int countAvailableChargerByDefaultLocation(Long memberId, int radiusMeter) {
        log.info("@# StationServiceImpl.countAvailableChargerByDefaultLocation()");
        log.info("@# memberId => {}", memberId);
        log.info("@# radiusMeter => {}", radiusMeter);

        return stationDAO.countAvailableChargerByDefaultLocation(memberId, radiusMeter);
    }

    /*
     * 기본 출발지 주변 추천 충전소 목록 조회
     */
    @Override
    public List<EvStationMapDTO> getNearbyStationListByDefaultLocation(Long memberId,
                                                                       int radiusMeter,
                                                                       int limit) {
        log.info("@# StationServiceImpl.getNearbyStationListByDefaultLocation()");
        log.info("@# memberId => {}", memberId);
        log.info("@# radiusMeter => {}", radiusMeter);
        log.info("@# limit => {}", limit);

        return stationDAO.findNearbyStationListByDefaultLocation(memberId, radiusMeter, limit);
    }
}






