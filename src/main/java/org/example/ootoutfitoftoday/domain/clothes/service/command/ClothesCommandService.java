package org.example.ootoutfitoftoday.domain.clothes.service.command;

import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ClothesCommandService {

    ClothesResponse createClothes(Long userId, ClothesRequest clothesRequest);

    ClothesResponse updateClothes(
            Long userId,
            Long clothesId,
            ClothesRequest clothesRequest
    );

    void softDeleteByUserIdAndClothesIdAndIsDeletedFalse(Long userId, Long clothesId);

    void clearCategoryFromClothes(List<Long> categoryIds);

    void updateLastWornAt(Long clothesId, LocalDateTime wornAt);
}
