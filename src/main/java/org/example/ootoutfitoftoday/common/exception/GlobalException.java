package org.example.ootoutfitoftoday.common.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;
    private final SuccessCode successCode;
    private final Object additionalData;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.successCode = null;
        this.additionalData = null;
    }

    public GlobalException(ErrorCode errorCode, SuccessCode successCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.successCode = successCode;
        this.additionalData = null;
    }

    public GlobalException(ErrorCode errorCode, Object additionalData) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.successCode = null;
        this.additionalData = additionalData;
    }
}