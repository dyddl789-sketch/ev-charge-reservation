package com.ev.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.admin.EvAdminStationDAO;
import com.ev.dto.map.EvKakaoAddressDTO;
import com.ev.dto.admin.EvAdminChargerFormDTO;
import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationListDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;
import com.ev.service.user.EvKakaoAddressSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminStationServiceImpl implements EvAdminStationService {

    private final EvAdminStationDAO evAdminStationDAO;
    private final EvKakaoAddressSearchService evKakaoAddressSearchService;

    @Override
    public EvAdminStationPageDTO getStationPage(EvAdminStationSearchDTO searchDTO) {

        log.info("@# EvAdminStationServiceImpl.getStationPage()");
        normalizeSearch(searchDTO);

        Long totalCount = safeLong(evAdminStationDAO.countTotalStations());
        Long activeCount = safeLong(evAdminStationDAO.countActiveStations());
        Long checkCount = safeLong(evAdminStationDAO.countCheckStations());

        Long searchCount = safeLong(evAdminStationDAO.countStationList(searchDTO));
        List<EvAdminStationListDTO> stationList = evAdminStationDAO.findStationList(searchDTO);

        int totalPage = calcTotalPage(searchCount, searchDTO.getSize());

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

        EvAdminStationFormDTO stationDTO = evAdminStationDAO.findStationForm(stationId);

        if (stationDTO == null) {
            throw new IllegalArgumentException("충전소 정보를 찾을 수 없습니다.");
        }

        stationDTO.setChargerList(evAdminStationDAO.findChargerFormList(stationId));

        return stationDTO;
    }

    @Override
    public List<EvAdminChargerFormDTO> getStationChargers(Long stationId) {
        log.info("@# EvAdminStationServiceImpl.getStationChargers()");
        log.info("@# stationId => {}", stationId);
        return evAdminStationDAO.findChargerFormList(stationId);
    }

    @Override
    @Transactional
    public EvAdminStationFormDTO registerStation(EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationServiceImpl.registerStation()");
        log.info("@# stationName => {}", stationDTO.getStationName());

        fillCoordinateIfNeeded(stationDTO);
        validateStation(stationDTO);

        evAdminStationDAO.insertStation(stationDTO);
        Long stationId = stationDTO.getStationId();

        log.info("@# inserted stationId => {}", stationId);

        if (stationDTO.getChargerList() != null) {
            for (EvAdminChargerFormDTO chargerDTO : stationDTO.getChargerList()) {
                if (chargerDTO.getChargerName() == null || chargerDTO.getChargerName().isBlank()) {
                    continue;
                }

                log.info("@# insert charger => {}", chargerDTO.getChargerName());
                evAdminStationDAO.insertCharger(stationId, chargerDTO);
            }
        }

        return getStationForm(stationId);
    }

    @Override
    @Transactional
    public EvAdminStationFormDTO updateStation(EvAdminStationFormDTO stationDTO) {

        log.info("@# EvAdminStationServiceImpl.updateStation()");
        log.info("@# stationId => {}", stationDTO.getStationId());

        if (stationDTO.getStationId() == null) {
            throw new IllegalArgumentException("충전소 번호가 필요합니다.");
        }

        fillCoordinateIfNeeded(stationDTO);
        validateStation(stationDTO);
        evAdminStationDAO.updateStation(stationDTO);

        Long stationId = stationDTO.getStationId();

        if (stationDTO.getDeleteChargerIds() != null) {
            for (Long chargerId : stationDTO.getDeleteChargerIds()) {
                log.info("@# delete charger request => {}", chargerId);
                evAdminStationDAO.deleteChargerIfUnused(stationId, chargerId);
            }
        }

        if (stationDTO.getChargerList() != null) {
            for (EvAdminChargerFormDTO chargerDTO : stationDTO.getChargerList()) {
                if (chargerDTO.getChargerName() == null || chargerDTO.getChargerName().isBlank()) {
                    continue;
                }

                if (chargerDTO.getChargerId() != null) {
                    log.info("@# update charger => {}", chargerDTO.getChargerId());
                    evAdminStationDAO.updateCharger(stationId, chargerDTO);
                } else {
                    log.info("@# insert new charger => {}", chargerDTO.getChargerName());
                    evAdminStationDAO.insertCharger(stationId, chargerDTO);
                }
            }
        }

        return getStationForm(stationId);
    }

    @Override
    @Transactional
    public void updateStationStatus(Long stationId, String stationStatus) {
        log.info("@# EvAdminStationServiceImpl.updateStationStatus()");
        log.info("@# stationId => {}, stationStatus => {}", stationId, stationStatus);

        int updateCount = evAdminStationDAO.updateStationStatus(stationId, stationStatus);
        if (updateCount == 0) {
            throw new IllegalArgumentException("충전소 상태를 변경할 수 없습니다.");
        }
    }

    @Override
    @Transactional
    public EvAdminChargerFormDTO registerCharger(Long stationId, EvAdminChargerFormDTO chargerDTO) {
        log.info("@# EvAdminStationServiceImpl.registerCharger()");
        log.info("@# stationId => {}, chargerName => {}", stationId, chargerDTO.getChargerName());

        if (stationId == null) {
            throw new IllegalArgumentException("충전소 번호가 필요합니다.");
        }

        evAdminStationDAO.insertCharger(stationId, chargerDTO);
        List<EvAdminChargerFormDTO> chargerList = evAdminStationDAO.findChargerFormList(stationId);

        return chargerList.stream()
                .filter(charger -> chargerDTO.getChargerName().equals(charger.getChargerName()))
                .reduce((first, second) -> second)
                .orElse(chargerDTO);
    }

    @Override
    @Transactional
    public void updateChargerStatus(Long chargerId, String status) {
        log.info("@# EvAdminStationServiceImpl.updateChargerStatus()");
        log.info("@# chargerId => {}, status => {}", chargerId, status);

        int updateCount = evAdminStationDAO.updateChargerStatus(chargerId, status);
        if (updateCount == 0) {
            throw new IllegalArgumentException("충전기 상태를 변경할 수 없습니다.");
        }
    }

    private void normalizeSearch(EvAdminStationSearchDTO searchDTO) {
        if (searchDTO.getPage() == null || searchDTO.getPage() < 1) {
            searchDTO.setPage(1);
        }

        if (searchDTO.getSize() == null || searchDTO.getSize() < 1) {
            searchDTO.setSize(10);
        }
    }

    private void fillCoordinateIfNeeded(EvAdminStationFormDTO stationDTO) {
        if (stationDTO.getLatitude() != null && stationDTO.getLongitude() != null) {
            return;
        }

        if (stationDTO.getAddress() == null || stationDTO.getAddress().isBlank()) {
            return;
        }

        log.info("@# 충전소 주소 기반 좌표 자동 조회 address => {}", stationDTO.getAddress());

        EvKakaoAddressDTO coordinate = evKakaoAddressSearchService.searchCoordinate(
                stationDTO.getAddress(),
                stationDTO.getStationName()
        );

        if (coordinate == null || coordinate.getLatitude() == null || coordinate.getLongitude() == null) {
            throw new IllegalArgumentException("주소로 위도/경도를 찾지 못했습니다. 주소를 다시 확인해 주세요.");
        }

        stationDTO.setLatitude(coordinate.getLatitude().doubleValue());
        stationDTO.setLongitude(coordinate.getLongitude().doubleValue());

        log.info("@# 충전소 좌표 자동 설정 latitude => {}, longitude => {}",
                stationDTO.getLatitude(), stationDTO.getLongitude());
    }

    private void validateStation(EvAdminStationFormDTO stationDTO) {
        if (stationDTO.getStationName() == null || stationDTO.getStationName().isBlank()) {
            throw new IllegalArgumentException("충전소명을 입력해 주세요.");
        }

        if (stationDTO.getAddress() == null || stationDTO.getAddress().isBlank()) {
            throw new IllegalArgumentException("주소를 입력해 주세요.");
        }

        if (stationDTO.getLatitude() == null || stationDTO.getLongitude() == null) {
            throw new IllegalArgumentException("주소 기반 좌표를 찾지 못했습니다. 주소를 다시 확인해 주세요.");
        }

        if (stationDTO.getOpenTime() == null || stationDTO.getOpenTime().isBlank()) {
            stationDTO.setOpenTime("00:00");
        }

        if (stationDTO.getCloseTime() == null || stationDTO.getCloseTime().isBlank()) {
            stationDTO.setCloseTime("23:59");
        }

        if (stationDTO.getStationStatus() == null || stationDTO.getStationStatus().isBlank()) {
            stationDTO.setStationStatus("운영중");
        }
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int calcTotalPage(Long totalCount, Integer size) {
        if (totalCount == null || totalCount == 0) {
            return 1;
        }

        return (int) Math.ceil(totalCount / (double) size);
    }
}
