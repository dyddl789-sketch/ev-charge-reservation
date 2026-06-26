package com.ev.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> EvApiResponse<T> ok(T data) {
        return new EvApiResponse<>(true, "OK", "요청이 정상 처리되었습니다.", data);
    }

    public static <T> EvApiResponse<T> fail(EvErrorCode errorCode) {
        return new EvApiResponse<>(false, errorCode.name(), errorCode.getMessage(), null);
    }

    public static <T> EvApiResponse<T> fail(String code, String message) {
        return new EvApiResponse<>(false, code, message, null);
    }
}
