package com.ev.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum EvErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "충전소 정보를 찾을 수 없습니다."),
    CHARGER_NOT_FOUND(HttpStatus.NOT_FOUND, "충전기 정보를 찾을 수 없습니다."),
    CHARGER_NOT_AVAILABLE(HttpStatus.CONFLICT, "현재 예약 가능한 충전기가 아닙니다."),

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
    RESERVATION_TIME_PASSED(HttpStatus.BAD_REQUEST, "예약 가능한 시간이 지났습니다."),
    RESERVATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 예약된 시간입니다."),
    RESERVATION_HOLD_EXPIRED(HttpStatus.CONFLICT, "예약 선점 시간이 만료되었습니다."),
    RESERVATION_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "취소할 수 없는 예약 상태입니다."),
    RESERVATION_NOT_NOSHOW_TARGET(HttpStatus.BAD_REQUEST, "노쇼 처리 대상 예약이 아닙니다."),

    COMPLAINT_NOT_FOUND(HttpStatus.NOT_FOUND, "민원 정보를 찾을 수 없습니다."),
    COMPLAINT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 민원입니다."),
    COMPLAINT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "처리할 수 없는 민원 상태입니다."),

    FAULT_NOT_FOUND(HttpStatus.NOT_FOUND, "장애 정보를 찾을 수 없습니다."),
    FAULT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 장애입니다."),
    FAULT_ASSIGN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "장애 담당자 배정 권한이 없습니다."),

    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 문서를 찾을 수 없습니다."),
    APPROVAL_NOT_YOUR_TURN(HttpStatus.FORBIDDEN, "현재 결재 순서가 아닙니다."),
    APPROVAL_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "이미 처리된 결재 문서입니다."),

    STAT_SAMPLE_DATA_NOT_FOUND(HttpStatus.BAD_REQUEST, "통계 샘플을 생성할 충전소 또는 충전기 데이터가 부족합니다."),
    STAT_SAMPLE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "통계 샘플 데이터 생성에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    EvErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
