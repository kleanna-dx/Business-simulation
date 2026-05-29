package com.company.precost.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 예외.
 * Service 레이어에서 발생시키며 GlobalExceptionHandler 가 ErrorCode 의 HttpStatus 로 응답한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
