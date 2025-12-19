package org.example.ootoutfitoftoday.domain.closet.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetGetResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetErrorCode;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetException;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosetQueryServiceImpl implements ClosetQueryService {

    private final ClosetRepository closetRepository;

    // 페이지네이션 - 코드 리뷰 반영
    private Pageable createPageable(
            int page,
            int size,
            String sort,
            String direction
    ) {
        // direction이 null 이거나 공백이라면? 기본 값은 오름차순, 그외는 direction 값을 반환
        String defaultDirection = direction == null || direction.isBlank() ? "ASC" : direction;

        Sort.Direction directionEnum;
        try {
            directionEnum = Sort.Direction.fromString(defaultDirection);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 입력값으로 인해 기본값인 오름차순으로 정렬합니다.", direction);
            directionEnum = Sort.Direction.ASC;
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(directionEnum, sort)
        );
    }

    // 로그인 유저의 옷장 조회
    @Override
    public Page<ClosetGetResponse> getMyClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Pageable pageable = createPageable(
                page,
                size,
                sort,
                direction
        );

        Page<Closet> closets = closetRepository.findAllMyClosetsAndIsDeletedFalse(userId, pageable);

        return closets.map(ClosetGetResponse::from);
    }

    // 공개 옷장(특정 유저 or 전체)
    @Override
    public Page<ClosetGetResponse> getPublicClosets(
            Long userId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Pageable pageable = createPageable(
                page,
                size,
                sort,
                direction
        );

        Page<Closet> closets;

        if (userId != null) {
            // 특정 유저의 공개된 옷장
            closets = closetRepository.findAllPublicClosetsByUserIdAndIsDeletedFalse(userId, pageable);
        } else {
            // 전체 공개된 옷장
            closets = closetRepository.findAllPublicClosetsAndIsDeletedFalse(pageable);
        }

        return closets.map(ClosetGetResponse::from);
    }

    // 내 옷장 상세 조회
    @Override
    public ClosetGetResponse getMyCloset(Long userId, Long closetId) {

        Closet closet = closetRepository.findMyClosetAndIsDeletedFalse(userId, closetId).orElseThrow(
                () -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND)
        );

        return ClosetGetResponse.from(closet);
    }

    // 공개 옷장 상세 조회
    @Override
    public ClosetGetResponse getPublicCloset(Long closetId) {

        Closet closet = closetRepository.findPublicClosetAndIsDeletedFalse(closetId).orElseThrow(
                () -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND)
        );

        return ClosetGetResponse.from(closet);
    }

    // 내 옷장 상세 조회(옷장-옷, 로그인 유저의 옷장인지 검증하는 메서드)
    @Override
    public Closet findClosetByIdAndUserIdAndIsDeletedFalse(Long userId, Long closetId) {

        return closetRepository.findMyClosetAndIsDeletedFalse(userId, closetId).orElseThrow(
                () -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });
    }
}