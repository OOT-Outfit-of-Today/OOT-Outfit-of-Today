package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    // 내 옷장 조회용(공개 비공개 모두 조회)
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
              AND c.user.isDeleted = false
            """)
    Page<Closet> findAllByUser_Id(@Param("userId") Long userId, Pageable pageable);

    // 해당 유저의 공개된 옷장
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
            """)
    Page<Closet> findAllByUser_IdAndIsPublicTrue(@Param("userId") Long userId, Pageable pageable);

    // 공개된 옷장 전체 조회
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
            """)
    Page<Closet> findAllByIsPublicTrue(Pageable pageable);
}