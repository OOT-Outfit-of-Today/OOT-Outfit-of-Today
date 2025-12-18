package org.example.ootoutfitoftoday.domain.clothesImage.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ClothesImageRequest {
    private List<Long> imageIds;
}
