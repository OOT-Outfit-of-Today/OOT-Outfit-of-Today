package org.example.ootoutfitoftoday.domain.salepostimage.service.command;

import org.example.ootoutfitoftoday.domain.salepostimage.dto.response.SalePostImageResponse;

import java.util.List;

public interface SalePostImageCommandService {

    // 이미지 추가
    // 설명: 기존 이미지는 유지하고 새 이미지 추가
    // 사용: POST /v1/sale-posts/{salePostId}/images
    List<SalePostImageResponse> addImages(
            Long salePostId,
            Long userId,
            List<Long> imageIds
    );

    // 이미지 전체 교체
    // 설명: 기존 이미지 모두 soft delete 후 새 이미지로 교체
    // 사용: PUT /v1/sale-posts/{salePostId}/images
    List<SalePostImageResponse> replaceImages(
            Long salePostId,
            Long userId,
            List<Long> imageIds
    );

    // 이미지 개별 삭제
    // 설명: 특정 이미지 하나만 soft delete
    // 사용: DELETE /v1/sale-posts/{salePostId}/images/{imageId}
    void deleteImage(
            Long salePostId,
            Long userId,
            Long salePostImageId
    );

    // 메인 이미지 변경
    // 설명: 특정 이미지를 메인 이미지로 변경
    // 사용: PATCH /v1/sale-posts/{salePostId}/images/{imageId}/main
    void updateMainImage(
            Long salePostId,
            Long userId,
            Long salePostImageId
    );

    // 이미지 순서 변경
    // 설명: displayOrder 변경
    // 사용: PATCH /v1/sale-posts/{salePostId}/images/order
    void reorderImages(
            Long salePostId,
            Long userId,
            List<Long> orderedImageIds
    );
}