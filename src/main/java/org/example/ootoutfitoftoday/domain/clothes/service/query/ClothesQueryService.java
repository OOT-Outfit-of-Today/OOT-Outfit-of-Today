package org.example.ootoutfitoftoday.domain.clothes.service.query;

import com.ootcommon.category.response.CategoryStat;
import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import com.ootcommon.clothes.response.ClothesColorCount;
import com.ootcommon.clothes.response.ClothesSizeCount;
import com.ootcommon.wearrecord.response.ClothesWearCount;
import com.ootcommon.wearrecord.response.NotWornOverPeriod;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesDetailResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSummaryResponse;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ClothesQueryService {

    Slice<ClothesSummaryResponse> getClothes(
            Long userId,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId,
            int size
    );

    ClothesDetailResponse getClothesById(Long userId, Long clothesId);

    int countClothesByIsDeletedFalse();

    List<CategoryStat> countTopCategoryStats();

    List<ClothesColorCount> clothesColorsCount();

    List<ClothesSizeCount> clothesSizesCount();

    List<CategoryStat> findTopCategoryStats();

    int countAllClothesByUserIdAndIsDeletedFalse(Long userId);

    List<CategoryStat> countUserTopCategoryStats(Long userId);

    List<Clothes> findAllClothesByUserId(Long userId);

    List<ClothesWearCount> leastWornClothes(Long userId);

    List<NotWornOverPeriod> notWornOverPeriod(Long userId);

    // 옷장 - 옷 연결할 옷 조회
    Clothes findClothesByIdAndUserIdAndIsDeletedFalse(Long userId, Long clothesId);
}