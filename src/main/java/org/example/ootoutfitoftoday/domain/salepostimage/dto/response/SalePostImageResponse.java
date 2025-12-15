package org.example.ootoutfitoftoday.domain.salepostimage.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;

@Getter
@Builder
public class SalePostImageResponse {

    // SalePostImage의 ID(중간 테이블 ID)
    private final Long salePostImageId;
    // 실제 Image의 ID
    private final Long imageId;
    private final String imageUrl;
    private final String fileName;
    private final Integer displayOrder;
    private final Boolean isMain;

    // 설명: SalePostImage가 Image 엔티티를 참조하므로
    //      image.getImage()로 접근
    public static SalePostImageResponse from(SalePostImage salePostImage) {
        return SalePostImageResponse.builder()
                .salePostImageId(salePostImage.getId())
                .imageId(salePostImage.getImage().getId())    // Image ID 추가
                .imageUrl(salePostImage.getImageUrl())        // 편의 메서드 사용
                .fileName(salePostImage.getFileName())        // 편의 메서드 사용
                .displayOrder(salePostImage.getDisplayOrder())
                .isMain(salePostImage.getIsMain())
                .build();
    }
}