package com.ev.dto.map;

import lombok.Data;

/*
 * 회원이 저장한 출발 위치 DTO
 *
 * saved_location 테이블과 매핑된다.
 * 길찾기 시뮬레이션에서 출발지 좌표로 사용한다.
 *
 * 주의:
 * DB에는 location geography(Point, 4326) 컬럼이 있지만,
 * Java 화면 처리에서는 latitude / longitude만 사용하므로 DTO에는 location 필드를 두지 않는다.
 */
@Data
public class EvSavedLocationDTO {

    // 위치 고유 번호
    private Long locationId;

    // 로그인 회원 번호
    private Long memberId;

    // 위치 이름: 집, 회사, 학교, 직접 입력 이름 등
    private String locationName;

    // 위치 유형: HOME / WORK / SCHOOL / FAVORITE / CUSTOM
    private String locationType;

    // 사용자가 입력한 주소
    private String address;

    // 위도
    private Double latitude;

    // 경도
    private Double longitude;

    // 기본 출발 위치 여부
    private Boolean isDefault;
}
