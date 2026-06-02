package com.ev.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.admin.EvAdminStationDAO;
import com.ev.dto.admin.EvAdminChargerFormDTO;
import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationListDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminStationServiceImpl implements EvAdminStationService {

    private final EvAdminStationDAO evAdminStationDAO;

    @Override
    public EvAdminStationPageDTO getStationPage(EvAdminStationSearchDTO searchDTO) {

        log.info("@# EvAdminStationServiceImpl.getStationPage()");

        Long totalCount = evAdminStationDAO.countTotalStations();
        Long activeCount = evAdminStationDAO.countActiveStations();
        Long checkCount = evAdminStationDAO.countCheckStations();

        Long searchCount = evAdminStationDAO.countStationList(searchDTO);
        List<EvAdminStationListDTO> stationList =
                evAdminStationDAO.findStationList(searchDTO);

        int size = searchDTO.getSize();
        int totalPage = (int) Math.ceil((double) searchCount / size);

        EvAdminStationPageDTO pageDTO = new EvAdminStationPageDTO();

        pageDTO.setTotalCount(totalCount);
        pageDTO.setActiveCount(activeCount);
        pageDTO.setCheckCount(checkCount);
        pageDTO.setSearchCount(searchCount);
        pageDTO.setPage(searchDTO.getPage());
        pageDTO.setSize(searchDTO.getSize());
        pageDTO.setTotalPage(totalPage);
        pageDTO.setStationList(stationList);

        return pageDTO;
    }

    @Override
    public EvAdminStationFormDTO getStationForm(Long stationId) {

        log.info("@# EvAdminStationServiceImpl.getStationForm()");
        log.info("@# stationId => {}", stationId);

        EvAdminStationFormDTO stationDTO =
                evAdminStationDAO.findStationForm(stationId);

        if (stationDTO == null) {
            throw new IllegalArgumentException("충전소 정보를 찾을 수 없습니다.");
        }

        List<EvAdminChargerFormDTO> chargerList =
                evAdminStationDAO.findChargerFormList(stationId);

        stationDTO.setChargerList(chargerList);

        return stationDTO;
    }

    @Override
    @Transactional
    public void registerStation(EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationServiceImpl.registerStation()");
        log.info("@# stationName => {}", stationDTO.getStationName());

        evAdminStationDAO.insertStation(stationDTO);

        Long stationId = stationDTO.getStationId();

        log.info("@# inserted stationId => {}", stationId);

        if (stationDTO.getChargerList() == null
                || stationDTO.getChargerList().isEmpty()) {

            log.info("@# chargerList empty");
            return;
        }

        for (EvAdminChargerFormDTO chargerDTO : stationDTO.getChargerList()) {

            log.info("@# insert charger => {}", chargerDTO.getChargerName());

            evAdminStationDAO.insertCharger(stationId, chargerDTO);
        }
    }

    @Override
    @Transactional
    public void updateStation(EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationServiceImpl.updateStation()");
        log.info("@# stationId => {}", stationDTO.getStationId());

        // 충전소 기본 정보 수정
        evAdminStationDAO.updateStation(stationDTO);

        Long stationId = stationDTO.getStationId();

        // 삭제 요청된 기존 충전기 처리
        if (stationDTO.getDeleteChargerIds() != null) {

            for (Long chargerId : stationDTO.getDeleteChargerIds()) {

                log.info("@# delete charger request => {}", chargerId);

                int deleteCount =
                        evAdminStationDAO.deleteChargerIfUnused(stationId, chargerId);

                log.info("@# delete charger result => {}", deleteCount);
            }
        }

        if (stationDTO.getChargerList() == null
                || stationDTO.getChargerList().isEmpty()) {

            log.info("@# chargerList empty");
            return;
        }

        for (EvAdminChargerFormDTO chargerDTO : stationDTO.getChargerList()) {

            if (chargerDTO.getChargerId() != null) {

                // 기존 충전기 수정: charger_id 유지
                log.info("@# update charger => {}", chargerDTO.getChargerId());

                evAdminStationDAO.updateCharger(stationId, chargerDTO);

            } else {

                // 새 충전기 추가
                log.info("@# insert new charger => {}", chargerDTO.getChargerName());

                evAdminStationDAO.insertCharger(stationId, chargerDTO);
            }
        }
    }
}