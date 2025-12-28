package org.example.ootoutfitoftoday.domain.clothes.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.category.entity.Category;
import org.example.ootoutfitoftoday.domain.category.service.query.CategoryQueryServiceImpl;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClothesCommandServiceImpl implements ClothesCommandService {

    private final ClothesRepository clothesRepository;
    private final CategoryQueryServiceImpl categoryQueryService;
    private final UserQueryService userQueryService;

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
                clothesRequest.getDescription()
        );

        Clothes savedClothes = clothesRepository.save(clothes);

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
                clothesRequest.getDescription()
        );

        return ClothesResponse.from(clothes);
    }

    @Override
    public void softDeleteByUserIdAndClothesIdAndIsDeletedFalse(Long userId, Long clothesId) {
        Clothes clothes = clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> {
                    log.warn("deleteClothes - 옷 없음. clothesId={}", clothesId);
                    return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                });

        clothes.softDelete();
    }

    @Override
    public void clearCategoryFromClothes(List<Long> categoryIds) {
        clothesRepository.clearCategoryFromClothes(categoryIds);
    }

    @Override
    public void updateLastWornAt(Long clothesId, LocalDateTime wornAt) {
        // 메서드가 실행되기 전에 유저의 옷인지 앞에서 검증했음. -> id 값만으로 옷을 간단하게 조회
        Clothes clothes = clothesRepository.findById(clothesId).orElseThrow(
                () -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND)
        );

        LocalDateTime lastWornAt = clothes.getLastWornAt();

        // 오늘 등록하거나 저장된 등록일보다 최근이라면?
        if (lastWornAt == null || wornAt.isAfter(lastWornAt)) {
            clothes.updateLastWornAt(wornAt);
        }
    }
}