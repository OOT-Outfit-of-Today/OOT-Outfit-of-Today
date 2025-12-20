package org.example.ootoutfitoftoday.domain.clothesImage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesImageSuccessCode implements SuccessCode {

    CLOTHES_IMAGE_LINK("CLOTHES_IMAGE_LINK", HttpStatus.OK, "옷에 이미지를 등록하였습니다."),
    CLOTHES_IMAGE_REMOVE("CLOTHES_IMAGE_REMOVE", HttpStatus.OK, "옷에 등록된 이미지를 제거하였습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
