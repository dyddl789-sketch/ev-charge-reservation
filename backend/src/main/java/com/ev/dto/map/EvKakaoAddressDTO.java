package com.ev.dto.map;

import java.math.BigDecimal;

import lombok.Data;

/*
 * Kakao 주소검색 API 좌표 결과 DTO
 */
@Data
public class EvKakaoAddressDTO {

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}