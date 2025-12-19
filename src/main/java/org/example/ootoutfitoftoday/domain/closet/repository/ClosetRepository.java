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
    @Query(
            value = """
                    SELECT c
                    FROM Closet c
                    LEFT JOIN FETCH c.image i
                    WHERE c.user.id = :userId
                      AND c.isDeleted = false
                    """,
            countQuery = """
                    SELECT count(c)
                    FROM Closet c
                    WHERE c.user.id = :userId
                      AND c.isDeleted = false
                    """)
    Page<Closet> findAllMyClosetsAndIsDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    // 해당 유저의 공개된 옷장
    @Query(
            value = """
                    SELECT c
                    FROM Closet c
                    LEFT JOIN FETCH c.image i
                    WHERE c.user.id = :userId
                      AND c.isDeleted = false
                      AND c.user.isDeleted = false
                      AND c.isPublic = true
                    """,
            countQuery = """
                    SELECT count(c)
                    FROM Closet c
                    WHERE c.user.id = :userId
                      AND c.isDeleted = false
                      AND c.user.isDeleted = false
                      AND c.isPublic = true
                    """)
    Page<Closet> findAllPublicClosetsByUserIdAndIsDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    // 공개된 옷장 전체 조회
    @Query(
            value = """
                    SELECT c
                    FROM Closet c
                    LEFT JOIN FETCH c.image i
                    WHERE c.isDeleted = false
                      AND c.user.isDeleted = false
                      AND c.isPublic = true
                    """,
            countQuery = """
                    SELECT count(c)
                    FROM Closet c
                    WHERE c.isDeleted = false
                      AND c.user.isDeleted = false
                      AND c.isPublic = true
                    """)
    Page<Closet> findAllPublicClosetsAndIsDeletedFalse(Pageable pageable);

    // 내 옷장 상세 조회
    @Query("""
            SELECT c
            FROM Closet c
            LEFT JOIN FETCH c.image i
            WHERE c.user.id = :userId
              AND c.isDeleted = false
              AND c.id = :closetId
            """)
    Optional<Closet> findMyClosetAndIsDeletedFalse(@Param("userId") Long userId, @Param("closetId") Long closetId);

    // 공개 옷장 상세 조회
    @Query("""
            SELECT c
            FROM Closet c
            LEFT JOIN FETCH c.image i
            WHERE c.isDeleted = false
              AND c.user.isDeleted = false
              AND c.isPublic = true
              AND c.id = :closetId
            """)
    Optional<Closet> findPublicClosetAndIsDeletedFalse(@Param("closetId") Long closetId);

    // 수정 혹은 삭제할 옷장 조회
    @Query("""
            SELECT c
            FROM Closet c
            LEFT JOIN FETCH c.image i
            WHERE c.isDeleted = false
              AND c.id = :closetId
              AND c.user.id = :userId
            """)
    Optional<Closet> findClosetByIdAndIsDeletedFalse(@Param("userId") Long userId, @Param("closetId") Long closetId);
}
