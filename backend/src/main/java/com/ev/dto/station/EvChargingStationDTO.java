package com.ev.dto.station;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;


@Data
public class EvChargingStationDTO {

    // 충전소 고유 번호
    private Long stationId;

    // 충전소 이름
    private String stationName;

    // 충전소 주소
    private String address;

    // 위도 
    private BigDecimal latitude;

    // 경도
    private BigDecimal longitude;

    // 사업자명, 운영기관
    private String operatorName;

    // 오픈 시간
    private LocalTime openTime;

    // 마감 시간
    private LocalTime closeTime;

    // 충전소 상태(운영중 / 점검중 / 운영중지)
    private String stationStatus;

    // 등록일
    private LocalDateTime createdAt;
    
    // 수정일
    private LocalDateTime updatedAt;

    /*
     * 화면 표시용 필드
     *
     * charging_station 테이블에는 없는 컬럼.
     * 충전소 목록 조회 시 charger 테이블을 count 해서 담을 값.
     */
    private Integer chargerCount;

    /*
     * 화면 표시용 필드
     *
     * 사용 가능한 충전기 수.
     * charger.status = '사용가능' 인 충전기 개수.
     */
    private Integer availableChargerCount;
}