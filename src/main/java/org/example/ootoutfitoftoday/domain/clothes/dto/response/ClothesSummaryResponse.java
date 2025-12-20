package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;

// 옷 전체 조회 반환 리스폰스
@Getter
@AllArgsConstructor
@Builder
public class ClothesSummaryResponse {

    private final Long id;
    private final Long categoryId;
    private final Long userId;
    private final ClothesSize clothesSize;
    private final ClothesColor clothesColor;
    private final String description;
    private final ClothesImageResponse clothesImages;

    public static ClothesSummaryResponse from(Clothes clothes, ClothesImageResponse clothesImages) {

        return ClothesSummaryResponse.builder()
                .id(clothes.getId())
                .categoryId(
                        clothes.getCategory() != null
                                ? clothes.getCategory().getId()
                                : null
                )
                .userId(clothes.getUser().getId())
                .clothesSize(clothes.getClothesSize())
                .clothesColor(clothes.getClothesColor())
                .description(clothes.getDescription())
                .clothesImages(clothesImages) // null이 들어오지 못하게 서비스에서 처리 필요함!
                .build();
    }
}
