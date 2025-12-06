package org.example.ootoutfitoftoday.domain.wearrecord.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.domain.clothes.entity.Clothes;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.example.ootoutfitoftoday.domain.user.entity.User;
import org.example.ootoutfitoftoday.domain.user.service.query.UserQueryService;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordErrorCode;
import org.example.ootoutfitoftoday.domain.wearrecord.exception.WearRecordException;
import org.example.ootoutfitoftoday.domain.wearrecord.repository.WearRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WearRecordCommandServiceImpl implements WearRecordCommandService {

    private final WearRecordRepository wearRecordRepository;
    private final ClothesCommandService clothesCommandService;
    private final UserQueryService userQueryService;
    private final ClothesQueryService clothesQueryService;

    @Override
    public WearRecordCreateResponse createWearRecord(Long userId, WearRecordCreateRequest request) {

        User user = userQueryService.findByIdAndIsDeletedFalse(userId);

        Clothes clothes = clothesQueryService.findClothesByIdAndUserIdAndIsDeletedFalse(userId, request.clothesId());

        LocalDateTime wornAt = LocalDateTime.now();
        WearRecord wearRecord = WearRecord.create(user, clothes, wornAt);
        WearRecord savedRecord = wearRecordRepository.save(wearRecord);

        clothesCommandService.updateLastWornAt(
                request.clothesId(),
                wornAt
        );
        log.debug("옷 마지막 착용 일시 업데이트 완료 - 옷 ID: {}", request.clothesId());

        return WearRecordCreateResponse.from(savedRecord.getId());
    }
}