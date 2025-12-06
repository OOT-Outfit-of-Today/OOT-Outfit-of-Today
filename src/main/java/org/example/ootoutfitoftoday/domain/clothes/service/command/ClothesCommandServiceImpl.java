package org.example.ootoutfitoftoday.domain.clothes.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryServiceImpl;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesImageUnlinkRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.example.ootoutfitoftoday.domain.clothesImage.service.command.ClothesImageCommandService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesCommandServiceImpl implements ClothesCommandService {

    private final ClothesRepository clothesRepository;
    private final CategoryQueryServiceImpl categoryQueryService;
    private final UserQueryService userQueryService;
    private final ClothesImageCommandService clothesImageCommandService;

    @Override
    public ClothesResponse createClothes(Long userId, ClothesRequest clothesRequest) {
        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Category category = null;

        if (clothesRequest.getCategoryId() != null) {
            category = categoryQueryService.findById(clothesRequest.getCategoryId());
        }

        Clothes clothes = Clothes.create(
                category,
                user,
                clothesRequest.getClothesSize(),
                clothesRequest.getClothesColor(),
                clothesRequest.getDescription(),
                new ArrayList<>()
        );

        Clothes savedClothes = clothesRepository.save(clothes);

        if (clothesRequest.getImages() != null && !clothesRequest.getImages().isEmpty()) {

            clothesImageCommandService.saveClothesImages(savedClothes, clothesRequest.getImages());
        }

        return ClothesResponse.from(savedClothes);
    }

    @Override
    public ClothesResponse updateClothes(
            Long userId,
            Long clothesId,
            ClothesRequest clothesRequest
    ) {
        Clothes clothes = clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> {
                    log.warn("deleteClothes - 옷 없음. clothesId={}", clothesId);
                    return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                });

        Category category = categoryQueryService.findById(clothesRequest.getCategoryId());

        clothes.update(
                category,
                clothesRequest.getClothesSize(),
                clothesRequest.getClothesColor(),
                clothesRequest.getDescription(),
                new ArrayList<>()
        );

        if (clothesRequest.getImages() != null && !clothesRequest.getImages().isEmpty()) {

            clothesImageCommandService.updateClothesImages(clothes, clothesRequest.getImages());
        }

        return ClothesResponse.from(clothes);
    }

    @Override
    public void deleteClothes(Long userId, Long clothesId) {
        Clothes clothes = clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> {
                    log.warn("deleteClothes - 옷 없음. clothesId={}", clothesId);
                    return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                });

        clothes.softDelete();

        clothesImageCommandService.softDeleteAllByClothesId(clothesId);
    }

    @Override
    public void clearCategoryFromClothes(List<Long> categoryIds) {

        clothesRepository.clearCategoryFromClothes(categoryIds);
    }

    // todo: 현재 구조상 "유저의 옷인지 검증한 데이터"를 가지고 조회하기에 id로 조회가 가능하게 구현했음. 하지만 추후에 작업할 때 이점 참고해서 리팩토링 진행할 것!
    @Override
    public void updateLastWornAt(Long clothesId, LocalDateTime wornAt) {
        Clothes clothes = clothesRepository.findById(clothesId).orElseThrow(
                () -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND)
        );

        clothes.updateLastWornAt(wornAt);
    }

    @Override
    public void removeClothesImages(
            Long userId,
            Long clothesId,
            ClothesImageUnlinkRequest clothesImageUnlinkRequest
    ) {
        clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> {
                    log.warn("removeClothesImages - 옷 없음. clothesId={}", clothesId);
                    return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                });

        clothesImageCommandService.removeClothesImages(clothesId, clothesImageUnlinkRequest.getImageIds());
    }
}