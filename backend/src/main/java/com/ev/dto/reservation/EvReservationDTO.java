package com.ev.dto.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/*
 * 예약 DTO
 *
 * 역할:
 * - 예약 등록 요청 데이터를 Controller → Service → Mapper로 전달
 * - 예약 완료 화면에서 보여줄 예약 정보를 JSP로 전달
 *
 * 시간 설계:
 * - reservationDate : 예약 기준 날짜
 * - startTime       : 실제 예약 시작 날짜 + 시간
 * - endTime         : 실제 예약 종료 날짜 + 시간
 *
 * startTime, endTime을 LocalDateTime으로 사용하는 이유:
 * - DB 컬럼 start_time, end_time이 timestamp 타입이기 때문
 * - 23:50 시작 → 다음날 00:30 종료 같은 예약도 처리하기 위해서
 */
@Data
public class EvReservationDTO {

    /*
     * 예약 기본 정보
     */
    private Long reservationId;
    private Long memberId;
    private Long vehicleId;
    private Long chargerId;

    /*
     * 예약 기준 날짜
     *
     * DB 컬럼:
     * reservation_date date
     *
     * 화면에서는 날짜 input에서 넘어오지만,
     * Service에서 startTime의 날짜를 기준으로 다시 세팅한다.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate reservationDate;

    /*
     * 예약 시작 시간
     *
     * DB 컬럼:
     * start_time timestamp
     *
     * JSP hidden input에서 넘어오는 값 예:
     * 2026-05-28T23:50
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /*
     * 예약 종료 시간
     *
     * DB 컬럼:
     * end_time timestamp
     *
     * 다음날 종료도 가능하다.
     *
     * 예:
     * startTime = 2026-05-28T23:50
     * endTime   = 2026-05-29T00:30
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /*
     * 배터리 잔량 정보
     */
    private Integer currentSoc;
    private Integer targetSoc;

    /*
     * 예약 계산 결과
     *
     * requiredKwh:
     * - 필요한 충전량
     *
     * estimatedMinutes:
     * - 예상 충전 시간
     *
     * estimatedCost:
     * - 예상 충전 비용
     */
    private Double requiredKwh;
    private Integer estimatedMinutes;
    private Double estimatedCost;

    /*
     * 예약 상태
     *
     * 예:
     * 예약완료
     * 인증완료
     * 충전중
     * 완료
     * 취소
     * 노쇼
     */
    private String status;

    /*
     * 이전 DB 인증코드 방식에서 사용하던 필드이다.
     *
     * 현재 예약 인증은 Redis의
     * ev:charger:{chargerId}:auth-code 값을 사용하므로
     * 새 예약 등록/인증 흐름에서는 이 필드를 사용하지 않는다.
     *
     * reservation.auth_code 컬럼이 남아 있어도 nullable이면 문제 없다.
     */
    private String authCode;

    /*
     * 인증 / 노쇼 / 취소 / 등록 / 수정 시간
     */
    private LocalDateTime verifiedAt;
    private LocalDateTime noShowAt;
    private LocalDateTime canceledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     * 예약 완료 화면 표시용 필드
     *
     * reservation 테이블에 직접 있는 값은 아니고,
     * Mapper에서 JOIN으로 조회해서 담는 값이다.
     */
    private String stationName;
    private String stationAddress;
    private String chargerName;
    private String connectorType;
    private String vehicleNickname;
    private String modelName;

    /*
     * JSP 화면 표시용 시작 시간
     *
     * LocalDateTime을 그대로 출력하면
     * 2026-05-28T23:50 처럼 T가 포함되어 보일 수 있다.
     *
     * 그래서 화면에서는
     * 2026-05-28 23:50 형태로 보여주기 위한 getter이다.
     */
    public String getStartTimeText() {
        if (startTime == null) {
            return "";
        }

        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /*
     * JSP 화면 표시용 종료 시간
     */
    public String getEndTimeText() {
        if (endTime == null) {
            return "";
        }

        return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    /*
     * 예약 인증 가능 여부
     *
     * true:
     * - 현재 시간이 예약 시작 10분 전 ~ 예약 종료 시간 사이
     *
     * false:
     * - 아직 인증 가능 시간이 아님
     */
    private Boolean verifyAvailable;
}