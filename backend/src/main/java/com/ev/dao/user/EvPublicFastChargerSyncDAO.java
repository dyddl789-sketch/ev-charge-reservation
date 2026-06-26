package com.ev.dao.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.ev.dto.publicdata.EvPublicChargerAugmentTargetDTO;
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

    /*
     * 충전소별 현재 활성 충전기 수 조회
     */
    int countActiveChargersByStation(@Param("stationId") Long stationId);

    /*
     * 공공데이터 샘플 적재 보강용 충전기 생성
     */
    int upsertAugmentedCharger(@Param("stationId") Long stationId,
                               @Param("externalChargerId") String externalChargerId,
                               @Param("chargerName") String chargerName,
                               @Param("chargerCode") String chargerCode,
                               @Param("chargerType") String chargerType,
                               @Param("connectorType") String connectorType,
                               @Param("chargingSpeedKw") java.math.BigDecimal chargingSpeedKw,
                               @Param("pricePerKwh") java.math.BigDecimal pricePerKwh);

    /*
     * DB에 저장된 PUBLIC_API 충전소 전체 중 보강 대상 조회
     */
    List<EvPublicChargerAugmentTargetDTO> findPublicApiAugmentTargets();
}