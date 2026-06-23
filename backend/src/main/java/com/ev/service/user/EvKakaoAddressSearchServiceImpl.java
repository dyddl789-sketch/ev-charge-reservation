package com.ev.service.user;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ev.dto.map.EvKakaoAddressDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/*
 * Kakao 주소/장소 검색 API 구현체
 *
 * 중요:
 * RestTemplate에 String URL을 넘기면 URL이 다시 처리되면서
 * Kakao가 인코딩된 문자열을 query로 받을 수 있다.
 *
 * 그래서 반드시 URI 객체로 만들어서 요청한다.
 */
@Slf4j
@Service
public class EvKakaoAddressSearchServiceImpl implements EvKakaoAddressSearchService {

    @Value("${kakao.rest-api.key}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * 주소만으로 위도/경도 변환
     */
    @Override
    public EvKakaoAddressDTO searchCoordinate(String address) {
        return searchCoordinate(address, null);
    }

    /*
     * 주소 + 장소명으로 위도/경도 변환
     */
    @Override
    public EvKakaoAddressDTO searchCoordinate(String address, String placeName) {
        log.info("@# EvKakaoAddressSearchServiceImpl.searchCoordinate()");
        log.info("@# address => {}", address);
        log.info("@# placeName => {}", placeName);

        if ((address == null || address.isBlank())
                && (placeName == null || placeName.isBlank())) {
            log.warn("@# 주소와 장소명이 모두 비어있어 Kakao 검색 불가");
            return null;
        }

        String normalizedAddress = normalizeKeyword(address);
        String normalizedPlaceName = normalizeKeyword(placeName);
        String shortAddress = makeShortAddress(normalizedAddress);
        String regionKeyword = makeRegionKeyword(normalizedAddress);

        /*
         * 1차: 전체 주소 검색
         */
        EvKakaoAddressDTO result = searchByAddressApi(normalizedAddress, "전체 주소 검색");

        if (result != null) {
            return result;
        }

        /*
         * 2차: 짧은 주소 검색
         */
        if (shortAddress != null && !shortAddress.equals(normalizedAddress)) {
            result = searchByAddressApi(shortAddress, "짧은 주소 검색");

            if (result != null) {
                return result;
            }
        }

        /*
         * 3차: 키워드 검색 후보
         */
        Set<String> keywordSet = new LinkedHashSet<>();

        addKeyword(keywordSet, combineKeyword(normalizedPlaceName, regionKeyword));
        addKeyword(keywordSet, combineKeyword(removeSpaces(normalizedPlaceName), regionKeyword));
        addKeyword(keywordSet, combineKeyword(normalizedPlaceName, shortAddress));
        addKeyword(keywordSet, normalizedPlaceName);
        addKeyword(keywordSet, removeSpaces(normalizedPlaceName));
        addKeyword(keywordSet, normalizedAddress);
        addKeyword(keywordSet, shortAddress);

        for (String keyword : keywordSet) {
            result = searchByKeywordApi(keyword, "키워드 검색");

            if (result != null) {
                return result;
            }
        }

        log.warn("@# Kakao 주소/키워드 검색 모두 실패 address => {}, placeName => {}",
                address,
                placeName);

        return null;
    }

    /*
     * Kakao 주소 검색 API
     */
    private EvKakaoAddressDTO searchByAddressApi(String address, String searchStepName) {
        log.info("@# searchByAddressApi()");
        log.info("@# searchStepName => {}", searchStepName);
        log.info("@# address => {}", address);

        if (address == null || address.isBlank()) {
            log.warn("@# 주소가 없어 주소 검색 API 생략 searchStepName => {}", searchStepName);
            return null;
        }

        try {
            String safeAddress = makeSafeKeyword(address);

            if (safeAddress == null || safeAddress.isBlank()) {
                log.warn("@# 안전 주소 생성 실패 address => {}", address);
                return null;
            }

            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                    .queryParam("query", safeAddress)
                    .queryParam("analyze_type", "similar")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            log.info("@# kakao address search uri => {}", uri);

            String body = requestKakao(uri);

            return parseCoordinateResponse(safeAddress, body, "ADDRESS");

        } catch (Exception e) {
            log.error("@# Kakao 주소 검색 API 처리 오류 address => {}", address, e);
            return null;
        }
    }

    /*
     * Kakao 키워드 장소 검색 API
     */
    private EvKakaoAddressDTO searchByKeywordApi(String keyword, String searchStepName) {
        log.info("@# searchByKeywordApi()");
        log.info("@# searchStepName => {}", searchStepName);
        log.info("@# keyword original => {}", keyword);

        if (keyword == null || keyword.isBlank()) {
            log.warn("@# keyword가 없어 키워드 검색 생략 searchStepName => {}", searchStepName);
            return null;
        }

        try {
            String safeKeyword = makeSafeKeyword(keyword);

            if (safeKeyword == null || safeKeyword.isBlank()) {
                log.warn("@# 안전 keyword 생성 실패 keyword => {}", keyword);
                return null;
            }

            log.info("@# keyword safe => {}", safeKeyword);
            log.info("@# keyword safe byte length => {}", getByteLength(safeKeyword));

            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", safeKeyword)
                    .queryParam("size", 5)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUri();

            log.info("@# kakao keyword search uri => {}", uri);

            String body = requestKakao(uri);

            return parseCoordinateResponse(safeKeyword, body, "KEYWORD");

        } catch (Exception e) {
            log.error("@# Kakao 키워드 검색 API 처리 오류 keyword => {}", keyword, e);
            return null;
        }
    }

    /*
     * Kakao API 공통 요청
     *
     * String URL이 아니라 URI로 요청해야
     * 한글 query가 이중 인코딩되지 않는다.
     */
    private String requestKakao(URI uri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("@# kakao response status => {}", response.getStatusCode());

            String body = response.getBody();

            log.info("@# kakao response body => {}", body);

            return body;

        } catch (HttpClientErrorException e) {
            log.warn("@# Kakao API 요청 실패 status => {}", e.getStatusCode());
            log.warn("@# Kakao API 요청 실패 body => {}", e.getResponseBodyAsString());
            return null;

        } catch (Exception e) {
            log.error("@# Kakao API 요청 중 알 수 없는 오류 uri => {}", uri, e);
            return null;
        }
    }

