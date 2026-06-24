package com.ev.dao.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.publicdata.EvPublicFastChargerItemDTO;

/*
 * 환경부 공공급속 충전기 API 데이터를 DB에 저장하는 DAO
 */
@Mapper
public interface EvPublicFastChargerSyncDAO {

    /*
     * 충전소 저장 또는 수정
     * external_station_id 기준으로 insert/update 처리
     */
    Long upsertStation(@Param("item") EvPublicFastChargerItemDTO item);

    /*
     * 충전기 저장 또는 수정
     * external_charger_id 기준으로 insert/update 처리
     */
    int upsertCharger(@Param("stationId") Long stationId,
                      @Param("item") EvPublicFastChargerItemDTO item);
}