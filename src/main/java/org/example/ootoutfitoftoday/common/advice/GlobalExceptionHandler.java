package org.example.ootoutfitoftoday.common.advice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.exception.CommonErrorCode;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.example.ootoutfitoftoday.common.exception.GlobalException;
import org.example.ootoutfitoftoday.common.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception ex) {
        log.error("알 수 없는 서버 오류 발생", ex);

        return ResponseEntity
                .status(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getHttpStatus())
                .body(Response.error(null, CommonErrorCode.UNEXPECTED_SERVER_ERROR));
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Response<Void>> handleGlobalException(GlobalException ex) {
        log.warn("비즈니스 오류 발생: {}", ex.getMessage());

        return handleExceptionInternal(ex.getErrorCode());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<String>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e
    ) {
        log.warn("JSON 파싱 실패: {}", e.getMessage());

        String detailMessage = "요청 데이터 형식이 올바르지 않습니다";

        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException) {
            detailMessage = "JSON 형식이 올바르지 않습니다";
        } else if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            String fieldName = ife.getPath().isEmpty()
                    ? "알 수 없는 필드"
                    : ife.getPath().get(0).getFieldName();
            detailMessage = String.format("%s 필드의 값 형식이 올바르지 않습니다", fieldName);
        }

        return ResponseEntity
                .status(CommonErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(Response.error(detailMessage, CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation 오류 발생: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.VALIDATION_ERROR));
    }

    private ResponseEntity<Response<Void>> handleExceptionInternal(ErrorCode errorCode) {

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(Response.error(null, errorCode));
    }
}