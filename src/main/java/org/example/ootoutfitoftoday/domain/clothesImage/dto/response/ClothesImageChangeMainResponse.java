package org.example.ootoutfitoftoday.domain.clothesImage.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ClothesImageChangeMainResponse {

    private final Long userId;
    private final Long clothesId;
    private final ClothesImageResponse clothesImages;

    @Builder
    public ClothesImageChangeMainResponse(
            Long userId,
            Long clothesId,
            ClothesImageResponse clothesImages
    ) {
        this.userId = userId;
        this.clothesId = clothesId;
        this.clothesImages = clothesImages;
    }

    public static ClothesImageChangeMainResponse from(
            Long userId,
            Long clothesId,
            ClothesImageResponse clothesImages
    ) {

        return ClothesImageChangeMainResponse.builder()
                .userId(userId)
                .clothesId(clothesId)
                .clothesImages(clothesImages)
                .build();
    }
}
