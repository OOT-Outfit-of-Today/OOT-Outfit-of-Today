package org.example.ootoutfitoftoday.domain.salepostimage.service.query;

import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;

import java.util.List;

public interface SalePostImageQueryService {

    // 판매글의 이미지 조회(SalePostImage 메타데이터만)
    // 삭제, 순서 변경 등 Image 정보가 불필요한 경우
    List<SalePostImage> findBySalePostIdWithoutImage(Long salePostId);

    // 판매글의 이미지 조회(Image 엔티티 포함)
    // 상세 조회, 목록 조회 등 Image URL이 필요한 경우
    List<SalePostImage> findBySalePostIdWithImage(Long salePostId);
}
