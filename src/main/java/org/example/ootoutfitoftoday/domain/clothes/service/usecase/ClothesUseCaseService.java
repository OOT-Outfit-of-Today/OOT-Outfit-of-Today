package org.example.ootoutfitoftoday.domain.clothes.service.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothesImage.service.command.ClothesImageCommandService;
import org.example.ootoutfitoftoday.domain.wearrecord.service.command.WearRecordCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClothesUseCaseService {

    private final ClothesCommandService clothesCommandService;
    private final WearRecordCommandService wearRecordCommandService;
    private final ClothesImageCommandService clothesImageCommandService;

    public void deleteClothes(Long userId, Long clothesId) {
        clothesCommandService.softDeleteByUserIdAndClothesIdAndIsDeletedFalse(userId, clothesId); // 옷 삭제
        wearRecordCommandService.softDeleteByUserIdAndClothesIdAndIsDeletedFalse(userId, clothesId); // 착용 기록 삭제
        clothesImageCommandService.softDeleteAllByClothesIdIsDeletedFalse(clothesId); // 옷 이미지 삭제
    }
}
