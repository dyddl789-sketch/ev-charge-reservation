package com.ev.controller.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.service.user.EvPublicFastChargerSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 환경부 공공급속 충전기 API 수동 동기화 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/public-api/ev-fast-charger")
@RequiredArgsConstructor
public class EvPublicFastChargerSyncController {

    private final EvPublicFastChargerSyncService syncService;

    /*
     * 한 페이지 수동 동기화
     *
     * 예:
     * /public-api/ev-fast-charger/sync?pageNo=1&numOfRows=10&rgnNm=부산광역시
     */
    @GetMapping("/sync")
    public Map<String, Object> sync(
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "numOfRows", defaultValue = "10") int numOfRows,
            @RequestParam(name = "rgnNm", required = false) String rgnNm) {

        log.info("@# EvPublicFastChargerSyncController.sync()");
        log.info("@# pageNo => {}", pageNo);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        int saveCount = syncService.syncPage(pageNo, numOfRows, rgnNm);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "환경부 공공급속 충전기 API 동기화 완료");
        result.put("pageNo", pageNo);
        result.put("numOfRows", numOfRows);
        result.put("rgnNm", rgnNm);
        result.put("saveCount", saveCount);

        log.info("@# result => {}", result);

        return result;
    }

    /*
     * 여러 페이지 수동 동기화
     *
     * 예:
     * /public-api/ev-fast-charger/sync-all?maxPages=5&numOfRows=100&rgnNm=부산광역시
     */
    @GetMapping("/sync-all")
    public Map<String, Object> syncAll(
            @RequestParam(name = "maxPages", defaultValue = "5") int maxPages,
            @RequestParam(name = "numOfRows", defaultValue = "100") int numOfRows,
            @RequestParam(name = "rgnNm", required = false) String rgnNm) {

        log.info("@# EvPublicFastChargerSyncController.syncAll()");
        log.info("@# maxPages => {}", maxPages);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        int saveCount = syncService.syncAll(maxPages, numOfRows, rgnNm);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "환경부 공공급속 충전기 API 전체 동기화 완료");
        result.put("maxPages", maxPages);
        result.put("numOfRows", numOfRows);
        result.put("rgnNm", rgnNm);
        result.put("saveCount", saveCount);

        log.info("@# result => {}", result);

        return result;
    }
    
    /*
     * 환경부 API 원본 응답 확인용
     *
     * 예:
     * /public-api/ev-fast-charger/raw?pageNo=1&numOfRows=10
     */
    @GetMapping(value = "/raw", produces = "application/json;charset=UTF-8")
    public String raw(
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "numOfRows", defaultValue = "10") int numOfRows,
            @RequestParam(name = "rgnNm", required = false) String rgnNm) {

        log.info("@# EvPublicFastChargerSyncController.raw()");
        log.info("@# pageNo => {}", pageNo);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        return syncService.getRawResponse(pageNo, numOfRows, rgnNm);
    }
}