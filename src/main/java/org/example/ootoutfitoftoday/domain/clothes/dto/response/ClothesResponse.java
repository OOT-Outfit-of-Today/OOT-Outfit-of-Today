package org.example.ootoutfitoftoday.domain.clothes.dto.response;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;

@Getter
@Builder
@AllArgsConstructor
public class ClothesResponse {

    private final Long id;
    private final Long categoryId;
    private final Long userId;
    private final ClothesSize clothesSize;
    private final ClothesColor clothesColor;
    private final String description;

    public static ClothesResponse from(Clothes clothes) {

        return ClothesResponse.builder()
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
                .build();
    }
}
