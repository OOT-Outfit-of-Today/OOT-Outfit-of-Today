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
    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort sortSpec = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();

        return PageRequest.of(page, size, sortSpec);
    }

    // 로그인 유저의 옷장 조회
    @Override
    public Page<ClosetGetResponse> getMyClosets(
            Long loginUserId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Pageable pageable = createPageable(page, size, sort, direction);

        Page<Closet> closets = closetRepository.findAllByUser_Id(loginUserId, pageable);

        return closets.map(ClosetGetResponse::from);
    }

    // 공개 옷장(특정 유저 or 전체)
    @Override
    public Page<ClosetGetResponse> getPublicClosets(
            Long targetUserId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Pageable pageable = createPageable(page, size, sort, direction);

        Page<Closet> closets;

        if (targetUserId != null) {
            // 특정 유저의 공개된 옷장
            closets = closetRepository.findAllByUser_IdAndIsPublicTrue(targetUserId, pageable);
        } else {
            // 전체 공개된 옷장
            closets = closetRepository.findAllByIsPublicTrue(pageable);
        }

        return closets.map(ClosetGetResponse::from);
    }

    @Override
    public ClosetGetResponse getCloset(Long closetId) {

        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        if (closet.isDeleted()) {
            log.warn("삭제된 옷장 - 옷장ID: {}", closetId);
            throw new ClosetException(ClosetErrorCode.CLOSET_DELETED);
        }

        return ClosetGetResponse.from(closet);
    }

    @Override
    public Closet findClosetById(Long closetId) {

        return closetRepository.findById(closetId)
                .orElseThrow(() -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });
    }
}