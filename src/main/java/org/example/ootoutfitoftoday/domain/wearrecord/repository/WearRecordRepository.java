package org.example.ootoutfitoftoday.domain.wearrecord.repository;

import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface WearRecordRepository extends JpaRepository<WearRecord, Long>, WearCustomRepository {

    @Query(
            value = """
                    SELECT DISTINCT wr
                    FROM WearRecord wr
                    JOIN FETCH wr.user u
                    JOIN FETCH wr.clothes c
                    LEFT JOIN FETCH c.images ci
                    LEFT JOIN FETCH ci.image i
                    WHERE u.id = :userId
                    """,
            countQuery = """
                     SELECT count(wr)
                     FROM WearRecord wr
                     WHERE wr.user.id = :userId
                    """)
    Page<WearRecord> findMyWearRecordsWithClothes(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 기준일에 이미 등록을 했다면 예외처리
    @Query("""
            SELECT CASE WHEN COUNT(wr) > 0 THEN TRUE ELSE FALSE END
            FROM WearRecord wr
            WHERE wr.user.id = :userId
              AND wr.clothes.id = :clothesId
              AND wr.clothes.isDeleted = false
              AND wr.wornAt >= :startOfDay
              AND wr.wornAt < :endOfDay
            """)
    boolean existsByUserIdAndClothesIdAndWornAtBetween(
            @Param("userId") Long userId,
            @Param("clothesId") Long clothsId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}