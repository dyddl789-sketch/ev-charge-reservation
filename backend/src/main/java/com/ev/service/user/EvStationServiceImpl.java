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
        log.info("@# StationServiceImpl.getStationMapList()");
        log.info("@# keyword => {}", keyword);


        return stationDAO.findStationMapList(keyword);
    }
}






