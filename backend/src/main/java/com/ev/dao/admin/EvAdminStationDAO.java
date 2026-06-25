package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.EvAdminChargerFormDTO;
import com.ev.dto.admin.EvAdminStationFormDTO;
import com.ev.dto.admin.EvAdminStationListDTO;
import com.ev.dto.admin.EvAdminStationSearchDTO;

@Mapper
public interface EvAdminStationDAO {

    Long countTotalStations();

    Long countActiveStations();

    Long countCheckStations();

    Long countStationList(EvAdminStationSearchDTO searchDTO);

    List<EvAdminStationListDTO> findStationList(EvAdminStationSearchDTO searchDTO);

    // 충전소 등록
    void insertStation(EvAdminStationFormDTO stationDTO);

    // 충전기 등록
    void insertCharger(
            @Param("stationId") Long stationId,
            @Param("charger") EvAdminChargerFormDTO chargerDTO
    );

    // 수정/상세 화면용 충전소 정보 조회
    EvAdminStationFormDTO findStationForm(Long stationId);

    // 수정/상세 화면용 충전기 목록 조회
    List<EvAdminChargerFormDTO> findChargerFormList(Long stationId);

    // 충전소 정보 수정
    void updateStation(EvAdminStationFormDTO stationDTO);

    // 충전소 상태 수정
    int updateStationStatus(@Param("stationId") Long stationId,
                            @Param("stationStatus") String stationStatus);

    // 기존 충전기 수정
    void updateCharger(
            @Param("stationId") Long stationId,
            @Param("charger") EvAdminChargerFormDTO chargerDTO
    );

    // 충전기 상태 수정
    int updateChargerStatus(@Param("chargerId") Long chargerId,
                            @Param("status") String status);

    // 예약/충전 이력이 없는 충전기만 삭제
    int deleteChargerIfUnused(
            @Param("stationId") Long stationId,
            @Param("chargerId") Long chargerId
    );
}
