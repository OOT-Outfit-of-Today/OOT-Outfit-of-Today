package org.example.ootoutfitoftoday.domain.clothesImage.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothesImage.entity.ClothesImage;

@Getter
public class ClothesImageResponse {

    @JsonIgnore // 서버 내부 매핑용 json 타입으로 반환하지 않도록 설정
    private final Long clothesId;

    private final Long imageId;
    private final String imageUrl;
    private final Boolean isMain;

    @Builder
    public ClothesImageResponse(Long clothesId, Long imageId, String imageUrl, Boolean isMain) {
        this.clothesId = clothesId;
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.isMain = isMain;
    }

    public static ClothesImageResponse from(ClothesImage clothesImage) {

        return ClothesImageResponse.builder()
                .clothesId(clothesImage.getClothes().getId())
                .imageId(clothesImage.getImage().getId())
                .imageUrl(clothesImage.getImage().getUrl())
                .isMain(clothesImage.getIsMain())
                .build();
    }
}
