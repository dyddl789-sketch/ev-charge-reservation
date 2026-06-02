package com.ev.service.admin;

import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationPageDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;

public interface EvAdminStationService {

    EvAdminStationPageDTO getStationPage(EvAdminStationSearchDTO searchDTO);

    EvAdminStationFormDTO getStationForm(Long stationId);

    void registerStation(EvAdminStationFormDTO stationDTO);

    void updateStation(EvAdminStationFormDTO stationDTO);
}