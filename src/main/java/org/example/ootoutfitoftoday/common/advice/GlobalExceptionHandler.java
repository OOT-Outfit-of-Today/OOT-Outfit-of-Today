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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        Response<Void> errorResponse = Response.error(null, CommonErrorCode.UNEXPECTED_SERVER_ERROR);
        return ResponseEntity
                .status(CommonErrorCode.UNEXPECTED_SERVER_ERROR.getHttpStatus())
                .body(errorResponse);
    }

    // [비즈니스 예외] 비즈니스 로직(Service)에서 의도적으로 던진 예외
    // 담당: 존재하지 않는 리소스, 권한 없음, 중복 데이터, 비즈니스 규칙 위반
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Response<?>> handleGlobalException(GlobalException ex) {
        log.warn("비즈니스 오류 발생: {}", ex.getMessage());

        // additionalData가 있으면 포함, 없으면 null
        Object data = ex.getAdditionalData();

        Response<?> errorResponse = Response.error(data, ex.getErrorCode());
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
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

        Response<Map<String, String>> errorResponse = Response.error(errorMessage, CommonErrorCode.INVALID_REQUEST_BODY);
        return ResponseEntity
                .status(CommonErrorCode.INVALID_REQUEST_BODY.getHttpStatus())
                .body(errorResponse);
    }

    // [Bean Validation 실패] @Valid 검증 실패
    // 담당: @NotNull 등 어노테이션 검증 실패, 필드가 없거나, 범위를 벗어나거나, 형식이 맞지 않을 때
    // 수정: 동일 필드의 모든 검증 에러를 List로 누적하여 일관된 응답 보장
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, List<String>>>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // 수정: Map<String, String> → Map<String, List<String>>으로 변경
        // 이유: 동일 필드에 여러 검증 에러 발생 시 put() 덮어쓰기로 일부만 응답되는 문제 해결
        //       Bean Validation 순서가 비결정적이므로 모든 에러를 누적하여 일관된 응답 보장
        Map<String, List<String>> errorMessages = new HashMap<>();

        // FieldError와 ObjectError(글로벌 에러) 모두 처리
        // FieldError: 개별 필드 검증 실패(예: @NotNull, @Size)
        // 수정: computeIfAbsent로 동일 필드의 모든 에러를 List에 누적
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessages.computeIfAbsent(error.getField(), k -> new ArrayList<>())
                    .add(error.getDefaultMessage());
            log.warn("Validation 실패(필드) - 필드: {}, 입력값: {}, 메시지: {}",
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage());
        });

        // ObjectError: 클래스 레벨 검증 실패(예: 두 필드 비교, 커스텀 검증)
        // 특정 필드에 속하지 않는 글로벌 에러 처리
        // 수정: 글로벌 에러도 List에 누적
        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            errorMessages.computeIfAbsent(error.getObjectName(), k -> new ArrayList<>())
                    .add(error.getDefaultMessage());
            log.warn("Validation 실패(글로벌) - 객체: {}, 메시지: {}",
                    error.getObjectName(),
                    error.getDefaultMessage());
        });

        Response<Map<String, List<String>>> errorResponse = Response.error(errorMessages, CommonErrorCode.VALIDATION_ERROR);
        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(errorResponse);
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

        Response<Map<String, String>> errorResponse = Response.error(errorMessage, CommonErrorCode.INVALID_INPUT_VALUE);
        return ResponseEntity
                .status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(errorResponse);
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

        Response<Map<String, String>> errorResponse = Response.error(errorMessage, CommonErrorCode.MISSING_REQUEST_PARAMETER);
        return ResponseEntity
                .status(CommonErrorCode.MISSING_REQUEST_PARAMETER.getHttpStatus())
                .body(errorResponse);
    }

    // [HTTP 메서드 불일치] 지원하지 않는 HTTP 메서드로 요청할 때
    // 담당: POST인데 GET으로 요청, GET인데 POST로 요청 등
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("지원하지 않는 HTTP 메서드 - 요청 메서드: {}, 지원 메서드: {}",
                ex.getMethod(),
                ex.getSupportedMethods() != null ? String.join(", ", ex.getSupportedMethods()) : "없음");

        String detailMessage = String.format("%s 메서드는 지원하지 않습니다.", ex.getMethod());

        Response<String> errorResponse = Response.error(detailMessage, CommonErrorCode.METHOD_NOT_ALLOWED);
        return ResponseEntity
                .status(CommonErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(errorResponse);
    }

    // 내부 헬퍼 메서드: ErrorCode를 Response로 변환
    private ResponseEntity<Response<Void>> handleExceptionInternal(ErrorCode errorCode) {

        Response<Void> errorResponse = Response.error(null, errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
}