package org.example.ootoutfitoftoday.domain.clothes.service.query;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesDetailResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSummaryResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesErrorCode;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesException;
import org.example.ootoutfitoftoday.domain.clothes.repository.ClothesRepository;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.service.query.ClothesImageQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClothesQueryServiceImpl implements ClothesQueryService {

    private final ClothesRepository clothesRepository;
    private final ClothesImageQueryService clothesImageQueryService;

    @Override
    public Slice<ClothesSummaryResponse> getClothes(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId,
            int size
    ) {
        Slice<Clothes> clothesSlice = clothesRepository.findAllByIsDeletedFalse(
                userId,
                categoryId,
                clothesColor,
                clothesSize,
                lastClothesId,
                size
        );

        List<Long> clothesIds = clothesSlice
                .getContent()
                .stream()
                .map(Clothes::getId)
                .toList();

        List<ClothesImageResponse> mainImages = clothesImageQueryService.findMainImageByClothesId(clothesIds);

        Map<Long, ClothesImageResponse> mainImageMap = mainImages.stream()
                .collect(Collectors.toMap(
                        ClothesImageResponse::getClothesId,
                        it -> it,
                        (a, b) -> a
                ));

        List<ClothesSummaryResponse> response = clothesSlice
                .getContent()
                .stream()
                .map(clothes -> ClothesSummaryResponse.from(
                        clothes,
                        mainImageMap.get(clothes.getId())
                ))
                .toList();

        return new SliceImpl<>(response, clothesSlice.getPageable(), clothesSlice.hasNext());
    }

    @Override
    public ClothesDetailResponse getClothesById(Long userId, Long clothesId) {
        Clothes clothes = clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> {
                    log.warn("getClothesById - 옷 없음. id={}", clothesId);

                    return new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND);
                }
        );

        List<ClothesImageResponse> images = clothesImageQueryService.findImagesByClothesId(clothesId);

        return ClothesDetailResponse.from(clothes, images);
    }

    @Override
    public int countClothesByIsDeletedFalse() {

        return clothesRepository.countAllClothesByIsDeletedFalse();
    }

    @Override
    public List<CategoryStat> countTopCategoryStats() {

        return clothesRepository.countTopCategoryStats();
    }

    @Override
    public List<ClothesColorCount> clothesColorsCount() {

        return clothesRepository.clothesColorsCount();
    }

    @Override
    public List<ClothesSizeCount> clothesSizesCount() {

        return clothesRepository.clothesSizesCount();
    }

    @Override
    public List<CategoryStat> findTopCategoryStats() {

        return clothesRepository.findTopCategoryStats();
    }

    @Override
    public int countAllClothesByUserIdAndIsDeletedFalse(Long userId) {

        return clothesRepository.countAllClothesByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<CategoryStat> countUserTopCategoryStats(Long userId) {

        return clothesRepository.countUserTopCategoryStats(userId);
    }

    @Override
    public List<Clothes> findAllClothesByUserId(Long userId) {

        return clothesRepository.findAllByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public List<ClothesWearCount> leastWornClothes(Long userId) {

        return clothesRepository.leastWornClothes(userId);
    }

    @Override
    public List<NotWornOverPeriod> notWornOverPeriod(Long userId) {
        List<NotWornOverPeriod> result = clothesRepository.notWornOverPeriod(userId)
                .stream()
                .map(dto -> NotWornOverPeriod.builder()
                        .clothesId(dto.getClothesId())
                        .clothesDescription(dto.getClothesDescription())
                        .lastWornAt(dto.getLastWornAt())
                        .daysNotWorn(dto.getLastWornAt() == null
                                ? 0L
                                : ChronoUnit.DAYS.between(dto.getLastWornAt(), LocalDateTime.now()))
                        .build())
                .toList();

        return result;
    }

    @Override
    public Clothes findClothesByIdAndUserIdAndIsDeletedFalse(Long userId, Long clothesId) {

        return clothesRepository.findClothesByIdAndUserIdAndIsDeletedFalse(userId, clothesId).orElseThrow(
                () -> new ClothesException(ClothesErrorCode.CLOTHES_NOT_FOUND)
        );
    }
}