package org.example.ootoutfitoftoday.domain.clothesImage.service.query;

import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;

import java.util.List;

public interface ClothesImageQueryService {

    // 옷 전체 조회 시 각 옷에 메인 이미지들만 노출 시키도록 구현
    List<ClothesImageResponse> findMainImageByClothesId(List<Long> clothesIds);

    // 옷 단건 조회 시 해당 옷에 등록된 이미지들 노출 시키도록 구현
    List<ClothesImageResponse> findImagesByClothesId(Long clothesId);
}
