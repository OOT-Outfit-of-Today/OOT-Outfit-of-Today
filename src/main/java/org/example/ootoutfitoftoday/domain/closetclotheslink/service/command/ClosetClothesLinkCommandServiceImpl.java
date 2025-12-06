package org.example.ootoutfitoftoday.domain.closetclotheslink.service.command;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.service.query.ClosetQueryService;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.entity.ClosetClothesLink;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkErrorCode;
import org.example.ootoutfitoftoday.domain.closetclotheslink.exception.ClosetClothesLinkException;
import org.example.ootoutfitoftoday.domain.closetclotheslink.repository.ClosetClothesLinkRepository;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClosetClothesLinkCommandServiceImpl implements ClosetClothesLinkCommandService {

    private final ClosetQueryService closetQueryService;
    private final ClothesQueryService clothesQueryService;
    private final ClosetClothesLinkRepository closetClothesLinkRepository;

    @Override
    public ClosetClothesLinkResponse createClosetClothesLink(
            Long userId,
            Long closetId,
            ClosetClothesLinkRequest request
    ) {
        // 로그인 유저를 기준으로 옷장과 옷을 조회하기에 유저 자체를 검증하는 조건문이 필요없음.
        Closet closet = closetQueryService.findClosetByIdAndUserIdAndIsDeletedFalse(userId, closetId);
        Clothes clothes = clothesQueryService.findClothesByIdAndUserIdAndIsDeletedFalse(userId, request.clothesId());

        // 데이터가 존재할 수도 있고 존재하지 않을 수도 있기에 Optional로 구현
        Optional<ClosetClothesLink> existing = closetClothesLinkRepository.findByClosetIdAndClothesId(closetId, request.clothesId());

        // 데이터가 존재한다면?
        if (existing.isPresent()) {
            // 데이터를 변수에 담는다.
            ClosetClothesLink alreadyLinked = existing.get();

            // 삭제되지 않은 데이터라면?
            if (!alreadyLinked.isDeleted()) {
                throw new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOSET_CLOTHES_ALREADY_LINKED);
            }

            // 삭제되지 않은 상태로 변환
            alreadyLinked.restore();

            // 결과를 여기서 반환해서 로직 종료
            return ClosetClothesLinkResponse.from(alreadyLinked);
        }

        // 아래는 DB에 입력된 데이터가 존재하지 않을 경우 새로 insert 쿼리를 사용하여 새로운 데이터 생성하는 로직
        ClosetClothesLink link = ClosetClothesLink.create(closet, clothes);
        ClosetClothesLink savedLink = closetClothesLinkRepository.save(link);

        return ClosetClothesLinkResponse.from(savedLink);
    }

    @Override
    public ClosetClothesLinkDeleteResponse deleteClosetClothesLink(
            Long userId,
            Long closetId,
            Long clothesId
    ) {
        closetQueryService.findClosetByIdAndUserIdAndIsDeletedFalse(userId, closetId);

        ClosetClothesLink link = closetClothesLinkRepository.findByClosetIdAndClothesIdAndIsDeletedFalse(closetId, clothesId).orElseThrow(
                () -> new ClosetClothesLinkException(ClosetClothesLinkErrorCode.CLOTHES_NOT_LINKED)
        );

        link.softDelete();

        return ClosetClothesLinkDeleteResponse.of(closetId, clothesId);
    }
}