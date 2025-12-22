package org.example.ootoutfitoftoday.domain.clothesImage.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ClothesImageLinkResponse {
    private final Long userId;
    private final Long clothesId;
    private final List<ClothesImageResponse> clothesImages;

    @Builder
    public ClothesImageLinkResponse(Long userId, Long clothesId, List<ClothesImageResponse> clothesImages) {
        this.userId = userId;
        this.clothesId = clothesId;
        this.clothesImages = clothesImages;
    }

    public static ClothesImageLinkResponse from(Long userId, Long clothesId, List<ClothesImageResponse> clothesImages) {

        return ClothesImageLinkResponse.builder()
                .userId(userId)
                .clothesId(clothesId)
                .clothesImages(clothesImages)
                .build();
    }
}
