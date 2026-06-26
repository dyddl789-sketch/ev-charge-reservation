package com.ev.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class EvGlobalExceptionHandler {

    @ExceptionHandler(EvBusinessException.class)
    public ResponseEntity<EvApiResponse<Void>> handleBusinessException(EvBusinessException e) {
        log.warn("@# 업무 예외 발생 code => {}, message => {}", e.getErrorCode().name(), e.getMessage());
        String message = e.getMessage() == null ? e.getErrorCode().getMessage() : e.getMessage();
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(EvApiResponse.fail(e.getErrorCode().name(), message));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ResponseEntity<EvApiResponse<Void>> handleValidationException(Exception e) {
        log.warn("@# 요청값 검증 예외 발생 message => {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(EvApiResponse.fail(EvErrorCode.INVALID_REQUEST));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<EvApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("@# 권한 예외 발생 message => {}", e.getMessage());
        return ResponseEntity
                .status(EvErrorCode.FORBIDDEN.getHttpStatus())
                .body(EvApiResponse.fail(EvErrorCode.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EvApiResponse<Void>> handleException(Exception e) {
        log.error("@# 서버 예외 발생", e);
        return ResponseEntity
                .internalServerError()
                .body(EvApiResponse.fail(EvErrorCode.INTERNAL_SERVER_ERROR));
    }
}
