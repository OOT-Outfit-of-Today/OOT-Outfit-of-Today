package org.example.ootoutfitoftoday.domain.closetclotheslink.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkGetResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkErrorCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkException;
import org.example.ootoutfitoftoday.domain.closetclotheslink.repository.ClosetClothesLinkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosetClothesLinkQueryServiceImpl implements ClosetClothesLinkQueryService {

    private final ClosetQueryService closetQueryService;
    private final ClosetClothesLinkRepository closetClothesLinkRepository;

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

    @Override
    public Page<ClosetClothesLinkGetResponse> getClothesInCloset(
            Long userId,
            Long closetId,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Closet closet = closetQueryService.findClosetById(closetId);

        if (!Objects.equals(closet.getUserId(), userId)) {
            throw new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOSET_CLOTHES_FORBIDDEN);
        }

        Pageable pageable = createPageable(
                page,
                size,
                sort,
                direction
        );

        Page<ClosetClothesLink> links = closetClothesLinkRepository.findAllByClosetId(closetId, pageable);

        return links.map(ClosetClothesLinkGetResponse::from);
    }
}