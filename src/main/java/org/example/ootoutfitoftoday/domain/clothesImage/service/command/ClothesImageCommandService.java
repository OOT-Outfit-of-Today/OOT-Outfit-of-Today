package org.example.ootoutfitoftoday.domain.clothesImage.service.command;

import org.example.ootoutfitoftoday.domain.clothesImage.dto.request.ClothesImageRequest;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageChangeMainResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageLinkResponse;

public interface ClothesImageCommandService {

    ClothesImageLinkResponse saveClothesImages(Long userId, Long clothesId, ClothesImageRequest clothesImageRequest);

    ClothesImageChangeMainResponse changeMainImage(Long userId, Long clothesId, Long clothesImageId);

    void removeClothesImages(Long userId, Long clothesId, ClothesImageRequest clothesImageRequest);

    int softDeleteAllByClothesIdIsDeletedFalse(Long id);
}
