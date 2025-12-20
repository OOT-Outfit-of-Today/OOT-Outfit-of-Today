package org.example.ootoutfitoftoday.domain.clothesImage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesImageErrorCode implements ErrorCode {

    IMAGE_EMPTY("IMAGE_EMPTY", HttpStatus.BAD_REQUEST, "등록할 이미지를 선택해주세요."),
    IMAGE_ALREADY_LINKED("IMAGE_ALREADY_LINKED", HttpStatus.CONFLICT, "이미 현재 옷에 연결된 이미지입니다."),
    IMAGE_ALREADY_LINKED_TO_OTHER_CLOTHES("IMAGE_ALREADY_LINKED_TO_OTHER_CLOTHES", HttpStatus.CONFLICT, "이미 다른 옷과 연결된 이미지입니다."),
    CLOTHES_IMAGE_NOT_FOUND("CLOTHES_IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 옷에 등록된 이미지가 존재하지 않습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}