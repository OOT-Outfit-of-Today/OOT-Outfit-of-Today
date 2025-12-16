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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [최종 안정망] 진짜 예상하지 못한 서버 오류
    // 담당: DB 연결 실패, NullPointerException 등 개발자가 놓친 버그들
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleException(Exception ex) {
        log.error("알 수 없는 서버 오류 발생", ex);

        return ResponseEntity
                .status(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getHttpStatus())
                .body(Response.error(null, CommonErrorCode.UNEXPECTED_SERVER_ERROR));
    }

    // [비즈니스 예외] 비즈니스 로직(Service)에서 의도적으로 던진 예외
    // 담당: 존재하지 않는 리소스, 권한 없음, 중복 데이터, 비즈니스 규칙 위반
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Response<Void>> handleGlobalException(GlobalException ex) {
        log.warn("비즈니스 오류 발생: {}", ex.getMessage());

        return handleExceptionInternal(ex.getErrorCode());
    }

    // [JSON 파싱 실패] Controller 진입 전 JSON → DTO 변환 실패
    // 담당: JSON 문법 오류, 타입 불일치, 잘못된 형식
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 실패: {}", e.getMessage());

        String detailMessage = "요청 데이터 형식이 올바르지 않습니다";

        // 원인 분석해서 더 구체적인 메시지 제공
        Throwable cause = e.getCause();
        if (cause instanceof JsonParseException) {
            // JSON 자체가 잘못된 경우
            detailMessage = "JSON 형식이 올바르지 않습니다";
        } else if (cause instanceof InvalidFormatException) {
            // 타입 변환 실패
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

    // [Bean Validation 실패] @Valid 검증 실패
    // 담당: @NotNull 등 어노테이션 검증 실패, 필드가 없거나, 범위를 벗어나거나, 형식이 맞지 않을 때
    // 모든 필드 에러를 Map으로 반환하여 한 번에 모든 문제를 파악 가능
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errorMessage = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
                    errorMessage.put(error.getField(), error.getDefaultMessage());
                    log.warn("Validation 오류 발생 - 필드: {}, 입력값: {}, 메시지: {}",
                            error.getField(),
                            error.getRejectedValue(),
                            error.getDefaultMessage());
                }
        );

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.VALIDATION_ERROR));
    }

    // [경로 변수 타입 오류] URL 경로의 파라미터 타입이 맞지 않을 때
    // 담당: PathVariable의 타입 변환 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        log.warn("경로 변수 타입 오류 - 파라미터: {}, 입력값: {}, 필요한 타입: {}",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음");

        String detailMessage = String.format(
                "%s 파라미터의 값이 올바르지 않습니다. 올바른 형식: %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음");

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(Response.error(detailMessage, CommonErrorCode.INVALID_INPUT_VALUE));
    }

    // 내부 헬퍼 메서드: ErrorCode를 Response로 변환
    private ResponseEntity<Response<Void>> handleExceptionInternal(ErrorCode errorCode) {

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(Response.error(null, errorCode));
    }
}