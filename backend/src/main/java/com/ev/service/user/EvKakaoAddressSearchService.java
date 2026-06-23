package com.ev.service.user;

import com.ev.dto.map.EvKakaoAddressDTO;

/*
 * Kakao 주소/장소 검색 API 서비스
 */
public interface EvKakaoAddressSearchService {

    /*
     * 주소만으로 위도/경도 변환
     */
    EvKakaoAddressDTO searchCoordinate(String address);

    /*
     * 주소 + 장소명으로 위도/경도 변환
     *
     * 주소검색 실패 시
     * 장소명 기반 키워드 검색까지 시도한다.
     */
    EvKakaoAddressDTO searchCoordinate(String address, String placeName);
}