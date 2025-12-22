package org.example.ootoutfitoftoday.domain.clothes.repository;

import com.ootcommon.category.response.CategoryStat;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClothesRepository extends JpaRepository<Clothes, Long>, ClothesCustomRepository {

    @Query("""
            SELECT c
            FROM Clothes c
            WHERE c.user.id = :userId
              AND c.id = :clothesId
              AND c.isDeleted = false
            """)
    Optional<Clothes> findClothesByIdAndUserIdAndIsDeletedFalse(@Param("userId") Long userId, @Param("clothesId") Long clothesId);

    @Modifying
    @Query("""
            UPDATE Clothes c
            SET c.category = NULL
            WHERE c.category.id IN :categoryIds
            """)
    void clearCategoryFromClothes(@Param("categoryIds") List<Long> categoryIds);

    int countAllClothesByIsDeletedFalse();

    int countAllClothesByUserIdAndIsDeletedFalse(Long userId);

    @Query("""
            SELECT new com.ootcommon.category.response.CategoryStat(
                c.category.name, count(c)
            )
            FROM Clothes c
            WHERE c.user.id = :userId
            GROUP BY c.category.id, c.category.name
            ORDER BY count(c) DESC, c.category.id
            """)
    List<CategoryStat> countUserTopCategoryStats(@Param("userId") Long userId);

    @Query("""
            SELECT c
            FROM Clothes c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
            """)
    List<Clothes> findAllByUserIdAndIsDeletedFalse(@Param("userId") Long userId);
}