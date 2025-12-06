package org.example.ootoutfitoftoday.domain.clothes.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesErrorCode implements ErrorCode {

    CLOTHES_NOT_FOUND("CLOTHES_NOT_FOUND", HttpStatus.NOT_FOUND, "옷을 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
