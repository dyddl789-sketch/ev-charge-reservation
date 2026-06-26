package com.ev.common;

import lombok.Getter;

@Getter
public class EvBusinessException extends RuntimeException {

    private final EvErrorCode errorCode;

    public EvBusinessException(EvErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public EvBusinessException(EvErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
