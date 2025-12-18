package org.example.ootoutfitoftoday.domain.clothesImage.service.query;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.repository.ClothesImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClothesImageQueryServiceImpl implements ClothesImageQueryService {

    private final ClothesImageRepository clothesImageRepository;

    // 옷 전체 조회 시 반환되는 메인 이미지
    @Override
    public List<ClothesImageResponse> findMainImageByClothesId(List<Long> clothesIds) {
        if (clothesIds == null || clothesIds.isEmpty()) {
            return List.of(); // 빈리스트 반환
        }

        return clothesImageRepository.findMainImagesByClothesIdsAndIsDeletedFalse(clothesIds)
                .stream()
                .map(ClothesImageResponse::from)
                .toList();
    }

    // 옷 단건 조회 시 반환되는 이미지들
    @Override
    public List<ClothesImageResponse> findImagesByClothesId(Long clothesId) {
        if (clothesId == null) {
            return List.of();
        }

        return clothesImageRepository.findByClothesIdAndIsDeletedFalse(clothesId)
                .stream()
                .map(ClothesImageResponse::from)
                .toList();
    }
}
