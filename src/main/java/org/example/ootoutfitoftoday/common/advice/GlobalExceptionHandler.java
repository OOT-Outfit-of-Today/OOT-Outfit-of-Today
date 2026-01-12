package org.example.ootoutfitoftoday.common.advice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.exception.CommonErrorCode;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.example.ootoutfitoftoday.common.exception.GlobalException;
import org.example.ootoutfitoftoday.common.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [최종 안전망] 진짜 예상하지 못한 서버 오류
    // 담당: DB 연결 실패, NullPointerException, 개발자가 놓친 모든 버그
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
    public ResponseEntity<Response<?>> handleGlobalException(GlobalException ex) {
        log.warn("비즈니스 오류 발생: {}", ex.getMessage());

        // additionalData가 있으면 포함, 없으면 null
        Object data = ex.getAdditionalData();

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(Response.error(data, ex.getErrorCode()));
    }

    // [JSON 파싱 실패] Controller 진입 전 JSON → DTO 변환 실패
    // 담당: JSON 문법 오류, 타입 불일치, 잘못된 형식
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Map<String, String>>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Map<String, String> errorMessage = new HashMap<>();

        // 원인 분석해서 더 구체적인 메시지 제공
        Throwable cause = ex.getCause();
        if (cause instanceof JsonParseException jpe) {
            // JSON 자체가 잘못된 경우
            String location = jpe.getLocation() != null
                    ? String.format("라인 %d, 컬럼 %d",
                    jpe.getLocation().getLineNr(),
                    jpe.getLocation().getColumnNr())
                    : "알 수 없는 위치";
            log.warn("JSON 파싱 실패 - JSON 문법 오류: 위치 {}, 메시지: {}",
                    location, jpe.getOriginalMessage());

            String detailMessage = "JSON 형식이 올바르지 않습니다.";
            errorMessage.put("json", detailMessage);

        } else if (cause instanceof InvalidFormatException ife) {
            // 타입 변환 실패
            // 중첩된 객체(nested DTO)의 필드 경로를 모두 조합하되 점(.)으로 구분하여 표현
            String fieldName = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining("."));

            // 필터링 후 빈 문자열이면 기본값 사용
            if (fieldName.isEmpty()) {
                fieldName = "알 수 없는 필드";
            }

            log.warn("JSON 파싱 실패 - 필드: {}, 입력값: {}, 대상 타입: {}",
                    fieldName,
                    ife.getValue(),
                    ife.getTargetType().getSimpleName());

            String detailMessage = String.format("%s 필드의 값 형식이 올바르지 않습니다.", fieldName);
            errorMessage.put(fieldName, detailMessage);

        } else {
            // 기타 파싱 오류
            log.warn("JSON 파싱 실패 - 기타 오류: {}", ex.getMessage());

            String detailMessage = "요청 데이터 형식이 올바르지 않습니다.";
            errorMessage.put("body", detailMessage);
        }

        return ResponseEntity
                .status(CommonErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.INVALID_REQUEST_BODY));
    }

    // [Bean Validation 실패] @Valid 검증 실패
    // 담당: @NotNull 등 어노테이션 검증 실패, 필드가 없거나, 범위를 벗어나거나, 형식이 맞지 않을 때
    // 모든 필드 에러를 Map으로 반환하여 한 번에 모든 문제를 파악 가능
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errorMessage = new HashMap<>();
        // FieldError와 ObjectError(글로벌 에러) 모두 처리
        // FieldError: 개별 필드 검증 실패(예: @NotNull, @Size)
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessage.put(error.getField(), error.getDefaultMessage());
            log.warn("Validation 실패(필드) - 필드: {}, 입력값: {}, 메시지: {}",
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage());
        });

        // ObjectError: 클래스 레벨 검증 실패(예: 두 필드 비교, 커스텀 검증)
        // 특정 필드에 속하지 않는 글로벌 에러 처리
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            errorMessage.put(error.getObjectName(), error.getDefaultMessage());
            log.warn("Validation 실패(글로벌) - 객체: {}, 메시지: {}",
                    error.getObjectName(),
                    error.getDefaultMessage());
        });

        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.VALIDATION_ERROR));
    }

    // [경로 변수 타입 오류] URL 경로의 파라미터 타입이 맞지 않을 때
    // 담당: PathVariable의 타입 변환 실패
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Map<String, String>>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("경로 변수 타입 오류 - 파라미터: {}, 입력값: {}, 필요한 타입: {}",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "알 수 없음");

        Map<String, String> errorMessage = new HashMap<>();
        String detailMessage = String.format("%s 파라미터의 값 형식이 올바르지 않습니다.", ex.getName());
        errorMessage.put(ex.getName(), detailMessage);

        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.INVALID_INPUT_VALUE));
    }

    // [필수 요청 파라미터 누락] @RequestParam(required=true) 파라미터가 없을 때
    // 담당: 쿼리 파라미터 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response<Map<String, String>>> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("필수 파라미터 누락 - 파라미터: {}, 타입: {}",
                ex.getParameterName(),
                ex.getParameterType());

        Map<String, String> errorMessage = new HashMap<>();
        String detailMessage = String.format("%s 파라미터는 필수입니다.", ex.getParameterName());
        errorMessage.put(ex.getParameterName(), detailMessage);

        return ResponseEntity
                .status(CommonErrorCode.MISSING_REQUEST_PARAMETER.getHttpStatus())
                .body(Response.error(errorMessage, CommonErrorCode.MISSING_REQUEST_PARAMETER));
    }

    // [HTTP 메서드 불일치] 지원하지 않는 HTTP 메서드로 요청할 때
    // 담당: POST인데 GET으로 요청, GET인데 POST로 요청 등
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("지원하지 않는 HTTP 메서드 - 요청 메서드: {}, 지원 메서드: {}",
                ex.getMethod(),
                ex.getSupportedMethods() != null ? String.join(", ", ex.getSupportedMethods()) : "없음");

        String detailMessage = String.format("%s 메서드는 지원하지 않습니다.", ex.getMethod());

        return ResponseEntity
                .status(CommonErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(Response.error(detailMessage, CommonErrorCode.METHOD_NOT_ALLOWED));
    }

    // 내부 헬퍼 메서드: ErrorCode를 Response로 변환
    private ResponseEntity<Response<Void>> handleExceptionInternal(ErrorCode errorCode) {

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(Response.error(null, errorCode));
    }
}