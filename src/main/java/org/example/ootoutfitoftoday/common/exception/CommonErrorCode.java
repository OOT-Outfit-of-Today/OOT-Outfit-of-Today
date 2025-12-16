package org.example.ootoutfitoftoday.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    UNEXPECTED_SERVER_ERROR("UNEXPECTED_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "예상하지 못한 서버 오류가 발생했습니다."),
    VALIDATION_ERROR("VALIDATION_ERROR", HttpStatus.BAD_REQUEST, "Validation 오류가 발생했습니다."),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST, "요청 데이터 형식이 올바르지 않습니다."),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    MISSING_REQUEST_PARAMETER("MISSING_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
