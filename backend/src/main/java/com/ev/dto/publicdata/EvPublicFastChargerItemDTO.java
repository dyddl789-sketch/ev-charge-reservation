package com.ev.dto.publicdata;

import java.math.BigDecimal;

import lombok.Data;

/*
 * 환경부 공공급속 충전기 API 응답 데이터를
 * 우리 DB 저장 형식으로 변환해서 담는 DTO
 */
@Data
public class EvPublicFastChargerItemDTO {

    // 외부 API 충전소 ID
    private String externalStationId;

    // 외부 API 충전기 ID
    private String externalChargerId;

    // 충전소명
    private String stationName;

    // 주소
    private String address;

    // 위도
    private BigDecimal latitude;

    // 경도
    private BigDecimal longitude;

    // 운영기관
    private String operatorName;

    // 운영시간
    private String useTime;

    // 상세 위치
    private String detailLocation;

    // 충전기 원본 타입
    private String chargerTypeRaw;

    // 커넥터 타입
    private String connectorType;

    // 충전 용량
    private BigDecimal chargingSpeedKw;

    // 지역명
    private String regionName;

    // 대분류
    private String largeCategoryName;

    // 소분류
    private String smallCategoryName;

    // 이용 제한
    private String userRestriction;
}