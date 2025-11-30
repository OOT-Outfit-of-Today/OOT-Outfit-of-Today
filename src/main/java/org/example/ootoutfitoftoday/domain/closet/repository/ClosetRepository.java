package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    // 내 옷장 조회용(공개 비공개 모두 조회)
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
            """)
    Page<Closet> findAllMyClosets(@Param("userId") Long userId, Pageable pageable);

    // 해당 유저의 공개된 옷장
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
            """)
    Page<Closet> findAllClosetsByUser_Id(@Param("userId") Long userId, Pageable pageable);

    // 공개된 옷장 전체 조회
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
            """)
    Page<Closet> findAllPublicClosets(Pageable pageable);

    // 내 옷장 단건 조회
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.user.id = :userId
              AND c.isDeleted = false
              AND c.id = :closetId
            """)
    Optional<Closet> findMyCloset(@Param("userId") Long userId, @Param("closetId") Long closetId);

    // 공개된 옷장 단건 조회
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
              AND c.id = :closetId
            """)
    Optional<Closet> findPublicCloset(@Param("closetId") Long closetId);

    // 수정 혹은 삭제할 옷장 조회
    @Query("""
            SELECT c
            FROM Closet c
            WHERE c.isDeleted = false
              AND c.id = :closetId
            """)
    Optional<Closet> findClosetById(@Param("closetId") Long closetId);
}