    /*
     * Kakao 주소/키워드 검색 응답 파싱
     *
     * documents[0].x = 경도
     * documents[0].y = 위도
     */
    private EvKakaoAddressDTO parseCoordinateResponse(
            String requestKeyword,
            String body,
            String searchType) throws Exception {

        log.info("@# parseCoordinateResponse()");
        log.info("@# requestKeyword => {}", requestKeyword);
        log.info("@# searchType => {}", searchType);

        if (body == null || body.isBlank()) {
            log.warn("@# Kakao 응답 body 없음 requestKeyword => {}", requestKeyword);
            return null;
        }

        JsonNode root = objectMapper.readTree(body);
        JsonNode documents = root.path("documents");

        if (!documents.isArray() || documents.size() == 0) {
            log.warn("@# Kakao 검색 결과 없음 requestKeyword => {}, searchType => {}",
                    requestKeyword,
                    searchType);
            return null;
        }

        JsonNode first = documents.get(0);

        String longitudeText = first.path("x").asText(null);
        String latitudeText = first.path("y").asText(null);

        if (longitudeText == null || longitudeText.isBlank()
                || latitudeText == null || latitudeText.isBlank()) {
            log.warn("@# Kakao 좌표값 없음 requestKeyword => {}", requestKeyword);
            return null;
        }

        String resultAddress = first.path("road_address_name").asText(null);

        if (resultAddress == null || resultAddress.isBlank()) {
            resultAddress = first.path("address_name").asText(null);
        }

        if (resultAddress == null || resultAddress.isBlank()) {
            resultAddress = first.path("place_name").asText(requestKeyword);
        }

        EvKakaoAddressDTO dto = new EvKakaoAddressDTO();
        dto.setAddress(resultAddress);
        dto.setLongitude(new BigDecimal(longitudeText));
        dto.setLatitude(new BigDecimal(latitudeText));

        log.info("@# Kakao 좌표 검색 성공 dto => {}", dto);

        return dto;
    }

    /*
     * keyword 후보 추가
     */
    private void addKeyword(Set<String> keywordSet, String keyword) {
        String normalized = normalizeKeyword(keyword);

        if (normalized == null || normalized.isBlank()) {
            return;
        }

        String safeKeyword = makeSafeKeyword(normalized);

        if (safeKeyword == null || safeKeyword.isBlank()) {
            return;
        }

        keywordSet.add(safeKeyword);
    }

    /*
     * 장소명 + 주소 검색어 조합
     */
    private String combineKeyword(String placeName, String address) {
        StringBuilder sb = new StringBuilder();

        if (placeName != null && !placeName.isBlank()) {
            sb.append(placeName.trim());
        }

        if (address != null && !address.isBlank()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }

            sb.append(address.trim());
        }

        String result = normalizeKeyword(sb.toString());

        log.info("@# combinedKeyword => {}", result);

        return result;
    }

    /*
     * 지역명 추출
     *
     * 예:
     * 서울특별시 종로구 인사동5길 29
     * → 서울특별시 종로구
     */
    private String makeRegionKeyword(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String[] parts = address.trim().split("\\s+");

        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }

        return address.trim();
    }

    /*
     * 주소를 짧게 줄인다.
     *
     * 예:
     * 제주특별자치도 서귀포시 남원읍 중산간동로 6862
     * → 제주특별자치도 서귀포시 남원읍 중산간동로
     */
    private String makeShortAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        String[] parts = address.trim().split("\\s+");

        if (parts.length <= 4) {
            return address.trim();
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                sb.append(" ");
            }

            sb.append(parts[i]);
        }

        String result = sb.toString();

        log.info("@# shortAddress => {}", result);

        return result;
    }

    /*
     * Kakao query 길이 제한 대응
     */
    private String makeSafeKeyword(String keyword) {
        String result = normalizeKeyword(keyword);

        if (result == null || result.isBlank()) {
            return null;
        }

        int maxBytes = 90;

        while (getByteLength(result) > maxBytes) {
            int lastSpaceIndex = result.lastIndexOf(" ");

            if (lastSpaceIndex > 0) {
                result = result.substring(0, lastSpaceIndex).trim();
            } else {
                result = result.substring(0, result.length() - 1).trim();
            }

            if (result.isBlank()) {
                return null;
            }
        }

        return result;
    }

    /*
     * 문자열 정리
     */
    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String result = keyword
                .replaceAll("\\s+", " ")
                .trim();

        if (result.isBlank()) {
            return null;
        }

        return result;
    }

    /*
     * 공백 제거
     */
    private String removeSpaces(String value) {
        if (value == null) {
            return null;
        }

        return value.replaceAll("\\s+", "").trim();
    }

    /*
     * UTF-8 byte 길이 확인
     */
    private int getByteLength(String value) {
        if (value == null) {
            return 0;
        }

        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}