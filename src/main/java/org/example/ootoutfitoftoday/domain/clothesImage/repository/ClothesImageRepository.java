package org.example.ootoutfitoftoday.domain.clothesImage.repository;

import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClothesImageRepository extends JpaRepository<ClothesImage, Long> {

    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Query("""
            UPDATE ClothesImage ci
            SET ci.isDeleted = true,
                ci.deletedAt = CURRENT_TIMESTAMP
            WHERE ci.clothes.id = :clothesId
              AND ci.isDeleted = false
            """)
    int softDeleteAllByClothesId(@Param("clothesId") Long clothesId);

    @Query("""
            SELECT EXISTS (
                    SELECT ci.id
                    FROM ClothesImage ci
                    WHERE ci.image.id In :imageIds
                      AND ci.isDeleted = false
                      AND ci.clothes.id <> :clothesId
                  )
            """)
    boolean existsLinkedImages(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            WHERE ci.clothes.id = :clothesId
              AND ci.image.id IN :imageIds
              AND ci.isDeleted = false
            """)
    List<ClothesImage> findByClothesIdAndImageIdsAndIsDeletedFalse(@Param("clothesId") Long clothesId, @Param("imageIds") List<Long> imageIds);

    @Query("""
            SELECT ci
            FROM ClothesImage ci
            JOIN FETCH ci.image
            WHERE ci.clothes.id = :clothesId
              AND ci.isDeleted = false
            ORDER BY ci.id asc, ci.createdAt asc
            """)
    List<ClothesImage> findByClothesIdAndIsDeletedFalse(@Param("clothesId") Long clothesId);

    // 메인 이미지만 반환하도록 구현
    @Query("""
            SELECT ci
            FROM ClothesImage ci
            JOIN FETCH ci.image
            WHERE ci.clothes.id IN :clothesIds
              AND ci.isMain = true
              AND ci.isDeleted = false
            """)
    List<ClothesImage> findMainImagesByClothesIdsAndIsDeletedFalse(@Param("clothesIds") List<Long> clothesIds);
}