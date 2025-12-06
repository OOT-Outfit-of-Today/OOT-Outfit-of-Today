package org.example.ootoutfitoftoday.domain.closetclotheslink.repository;

import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClosetClothesLinkRepository extends JpaRepository<ClosetClothesLink, Long> {

    // 옷장에 등록된 옷인지 아닌지 검증하는 쿼리 메서드
    Optional<ClosetClothesLink> findByClosetIdAndClothesId(Long closetId, Long clothesId);

    Page<ClosetClothesLink> findAllByClosetId(Long closetId, Pageable pageable);

    Optional<ClosetClothesLink> findByClosetIdAndClothesIdAndIsDeletedFalse(Long closetId, Long clothesId);
}
