package com.ev.service.user;

/*
 * 환경부 공공급속 충전기 API 동기화 서비스
 */
public interface EvPublicFastChargerSyncService {

    /*
     * 한 페이지 동기화
     */
    int syncPage(int pageNo, int numOfRows, String rgnNm);

    /*
     * 관리자 MIS 샘플 적재용: API row를 넉넉히 받은 뒤 충전소 기준으로 묶어서
     * 지역별 충전소 수와 충전소당 충전기 수를 제한해 저장한다.
     */
    int syncRegionStationSample(int maxStations, int apiRows, String rgnNm, int maxChargersPerStation);

    /*
     * DB에 저장된 PUBLIC_API 샘플 충전소 전체를 대상으로 부족 충전기를 보강한다.
     */
    int augmentAllPublicApiSampleChargers(int targetChargerCount);

    /*
     * 여러 페이지 동기화
     */
    int syncAll(int maxPages, int numOfRows, String rgnNm);

    /*
     * 환경부 API 원본 응답 확인용
     */
    String getRawResponse(int pageNo, int numOfRows, String rgnNm);
}