package org.example.ootoutfitoftoday.domain.salepostimage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SalePostImageErrorCode implements ErrorCode {

    // 에러코드 명확화: SALE_POST_IMAGE 접두사 추가
    // 설명: 범용 Image와 중간테이블 SalePostImage 구분
    SALE_POST_IMAGE_NOT_FOUND("SALE_POST_IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "판매글 이미지를 찾을 수 없습니다"),
    TOO_MANY_SALE_POST_IMAGES("TOO_MANY_SALE_POST_IMAGES", HttpStatus.BAD_REQUEST, "판매글 이미지는 최대 10개까지 가능합니다"),
    EMPTY_SALE_POST_IMAGES("EMPTY_SALE_POST_IMAGES", HttpStatus.BAD_REQUEST, "판매글 이미지는 최소 1개 이상 필요합니다"),
    SALE_POST_IMAGE_NOT_BELONG("SALE_POST_IMAGE_NOT_BELONG", HttpStatus.BAD_REQUEST, "해당 판매글의 이미지가 아닙니다"),
    ALREADY_MAIN_SALE_POST_IMAGE("ALREADY_MAIN_SALE_POST_IMAGE", HttpStatus.BAD_REQUEST, "이미 메인 이미지입니다"),
    CANNOT_DELETE_LAST_SALE_POST_IMAGE("CANNOT_DELETE_LAST_SALE_POST_IMAGE", HttpStatus.BAD_REQUEST, "최소 1개의 이미지는 남아있어야 합니다"),
    DUPLICATE_SALE_POST_IMAGE("DUPLICATE_SALE_POST_IMAGE", HttpStatus.BAD_REQUEST, "판매글에 동일한 이미지를 중복으로 등록 할 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}