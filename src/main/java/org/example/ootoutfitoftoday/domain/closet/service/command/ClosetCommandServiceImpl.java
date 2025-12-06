package org.example.ootoutfitoftoday.domain.closet.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetCreateResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetDeleteResponse;
import org.example.ootoutfitoftoday.domain.closet.dto.response.ClosetUpdateResponse;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetErrorCode;
import org.example.ootoutfitoftoday.domain.closet.exception.ClosetException;
import org.example.ootoutfitoftoday.domain.closet.repository.ClosetRepository;
import org.example.ootoutfitoftoday.domain.image.entity.Image;
import org.example.ootoutfitoftoday.domain.image.service.query.ImageQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClosetCommandServiceImpl implements ClosetCommandService {

    private final ClosetRepository closetRepository;
    private final UserQueryService userQueryService;
    private final ImageQueryService imageQueryService;

    @Override
    public ClosetCreateResponse createCloset(Long userId, ClosetRequest request) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Closet closet = Closet.create(
                user,
                request.name(),
                request.description(),
                request.isPublic()
        );

        if (request.imageId() != null) {
            Image image = imageQueryService.findImageById(request.imageId());
            closet.setClosetImage(image);
        }

        Closet savedCloset = closetRepository.save(closet);

        return ClosetCreateResponse.from(savedCloset);
    }

    @Override
    public ClosetUpdateResponse updateCloset(
            Long userId,
            Long closetId,
            ClosetRequest request
    ) {

        Closet updatedCloset = closetRepository.findClosetByIdAndIsDeletedFalse(userId, closetId).orElseThrow(
                () -> {
                    log.warn("옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        Image newImage = null;
        if (request.imageId() != null) {
            newImage = imageQueryService.findImageById(request.imageId());
        }

        updatedCloset.update(
                request.name(),
                request.description(),
                request.isPublic()
        );

        updatedCloset.setClosetImage(newImage);

        return ClosetUpdateResponse.from(updatedCloset);
    }

    @Override
    public ClosetDeleteResponse deleteCloset(Long userId, Long closetId) {

        Closet closet = closetRepository.findClosetByIdAndIsDeletedFalse(userId, closetId).orElseThrow(
                () -> {
                    log.warn("삭제할 옷장을 찾을 수 없음 - 옷장ID: {}", closetId);
                    return new ClosetException(ClosetErrorCode.CLOSET_NOT_FOUND);
                });

        closet.softDelete();

        if (closet.getClosetImage() != null) {
            closet.getClosetImage().softDelete();
        }

        return ClosetDeleteResponse.of(closet.getId(), closet.getDeletedAt());
    }
}