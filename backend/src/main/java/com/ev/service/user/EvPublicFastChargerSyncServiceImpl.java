package com.ev.service.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ev.dao.user.EvPublicFastChargerSyncDAO;
import com.ev.dto.map.EvKakaoAddressDTO;
import com.ev.dto.publicdata.EvPublicChargerAugmentTargetDTO;
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

        int safeNumOfRows = numOfRows <= 0 ? 10 : Math.min(numOfRows, 100);
        log.info("@# safeNumOfRows => {}", safeNumOfRows);

        JsonNode root = WebClient.create(apiBaseUrl)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path(apiPath)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", safeNumOfRows)
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
     * 관리자 MIS 공공데이터 샘플 적재용 동기화
     *
     * API 응답 row를 넉넉히 받은 뒤 충전소 기준으로 그룹화한다.
     * 지역별 충전소는 maxStations개, 충전소당 충전기는 maxChargersPerStation개까지 저장한다.
     * 이렇게 하면 실제 공공데이터 기반은 유지하면서 화면에서 충전소별 충전기가 너무 1개씩만
     * 보이는 문제를 줄일 수 있다.
     */
    @Override
    public int syncRegionStationSample(int maxStations, int apiRows, String rgnNm, int maxChargersPerStation) {
        log.info("@# EvPublicFastChargerSyncServiceImpl.syncRegionStationSample()");
        log.info("@# maxStations => {}, apiRows => {}, rgnNm => {}, maxChargersPerStation => {}",
                maxStations, apiRows, rgnNm, maxChargersPerStation);

        int safeMaxStations = maxStations <= 0 ? 10 : Math.min(maxStations, 30);

        /*
         * 환경부 공공급속 충전기 API는 numOfRows 최대값이 100이다.
         * 기존에는 충전소별 2~3대 확보를 위해 apiRows=400을 그대로 넘겨 resultCode=98 오류가 발생했고,
         * 이 때문에 item이 0건으로 내려와 공공데이터 적재가 실제로 저장되지 않았다.
         *
         * 해결 방식:
         * - 한 번의 요청 numOfRows는 반드시 100 이하로 제한한다.
         * - apiRows가 100을 넘으면 pageNo를 나누어 여러 번 호출한다.
         * - 여러 페이지에서 받은 row를 합쳐 충전소 기준으로 그룹화한 뒤 저장한다.
         */
        int desiredApiRows = apiRows <= 0 ? 300 : Math.min(apiRows, 500);
        int safeRowsPerPage = Math.min(100, Math.max(20, desiredApiRows));
        int maxPageCount = Math.max(1, (int) Math.ceil((double) desiredApiRows / safeRowsPerPage));
        int safeMaxChargers = maxChargersPerStation <= 0 ? 3 : Math.min(maxChargersPerStation, 5);

        log.info("@# 공공데이터 샘플 적재 페이징 설정 desiredApiRows => {}, safeRowsPerPage => {}, maxPageCount => {}",
                desiredApiRows, safeRowsPerPage, maxPageCount);

        List<JsonNode> itemNodeList = new ArrayList<>();

        for (int pageNo = 1; pageNo <= maxPageCount; pageNo++) {
            /*
             * Java 람다식 안에서는 변경되는 지역변수 pageNo를 직접 사용할 수 없다.
             * 따라서 현재 반복 회차의 pageNo를 final 변수로 복사해서 사용한다.
             */
            final int currentPageNo = pageNo;

            JsonNode root = WebClient.create(apiBaseUrl)
                    .get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .path(apiPath)
                                .queryParam("serviceKey", serviceKey)
                                .queryParam("pageNo", currentPageNo)
                                .queryParam("numOfRows", safeRowsPerPage)
                                .queryParam("returnType", "JSON");

                        if (rgnNm != null && !rgnNm.isBlank()) {
                            uriBuilder.queryParam("rgnNm", rgnNm);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String resultCode = findApiResultCode(root);
            String resultMsg = findApiResultMsg(root);

            if (resultCode != null && !isSuccessResultCode(resultCode)) {
                log.warn("@# 공공데이터 API 오류 region => {}, pageNo => {}, numOfRows => {}, resultCode => {}, resultMsg => {}",
                        rgnNm, currentPageNo, safeRowsPerPage, resultCode, resultMsg);
                break;
            }

            List<JsonNode> pageItemNodeList = extractItems(root);
            log.info("@# 샘플 적재 API pageNo => {}, itemNodeList size => {}",
                    currentPageNo, pageItemNodeList.size());

            if (pageItemNodeList.isEmpty()) {
                log.info("@# 샘플 적재 API 데이터 없음. region => {}, pageNo => {}", rgnNm, currentPageNo);
                break;
            }

            itemNodeList.addAll(pageItemNodeList);
        }

        log.info("@# 샘플 적재 API 전체 itemNodeList size => {}", itemNodeList.size());

        Map<String, EvKakaoAddressDTO> coordinateCache = new HashMap<>();
        Set<String> failedCoordinateCache = new HashSet<>();
        Map<String, List<EvPublicFastChargerItemDTO>> stationGroupMap = new LinkedHashMap<>();

        for (JsonNode node : itemNodeList) {
            EvPublicFastChargerItemDTO item = toItem(node);
            fillCoordinateByKakao(item, coordinateCache, failedCoordinateCache);

            if (!isValidItem(item)) {
                log.warn("@# 샘플 적재 저장 제외 item => {}", item);
                continue;
            }

            stationGroupMap
                    .computeIfAbsent(item.getExternalStationId(), key -> new ArrayList<>())
                    .add(item);
        }

        int stationSaveCount = 0;
        int chargerSaveCount = 0;

        for (List<EvPublicFastChargerItemDTO> stationItems : stationGroupMap.values()) {
            if (stationSaveCount >= safeMaxStations) {
                break;
            }

            if (stationItems == null || stationItems.isEmpty()) {
                continue;
            }

            EvPublicFastChargerItemDTO stationItem = stationItems.get(0);
            Long stationId = syncDAO.upsertStation(stationItem);
            stationSaveCount++;

            int savedChargerInStation = 0;
            Set<String> savedChargerExternalIdSet = new HashSet<>();

            for (EvPublicFastChargerItemDTO chargerItem : stationItems) {
                if (savedChargerInStation >= safeMaxChargers) {
                    break;
                }

                if (!savedChargerExternalIdSet.add(chargerItem.getExternalChargerId())) {
                    continue;
                }

                syncDAO.upsertCharger(stationId, chargerItem);
                chargerSaveCount++;
                savedChargerInStation++;
            }

            /*
             * 공공데이터 API가 특정 충전소에 충전기 1개 row만 내려주는 경우가 많다.
             * MIS 시연 화면과 예약/통계 샘플 데이터가 빈약해지지 않도록 PUBLIC_API 충전소에 한해
             * 부족한 충전기를 보강 생성한다. LOCAL 충전소와 실제 사용자 데이터는 건드리지 않는다.
             */
            int activeChargerCount = syncDAO.countActiveChargersByStation(stationId);
            int augmentCount = augmentChargersForSampleStation(stationId, stationItem, activeChargerCount, safeMaxChargers);
            chargerSaveCount += augmentCount;
        }

        log.info("@# 지역별 공공데이터 샘플 적재 완료 region => {}, stationSaveCount => {}, chargerSaveCount => {}",
                rgnNm, stationSaveCount, chargerSaveCount);

        return chargerSaveCount;
    }

    /*
     * 공공데이터 샘플 충전소의 충전기 수를 목표 개수까지 보강한다.
     * 실제 API 원본 충전기가 1대만 있어도 2~3대가 보이도록 만드는 시연용 보강 로직이다.
     */
    private int augmentChargersForSampleStation(
            Long stationId,
            EvPublicFastChargerItemDTO stationItem,
            int activeChargerCount,
            int targetChargerCount) {

        log.info("@# augmentChargersForSampleStation stationId => {}, activeChargerCount => {}, targetChargerCount => {}",
                stationId, activeChargerCount, targetChargerCount);

        if (stationId == null || stationItem == null) {
            return 0;
        }

        if (activeChargerCount >= targetChargerCount) {
            return 0;
        }

        int augmentCount = 0;

        for (int chargerNo = activeChargerCount + 1; chargerNo <= targetChargerCount; chargerNo++) {
            String chargerCode = String.format("%02d", chargerNo);
            String externalChargerId = "AUGMENT-" + stationId + "-" + chargerCode;
            String chargerName = "충전기 " + chargerCode;

            ChargerSeed seed = buildAugmentedChargerSeed(chargerNo, stationItem.getChargingSpeedKw(), stationItem.getConnectorType());

            int result = syncDAO.upsertAugmentedCharger(
                    stationId,
                    externalChargerId,
                    chargerName,
                    chargerCode,
                    seed.chargerType(),
                    seed.connectorType(),
                    seed.chargingSpeedKw(),
                    seed.pricePerKwh()
            );

            augmentCount += result;
        }

        log.info("@# 보강 충전기 생성 완료 stationId => {}, augmentCount => {}", stationId, augmentCount);

        return augmentCount;
    }

    private ChargerSeed buildAugmentedChargerSeed(int chargerNo, BigDecimal baseSpeedKw, String baseConnectorType) {
        int pattern = chargerNo % 3;

        if (pattern == 0) {
            return new ChargerSeed("초급속", "DC콤보", BigDecimal.valueOf(150), BigDecimal.valueOf(410));
        }

        if (pattern == 1) {
            return new ChargerSeed("완속", "AC완속", BigDecimal.valueOf(7), BigDecimal.valueOf(260));
        }

        BigDecimal speed = baseSpeedKw != null && baseSpeedKw.compareTo(BigDecimal.ZERO) > 0
                ? baseSpeedKw.max(BigDecimal.valueOf(50))
                : BigDecimal.valueOf(100);
        String connectorType = baseConnectorType == null || baseConnectorType.isBlank() ? "DC콤보" : baseConnectorType;

        return new ChargerSeed(speed.compareTo(BigDecimal.valueOf(100)) >= 0 ? "초급속" : "급속",
                connectorType,
                speed,
                BigDecimal.valueOf(347.20));
    }

    private record ChargerSeed(
            String chargerType,
            String connectorType,
            BigDecimal chargingSpeedKw,
            BigDecimal pricePerKwh) {
    }


    /*
     * 공공데이터 적재 이후 DB에 이미 저장된 PUBLIC_API 충전소 전체를 대상으로
     * 충전기 수가 부족한 충전소를 일괄 보강한다.
     */
    @Override
    public int augmentAllPublicApiSampleChargers(int targetChargerCount) {
        log.info("@# EvPublicFastChargerSyncServiceImpl.augmentAllPublicApiSampleChargers()");
        log.info("@# targetChargerCount => {}", targetChargerCount);

        int safeTargetCount = targetChargerCount <= 0 ? 3 : Math.min(targetChargerCount, 5);
        List<EvPublicChargerAugmentTargetDTO> targetList = syncDAO.findPublicApiAugmentTargets();

        log.info("@# PUBLIC_API 보강 대상 조회 totalStationCount => {}", targetList.size());

        int targetStationCount = 0;
        int createdCount = 0;

        for (EvPublicChargerAugmentTargetDTO target : targetList) {
            if (target == null || target.getStationId() == null) {
                continue;
            }

            int activeCount = target.getActiveChargerCount();
            if (activeCount >= safeTargetCount) {
                continue;
            }

            targetStationCount++;

            EvPublicFastChargerItemDTO seedItem = new EvPublicFastChargerItemDTO();
            seedItem.setExternalStationId(target.getExternalStationId());
            seedItem.setConnectorType(target.getConnectorType());
            seedItem.setChargingSpeedKw(target.getChargingSpeedKw());

            int augmentCount = augmentChargersForSampleStation(
                    target.getStationId(),
                    seedItem,
                    activeCount,
                    safeTargetCount
            );

            createdCount += augmentCount;
        }

        log.info("@# PUBLIC_API 전체 보강 완료 targetStationCount => {}, createdCount => {}",
                targetStationCount, createdCount);

        return createdCount;
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

    private String findApiResultCode(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return null;
        }

        String resultCode = textFromPath(root.path("response").path("header").path("resultCode"));
        if (resultCode != null) {
            return resultCode;
        }

        return textFromPath(root.path("header").path("resultCode"));
    }

    private String findApiResultMsg(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return null;
        }

        String resultMsg = textFromPath(root.path("response").path("header").path("resultMsg"));
        if (resultMsg != null) {
            return resultMsg;
        }

        return textFromPath(root.path("header").path("resultMsg"));
    }

    private String textFromPath(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        String value = node.asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isSuccessResultCode(String resultCode) {
        if (resultCode == null || resultCode.isBlank()) {
            return true;
        }

        String normalizedCode = resultCode.trim();

        /*
         * 환경부 공공데이터 API는 정상 응답을 00/0뿐 아니라
         * HTTP 상태값처럼 200 + NORMAL SERVICE. 형태로 내려주는 경우가 있다.
         * 200을 오류로 판단하면 정상 응답을 받았는데도 item 추출 전에 break되어
         * 공공데이터 샘플 적재가 0건으로 끝난다.
         */
        return "00".equals(normalizedCode)
                || "0".equals(normalizedCode)
                || "200".equals(normalizedCode);
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

        int safeNumOfRows = numOfRows <= 0 ? 10 : Math.min(numOfRows, 100);
        log.info("@# safeNumOfRows => {}", safeNumOfRows);

        String response = WebClient.create(apiBaseUrl)
                .get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path(apiPath)
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", safeNumOfRows)
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