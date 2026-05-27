package com.ev.service.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.ev.dto.route.EvRoutePointDTO;
import com.ev.dto.route.EvRouteSimulationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EvRouteServiceImpl implements EvRouteService {

    @Value("${kakao.rest-api.key}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public EvRouteSimulationDTO getRouteSimulation(
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng) {

        log.info("@# EvRouteServiceImpl.getRouteSimulation()");
        log.info("@# startLat => {}", startLat);
        log.info("@# startLng => {}", startLng);
        log.info("@# endLat => {}", endLat);
        log.info("@# endLng => {}", endLng);

        try {
            /*
             * Kakao Mobility 자동차 길찾기 API
             *
             * 중요:
             * origin / destination 좌표 순서는
             * 경도,위도 이다.
             */
            String origin = startLng + "," + startLat;
            String destination = endLng + "," + endLat;

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://apis-navi.kakaomobility.com/v1/directions")
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("priority", "RECOMMEND")
                    .queryParam("car_fuel", "GASOLINE")
                    .queryParam("car_hipass", "false")
                    .queryParam("alternatives", "false")
                    .queryParam("road_details", "false")
                    .queryParam("summary", "false")
                    .build()
                    .toUriString();

            log.info("@# kakao route url => {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String body = response.getBody();

            log.info("@# kakao route response status => {}", response.getStatusCode());

            return parseRouteResponse(body);

        } catch (Exception e) {
            log.error("@# Kakao Mobility route api error", e);

            EvRouteSimulationDTO failDTO = new EvRouteSimulationDTO();
            failDTO.setDistanceMeter(0);
            failDTO.setDurationSecond(0);
            failDTO.setDistanceText("0km");
            failDTO.setDurationText("0분");
            failDTO.setPath(new ArrayList<>());

            return failDTO;
        }
    }

    /**
     * Kakao Mobility 응답 JSON을
     * 프론트에서 쓰기 쉬운 DTO로 변환한다.
     */
    private EvRouteSimulationDTO parseRouteResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        JsonNode route = root.path("routes").get(0);

        int resultCode = route.path("result_code").asInt();

        if (resultCode != 0) {
            log.info("@# route result_code => {}", resultCode);
            log.info("@# route result_msg => {}", route.path("result_msg").asText());

            EvRouteSimulationDTO emptyDTO = new EvRouteSimulationDTO();
            emptyDTO.setDistanceMeter(0);
            emptyDTO.setDurationSecond(0);
            emptyDTO.setDistanceText("0km");
            emptyDTO.setDurationText("0분");
            emptyDTO.setPath(new ArrayList<>());

            return emptyDTO;
        }

        JsonNode summary = route.path("summary");

        int distance = summary.path("distance").asInt();
        int duration = summary.path("duration").asInt();

        List<EvRoutePointDTO> path = new ArrayList<>();

        /*
         * sections[].roads[].vertexes에 경로 좌표가 들어있다.
         *
         * vertexes 구조:
         * [경도, 위도, 경도, 위도, ...]
         */
        JsonNode sections = route.path("sections");

        for (JsonNode section : sections) {
            JsonNode roads = section.path("roads");

            for (JsonNode road : roads) {
                JsonNode vertexes = road.path("vertexes");

                for (int i = 0; i < vertexes.size(); i += 2) {
                    double lng = vertexes.get(i).asDouble();
                    double lat = vertexes.get(i + 1).asDouble();

                    path.add(new EvRoutePointDTO(lat, lng));
                }
            }
        }

        EvRouteSimulationDTO dto = new EvRouteSimulationDTO();
        dto.setDistanceMeter(distance);
        dto.setDurationSecond(duration);
        dto.setDistanceText(formatDistance(distance));
        dto.setDurationText(formatDuration(duration));
        dto.setPath(path);

        log.info("@# route distance => {}", dto.getDistanceText());
        log.info("@# route duration => {}", dto.getDurationText());
        log.info("@# route path size => {}", path.size());

        return dto;
    }

    private String formatDistance(int meter) {
        if (meter >= 1000) {
            double km = meter / 1000.0;
            return String.format("%.1fkm", km);
        }

        return meter + "m";
    }

    private String formatDuration(int second) {
        int minute = second / 60;

        if (minute < 60) {
            return minute + "분";
        }

        int hour = minute / 60;
        int remainMinute = minute % 60;

        return hour + "시간 " + remainMinute + "분";
    }
}