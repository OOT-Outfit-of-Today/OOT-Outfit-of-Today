package org.example.ootoutfitoftoday.domain.salepostimage.repository;

import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalePostImageRepository extends JpaRepository<SalePostImage, Long> {

    /**
     * SalePostImage 조회
     * SalePostImage 엔티티는 조회함
     * 연관된 Image 엔티티는 fetch join 하지 않음 (lazy loading)
     * Image.url 등이 필요 없는 경우 사용 (메타데이터만 필요할 때)
     * 사용 예: soft delete, 순서 변경, 개수 확인 등
     * displayOrder 오름차순 정렬
     * @param salePostId 판매글 ID
     * @return SalePostImage 목록(Image는 lazy loading)
    */
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            WHERE spi.salePost.id = :salePostId
            AND spi.isDeleted = false
            ORDER BY spi.displayOrder ASC
            """)
    List<SalePostImage> findBySalePostIdAndIsDeletedFalseWithoutImage(@Param("salePostId") Long salePostId);

    /**
     * SalePostImage 조회 (Image fetch join)
     * - SalePostImage와 연관된 Image를 한 번에 조회
     * - fetch join으로 N+1 문제 방지
     * - Image.url 등이 필요한 경우 사용
     * - 사용 예: 상세 조회, 목록 조회, 응답 생성 등
     * - displayOrder 오름차순 정렬
     * @param salePostId 판매글 ID
     * @return SalePostImage 목록(Image 포함)
     */
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            JOIN FETCH spi.image
            WHERE spi.salePost.id = :salePostId
            AND spi.isDeleted = false
            ORDER BY spi.displayOrder ASC
            """)
    List<SalePostImage> findBySalePostIdAndIsDeletedFalseWithImage(@Param("salePostId") Long salePostId);

    // 추가: 특정 Image를 사용하는 SalePostImage 개수 조회
    // 이유: Image 삭제 시 참조 확인용
    //      배치 작업에서 미사용 Image 정리 시 활용
    @Query("""
            SELECT COUNT(spi)
            FROM SalePostImage spi
            WHERE spi.image.id = :imageId
            AND spi.isDeleted = false
            """)
    long countByImageId(@Param("imageId") Long imageId);

    // 추가: 메인 이미지 조회
    // 설명: 판매글의 썸네일로 사용할 메인 이미지 조회
    //      목록 조회 시 사용(Native Query 대체용)
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            JOIN FETCH spi.image
            WHERE spi.salePost.id = :salePostId
            AND spi.isMain = true
            AND spi.isDeleted = false
            """)
    Optional<SalePostImage> findMainImageBySalePostId(@Param("salePostId") Long salePostId);

    // 추가: 삭제된 것 포함 조회(복구용)
    // 이유: 관리자가 삭제된 이미지를 복구하거나 확인할 때 사용
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            WHERE spi.salePost.id = :salePostId
            ORDER BY spi.displayOrder ASC
            """)
    List<SalePostImage> findBySalePostIdIncludingDeleted(@Param("salePostId") Long salePostId);
}