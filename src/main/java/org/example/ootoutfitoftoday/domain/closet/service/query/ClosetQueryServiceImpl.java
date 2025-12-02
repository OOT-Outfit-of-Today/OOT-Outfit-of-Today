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

    // 페이지네이션
    private Pageable createPageable(
            int page,
            int size,
            String sort,
            String direction
    ) {
        Sort sortSpec = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        return PageRequest.of(
                page,
                size,
                sortSpec
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

        Page<Closet> closets = closetRepository.findAllMyClosetsIsDeletedFalse(userId, pageable);

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
            closets = closetRepository.findAllPublicClosetsByUser_IdIsDeletedFalse(userId, pageable);
        } else {
            // 전체 공개된 옷장
            closets = closetRepository.findAllPublicClosetsIsDeletedFalse(pageable);
        }

        return closets.map(ClosetGetResponse::from);
    }

    // 해당 옷장 조회
    @Override
    public ClosetGetResponse getMyCloset(Long userId, Long closetId) {

        Closet closet = closetRepository.findMyClosetIsDeletedFalse(userId, closetId).orElseThrow(
                () -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND)
        );

        return ClosetGetResponse.from(closet);
    }

    // 공개 옷장 상세 조회
    @Override
    public ClosetGetResponse getPublicCloset(Long closetId) {

        Closet closet = closetRepository.findPublicClosetIsDeletedFalse(closetId).orElseThrow(
                () -> new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND)
        );

        return ClosetGetResponse.from(closet);
    }

    @Override
    public Closet findClosetById(Long closetId) {

        return closetRepository.findById(closetId).orElseThrow(
                () -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });
    }
}