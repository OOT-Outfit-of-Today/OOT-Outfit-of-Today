package org.example.ootoutfitoftoday.domain.recommendation.dto.response;

import com.ootcommon.recommendation.status.RecommendationStatus;
import com.ootcommon.recommendation.type.RecommendationType;
import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;
import org.example.ootoutfitoftoday.domain.recommendation.entity.Recommendation;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record RecommendationGetMyResponse(
        Long recommendationId,
        Long userId,
        Long clothesId,
        String clothesName,
        ClothesImageResponse clothesImage,
        RecommendationType type,
        String reason,
        RecommendationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RecommendationGetMyResponse from(
            Recommendation recommendation,
            ClothesImageResponse clothesImage
    ) {
        Clothes clothes = recommendation.getClothes();

        String name = clothes.getDescription();
        // 단방향 구조로 인해 이미지 DTO는 외부에서 조회한 값을 사용한다.

        return RecommendationGetMyResponse.builder()
                .recommendationId(recommendation.getId())
                .userId(recommendation.getUser().getId())
                .clothesId(clothes.getId())
                .clothesName(name)
                .clothesImage(clothesImage)
                .type(recommendation.getType())
                .reason(recommendation.getReason())
                .status(recommendation.getStatus())
                .createdAt(recommendation.getCreatedAt())
                .updatedAt(recommendation.getUpdatedAt())
                .build();
    }
}