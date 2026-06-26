package com.ev.service.admin;

import java.util.List;

import com.ev.dto.admin.EvAdminChargerFormDTO;
import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;

public interface EvAdminStationService {

    EvAdminStationPageDTO getStationPage(EvAdminStationSearchDTO searchDTO);

    EvAdminStationFormDTO getStationForm(Long stationId);

    List<EvAdminChargerFormDTO> getStationChargers(Long stationId);

    EvAdminStationFormDTO registerStation(EvAdminStationFormDTO stationDTO);

    EvAdminStationFormDTO updateStation(EvAdminStationFormDTO stationDTO);

    void updateStationStatus(Long stationId, String stationStatus);

    EvAdminChargerFormDTO registerCharger(Long stationId, EvAdminChargerFormDTO chargerDTO);

    void updateChargerStatus(Long chargerId, String status);
}
