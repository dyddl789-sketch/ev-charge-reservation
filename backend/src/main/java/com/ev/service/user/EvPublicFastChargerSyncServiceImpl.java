package com.ev.service.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ev.dao.user.EvPublicFastChargerSyncDAO;
import com.ev.dto.map.EvKakaoAddressDTO;
import com.ev.dto.publicdata.EvPublicFastChargerItemDTO;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 환경부 공공급속 충전기 API 동기화 구현체
 *
 * 처리 흐름:
 * 1. 환경부 API 호출
 * 2. 충전소/충전기 데이터 추출
 * 3. 주소는 있지만 좌표가 없으면 Kakao 주소검색 API로 좌표 보강
 * 4. charging_station / charger 테이블에 저장 또는 수정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvPublicFastChargerSyncServiceImpl implements EvPublicFastChargerSyncService {

    private final EvPublicFastChargerSyncDAO syncDAO;

    private final EvKakaoAddressSearchService kakaoAddressSearchService;

    @Value("${public.ev.fast.api.base-url}")
    private String apiBaseUrl;

    @Value("${public.ev.fast.api.path}")
    private String apiPath;

    @Value("${public.ev.fast.api.service-key}")
    private String serviceKey;

    /*
     * 한 페이지 동기화
     */
    @Override
    public int syncPage(int pageNo, int numOfRows, String rgnNm) {
        log.info("@# EvPublicFastChargerSyncServiceImpl.syncPage()");
        log.info("@# pageNo => {}", pageNo);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        JsonNode root = WebClient.create(apiBaseUrl)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path(apiPath)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("returnType", "JSON");

                    if (rgnNm != null && !rgnNm.isBlank()) {
                        uriBuilder.queryParam("rgnNm", rgnNm);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        log.info("@# 환경부 API 응답 root => {}", root);

        List<JsonNode> itemNodeList = extractItems(root);

        log.info("@# 환경부 API itemNodeList size => {}", itemNodeList.size());

        int saveCount = 0;

        /*
         * 같은 주소가 여러 충전기에 반복될 수 있으므로
         * Kakao 주소검색 결과를 페이지 단위로 캐싱한다.
         */
        Map<String, EvKakaoAddressDTO> coordinateCache = new HashMap<>();

	     // Kakao 검색 실패 주소도 캐싱해서 같은 주소를 반복 호출하지 않도록 처리
	     Set<String> failedCoordinateCache = new HashSet<>();

        for (JsonNode node : itemNodeList) {
            log.info("@# 환경부 API item raw => {}", node);

            EvPublicFastChargerItemDTO item = toItem(node);

            /*
             * 환경부 API에 위도/경도가 없으면
             * Kakao 주소검색 API로 좌표를 보강한다.
             */
            fillCoordinateByKakao(item, coordinateCache, failedCoordinateCache);

            if (!isValidItem(item)) {
                log.warn("@# 저장 제외 item => {}", item);
                continue;
            }

            Long stationId = syncDAO.upsertStation(item);

            log.info("@# upsert stationId => {}", stationId);

            syncDAO.upsertCharger(stationId, item);

            saveCount++;
        }

        log.info("@# 환경부 공공급속 충전기 동기화 완료 saveCount => {}", saveCount);

        return saveCount;
    }

    /*
     * 여러 페이지 동기화
     */
    @Override
    public int syncAll(int maxPages, int numOfRows, String rgnNm) {
        log.info("@# EvPublicFastChargerSyncServiceImpl.syncAll()");
        log.info("@# maxPages => {}", maxPages);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        int totalSaveCount = 0;

        for (int pageNo = 1; pageNo <= maxPages; pageNo++) {
            int saveCount = syncPage(pageNo, numOfRows, rgnNm);

            totalSaveCount += saveCount;

            if (saveCount == 0) {
                log.info("@# 저장 데이터가 없어 동기화 종료 pageNo => {}", pageNo);
                break;
            }
        }

        log.info("@# 환경부 공공급속 충전기 전체 동기화 완료 totalSaveCount => {}", totalSaveCount);

        return totalSaveCount;
    }

    /*
     * 공공데이터 응답 구조에서 item 배열 추출
     *
     * 현재 API 실제 구조:
     * {
     *   "header": {...},
     *   "body": {
     *      "items": [...]
     *   }
     * }
     */
    private List<JsonNode> extractItems(JsonNode root) {
        List<JsonNode> itemNodeList = new ArrayList<>();

        if (root == null || root.isNull() || root.isMissingNode()) {
            log.warn("@# 환경부 API 응답 root가 비어있음");
            return itemNodeList;
        }

        log.info("@# extractItems root => {}", root);

        JsonNode items = root.path("response").path("body").path("items").path("item");

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("response").path("body").path("items");
        }

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("body").path("items").path("item");
        }

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("body").path("items");
        }

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("body").path("item");
        }

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("items").path("item");
        }

        if (items.isMissingNode() || items.isNull()) {
            items = root.path("items");
        }

        log.info("@# extractItems items => {}", items);

        if (items.isArray()) {
            for (JsonNode item : items) {
                itemNodeList.add(item);
            }
        } else if (items.isObject()) {
            itemNodeList.add(items);
        }

        log.info("@# extractItems itemNodeList size => {}", itemNodeList.size());

        return itemNodeList;
    }

    /*
     * API 응답 item을 우리 DTO로 변환
     *
     * 현재 확인된 환경부 API 주요 필드:
     * - rgnNm       지역명
     * - sggNm       시군구
     * - roadNmAddr  도로명주소
     * - lclsfNm     대분류
     * - sclsfNm     소분류
     * - chgstnNm    충전소명
     * - chgstnId    충전소 ID
     * - chrgrId     충전기 ID
     * - chrgrCpct   충전용량
     * - chrgrFrm    충전기 형식
     */
    private EvPublicFastChargerItemDTO toItem(JsonNode node) {
        log.info("@# toItem node => {}", node);

        EvPublicFastChargerItemDTO item = new EvPublicFastChargerItemDTO();

        String stationName = textAny(
                node,
                "chgstnNm",
                "statNm",
                "stationName",
                "충전소명"
        );

        String address = textAny(
                node,
                "roadNmAddr",
                "roadnAddr",
                "lotnoAddr",
                "addr",
                "address",
                "주소"
        );

        String stationId = textAny(
                node,
                "chgstnId",
                "ogshnId",
                "statId",
                "stationId",
                "충전소ID"
        );

        String chargerId = textAny(
                node,
                "chrgrId",
                "chgrId",
                "chgerId",
                "chargerId",
                "충전기ID"
        );

        BigDecimal latitude = decimalAny(
                node,
                "lat",
                "latitude",
                "lati",
                "위도"
        );

        BigDecimal longitude = decimalAny(
                node,
                "lng",
                "lon",
                "longitude",
                "longi",
                "경도"
        );

        if (latitude == null || longitude == null) {
            BigDecimal[] latLng = parseLatLng(textAny(
                    node,
                    "latLot",
                    "latlng",
                    "latLng",
                    "위도경도"
            ));

            latitude = latLng[0];
            longitude = latLng[1];
        }

        BigDecimal chargingSpeedKw = decimalAny(
                node,
                "chrgrCpct",
                "chgrCpct",
                "chgcpcy",
                "chgCpct",
                "chargingCapacity",
                "output",
                "급속충전량",
                "충전용량"
        );

        if (chargingSpeedKw == null) {
            chargingSpeedKw = BigDecimal.valueOf(50);
        }

        String chargerTypeRaw = textAny(
                node,
                "chrgrFrm",
                "chgrFrm",
                "chrgrType",
                "chgerType",
                "chargerType",
                "충전기타입"
        );

        item.setStationName(stationName);
        item.setAddress(address);
        item.setLatitude(latitude);
        item.setLongitude(longitude);

        item.setExternalStationId(makeStationExternalId(stationId, stationName, address));
        item.setExternalChargerId(makeChargerExternalId(
                item.getExternalStationId(),
                chargerId,
                chargerTypeRaw
        ));

        String operatorName = textAny(
                node,
                "slofsNm",
                "oprtrInstNm",
                "busiNm",
                "operatorName",
                "운영기관",
                "운영기관명"
        );

        if (operatorName == null || operatorName.isBlank()) {
            operatorName = "환경부 공공데이터";
        }

        item.setOperatorName(operatorName);

        item.setUseTime(textAny(
                node,
                "useTime",
                "openTime",
                "이용시간",
                "운영시간"
        ));

        item.setDetailLocation(textAny(
                node,
                "location",
                "detailLocation",
                "dtlAddr",
                "상세위치"
        ));

        item.setChargerTypeRaw(chargerTypeRaw);
        item.setConnectorType(convertConnectorType(chargerTypeRaw));
        item.setChargingSpeedKw(chargingSpeedKw);

        item.setRegionName(textAny(
                node,
                "rgnNm",
                "sido",
                "시도",
                "지역명"
        ));

        item.setLargeCategoryName(textAny(
                node,
                "lclsfNm",
                "시설구분대",
                "대분류"
        ));

        item.setSmallCategoryName(textAny(
                node,
                "sclsfNm",
                "시설구분소",
                "소분류"
        ));

        item.setUserRestriction(textAny(
                node,
                "userLmt",
                "이용자제한"
        ));

        log.info("@# 변환된 item => {}", item);

        return item;
    }

    /*
     * 환경부 API에 위도/경도가 없을 때
     * Kakao 주소검색/키워드검색 API로 좌표를 보강한다.
     */
    private void fillCoordinateByKakao(
            EvPublicFastChargerItemDTO item,
            Map<String, EvKakaoAddressDTO> coordinateCache,
            Set<String> failedCoordinateCache) {

        log.info("@# fillCoordinateByKakao()");

        if (item == null) {
            log.warn("@# item is null");
            return;
        }

        if (item.getLatitude() != null && item.getLongitude() != null) {
            log.info("@# 이미 좌표가 있어 Kakao 검색 생략");
            return;
        }

        String address = item.getAddress();
        String stationName = item.getStationName();

        if ((address == null || address.isBlank())
                && (stationName == null || stationName.isBlank())) {
            log.warn("@# 주소와 충전소명이 없어 Kakao 검색 불가 item => {}", item);
            return;
        }

        String cacheKey = String.valueOf(address) + "|" + String.valueOf(stationName);

        if (failedCoordinateCache.contains(cacheKey)) {
            log.warn("@# 이전에 Kakao 좌표 변환 실패한 항목이라 재검색 생략 cacheKey => {}", cacheKey);
            return;
        }

        EvKakaoAddressDTO coordinate = coordinateCache.get(cacheKey);

        if (coordinate == null) {
            log.info("@# Kakao 좌표 검색 실행 address => {}, stationName => {}",
                    address,
                    stationName);

            coordinate = kakaoAddressSearchService.searchCoordinate(address, stationName);

            if (coordinate != null) {
                coordinateCache.put(cacheKey, coordinate);
            } else {
                failedCoordinateCache.add(cacheKey);
            }
        } else {
            log.info("@# Kakao 좌표 캐시 사용 cacheKey => {}", cacheKey);
        }

        if (coordinate == null) {
            log.warn("@# Kakao 좌표 변환 실패 address => {}, stationName => {}",
                    address,
                    stationName);
            return;
        }

        item.setLatitude(coordinate.getLatitude());
        item.setLongitude(coordinate.getLongitude());

        log.info("@# Kakao 좌표 보강 완료 address => {}, stationName => {}, lat => {}, lng => {}",
                address,
                stationName,
                item.getLatitude(),
                item.getLongitude());
    }

    /*
     * 필수값 검증
     */
    private boolean isValidItem(EvPublicFastChargerItemDTO item) {
        if (item == null) {
            log.warn("@# 저장 제외 사유: item 없음");
            return false;
        }

        if (item.getExternalStationId() == null) {
            log.warn("@# 저장 제외 사유: externalStationId 없음");
            return false;
        }

        if (item.getExternalChargerId() == null) {
            log.warn("@# 저장 제외 사유: externalChargerId 없음");
            return false;
        }

        if (item.getStationName() == null) {
            log.warn("@# 저장 제외 사유: stationName 없음");
            return false;
        }

        if (item.getAddress() == null) {
            log.warn("@# 저장 제외 사유: address 없음");
            return false;
        }

        if (item.getLatitude() == null || item.getLongitude() == null) {
            log.warn("@# 저장 제외 사유: 위도/경도 없음, address => {}", item.getAddress());
            return false;
        }

        return true;
    }

    /*
     * 충전소 외부 ID 생성
     */
    private String makeStationExternalId(String stationId, String stationName, String address) {
        if (stationId != null && !stationId.isBlank()) {
            return stationId;
        }

        String source = String.valueOf(stationName) + "|" + String.valueOf(address);

        return "PUBLIC-ST-" + Math.abs(source.hashCode());
    }

    /*
     * 충전기 외부 ID 생성
     */
    private String makeChargerExternalId(String stationExternalId, String chargerId, String chargerTypeRaw) {
        if (chargerId != null && !chargerId.isBlank()) {
            return stationExternalId + "-" + chargerId;
        }

        String source = String.valueOf(stationExternalId) + "|" + String.valueOf(chargerTypeRaw);

        return stationExternalId + "-CH-" + Math.abs(source.hashCode());
    }

    /*
     * 충전기 타입을 우리 DB 커넥터 타입으로 변환
     */
    private String convertConnectorType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DC콤보";
        }

        if (raw.contains("NACS")) {
            return "NACS";
        }

        /*
         * 복합 타입 예:
         * DC차데모+AC3상+DC콤보
         *
         * AC가 포함되어 있어도 DC콤보가 있으면
         * 예약/AI 추천 기준 커넥터를 DC콤보로 우선 저장한다.
         */
        if (raw.contains("콤보")) {
            return "DC콤보";
        }

        if (raw.contains("차데모")) {
            return "CHAdeMO";
        }

        if (raw.contains("AC")) {
            return "AC완속";
        }

        return raw;
    }

    /*
     * 여러 필드명 후보 중 값이 있는 첫 번째 문자열 반환
     */
    private String textAny(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = node.path(fieldName).asText(null);

            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    /*
     * 여러 필드명 후보 중 숫자값 반환
     */
    private BigDecimal decimalAny(JsonNode node, String... fieldNames) {
        String value = textAny(node, fieldNames);

        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (Exception e) {
            log.warn("@# 숫자 변환 실패 value => {}", value);
            return null;
        }
    }

    /*
     * 위도경도 문자열 파싱
     */
    private BigDecimal[] parseLatLng(String value) {
        BigDecimal[] result = new BigDecimal[] { null, null };

        if (value == null || value.isBlank()) {
            return result;
        }

        String cleaned = value
                .replace("(", "")
                .replace(")", "")
                .replace("[", "")
                .replace("]", "")
                .trim();

        String[] parts = cleaned.split(",");

        if (parts.length < 2) {
            return result;
        }

        try {
            result[0] = new BigDecimal(parts[0].trim());
            result[1] = new BigDecimal(parts[1].trim());
        } catch (Exception e) {
            log.warn("@# 위도경도 변환 실패 value => {}", value);
        }

        return result;
    }

    /*
     * 환경부 API 원본 응답 확인용
     */
    @Override
    public String getRawResponse(int pageNo, int numOfRows, String rgnNm) {
        log.info("@# EvPublicFastChargerSyncServiceImpl.getRawResponse()");
        log.info("@# pageNo => {}", pageNo);
        log.info("@# numOfRows => {}", numOfRows);
        log.info("@# rgnNm => {}", rgnNm);

        String response = WebClient.create(apiBaseUrl)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path(apiPath)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", numOfRows)
                            .queryParam("returnType", "JSON");

                    if (rgnNm != null && !rgnNm.isBlank()) {
                        uriBuilder.queryParam("rgnNm", rgnNm);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("@# 환경부 API raw response => {}", response);

        return response;
    }
}