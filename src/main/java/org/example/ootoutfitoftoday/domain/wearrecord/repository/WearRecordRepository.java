package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WearRecordRepository extends JpaRepository<WearRecord, Long>, WearCustomRepository {

    @Query("""
            SELECT wr
            FROM WearRecord wr
            JOIN FETCH wr.clothes c
            WHERE wr.user.id = :userId
              AND wr.isDeleted = false
              AND c.isDeleted = false
            """)
    Page<WearRecord> findMyWearRecordsWithClothesAndIsDeletedFalse(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 기준일에 이미 등록을 했다면 예외처리
    @Query("""
            SELECT CASE WHEN COUNT(wr) > 0 THEN TRUE ELSE FALSE END
            FROM WearRecord wr
            WHERE wr.user.id = :userId
              AND wr.clothes.id = :clothesId
              AND wr.isDeleted = false
              AND wr.clothes.isDeleted = false
              AND wr.wornAt >= :startOfDay
              AND wr.wornAt < :endOfDay
            """)
    boolean existsByUserIdAndClothesIdAndWornAtBetweenAndIsDeletedFalse(
            @Param("userId") Long userId,
            @Param("clothesId") Long clothesId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * clearAutomatically = true <- 벌크 쿼리 실행 후 영속성 컨텍스트를 자동으로 비운다(clear)
     * flushAutomatically = true <- 벌크 쿼리 실행 전에 영속성 컨텍스트 변경 사항을 DB에 먼저 반영(flush)
     *
     * 1. flushAutomatically
     *  - 영속성 컨텍스트 변경 사항 → DB 반영
     * 2. 벌크 UPDATE 실행(DB 직접 수정)
     * 3. clearAutomatically
     *  - 영속성 컨텍스트 비움
     * 4. 이후 조회는 항상 DB 기준의 최신 상태
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE WearRecord wr
            SET wr.isDeleted = true,
                wr.deletedAt = CURRENT_TIMESTAMP
            WHERE wr.user.id = :userId
              AND wr.clothes.id = :clothesId
              AND wr.isDeleted = false
            """)
    int softDeleteByUserIdAndClothesIdAndIsDeletedFalse(
            @Param("userId") Long userId,
            @Param("clothesId") Long clothesId
    );
}