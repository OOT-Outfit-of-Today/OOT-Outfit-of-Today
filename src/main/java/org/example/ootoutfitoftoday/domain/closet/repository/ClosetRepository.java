package org.example.ootoutfitoftoday.domain.closet.repository;

import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosetRepository extends JpaRepository<Closet, Long> {

    // 내 옷장 조회용(공개 비공개 모두 조회)
    Page<Closet> findAllByUser_Id(Long userId, Pageable pageable);

    // 해당 유저의 공개된 옷장
    Page<Closet> findAllByUser_IdAndIsPublicTrue(Long userId, Pageable pageable);

    // 공개된 옷장 전체 조회
    Page<Closet> findAllByIsPublicTrue(Pageable pageable);
}