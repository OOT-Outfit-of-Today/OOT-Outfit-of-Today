package org.example.ootoutfitoftoday.domain.salepostimage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalePostImageSuccessCode implements SuccessCode {

    SALE_POST_IMAGES_ADDED("SALE_POST_IMAGES_ADDED", HttpStatus.CREATED, "이미지가 추가되었습니다"),
    SALE_POST_IMAGES_REPLACED("SALE_POST_IMAGES_REPLACED", HttpStatus.OK, "이미지가 교체되었습니다"),
    SALE_POST_IMAGE_DELETED("SALE_POST_IMAGE_DELETED", HttpStatus.OK, "이미지가 삭제되었습니다"),
    MAIN_SALE_POST_IMAGE_UPDATED("MAIN_SALE_POST_IMAGE_UPDATED", HttpStatus.OK, "메인 이미지가 변경되었습니다");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}