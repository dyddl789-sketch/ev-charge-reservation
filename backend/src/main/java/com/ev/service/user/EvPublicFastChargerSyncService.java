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
     * 여러 페이지 동기화
     */
    int syncAll(int maxPages, int numOfRows, String rgnNm);

    /*
     * 환경부 API 원본 응답 확인용
     */
    String getRawResponse(int pageNo, int numOfRows, String rgnNm);
}