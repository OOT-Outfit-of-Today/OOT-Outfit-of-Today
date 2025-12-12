package org.example.ootoutfitoftoday.domain.salepostimage.repository;

import org.example.ootoutfitoftoday.domain.salepostimage.entity.SalePostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalePostImageRepository extends JpaRepository<SalePostImage, Long> {

    // 명시적으로 isDeleted = false 조건 추가
    // 이유: @Where/@SQLRestriction 없이 명시적으로 처리
    //      삭제되지 않은 이미지만 조회
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            WHERE spi.salePost.id = :salePostId
            AND spi.isDeleted = false
            ORDER BY spi.displayOrder ASC
            """)
    List<SalePostImage> findBySalePostId(@Param("salePostId") Long salePostId);

    // 추가: Image와 함께 조회(Fetch Join)
    // 이유: N+1 문제 방지
    //      SalePostImage 조회 시 Image도 함께 로딩
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            JOIN FETCH spi.image i
            WHERE spi.salePost.id = :salePostId
            AND spi.isDeleted = false
            ORDER BY spi.displayOrder ASC
            """)
    List<SalePostImage> findBySalePostIdWithImage(@Param("salePostId") Long salePostId);

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
    // 이유: 썸네일 표시용
    @Query("""
            SELECT spi
            FROM SalePostImage spi
            JOIN FETCH spi.image i
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
