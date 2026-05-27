package com.ev.dao.user;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvChargingStationDTO;
import com.ev.dto.station.EvStationMapDTO;

@Mapper
public interface EvStationDAO {

    /*
     충전소 목록 조회
     keyword가 없으면 전체 충전소 조회.
     keyword가 있으면 충전소명, 주소, 운영기관명으로 검색.
	*/
    List<EvChargingStationDTO> findStationList(@Param("keyword") String keyword);

    // 충전소 상세 조회
   
    EvChargingStationDTO findStationById(@Param("stationId") Long stationId);

    // 특정 충전소에 속한 충전기 목록 조회
     
    List<EvChargerDTO> findChargerListByStationId(@Param("stationId") Long stationId);
    
    // 카카오맵에 표시할 충전소 목록 조회
    List<EvStationMapDTO> findStationMapList(@Param("keyword") String keyword);
}