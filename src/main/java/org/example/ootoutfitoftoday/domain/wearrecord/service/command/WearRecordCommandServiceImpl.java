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

import java.time.LocalDate;
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

        // 입력 받은 시각이 있다면 그 시각으로 없다면 api를 접근한 시각으로 등록
        LocalDate wornDate = request.wornDate() == null ? LocalDate.now() : request.wornDate();

        // LocalDate를 LocalDateTime으로 변환
        LocalDateTime wornAt = wornDate.atStartOfDay();

        // 미래 혹은 한달 이전 검증 로직
        validateWornAt(wornAt);

        LocalDateTime startOfDay = wornDate.atStartOfDay(); // 입력된 날의 00시 00분 00초
        LocalDateTime endOfDay = wornDate.plusDays(1).atStartOfDay(); // 입력된 날의 다음날 00시 00분 00초

        // 기준일에 이미 등록을 했다면 예외처리
        validateDuplicateWearRecord(
                userId,
                request.clothesId(),
                startOfDay,
                endOfDay
        );

        WearRecord wearRecord = WearRecord.create(user, clothes, wornAt);
        WearRecord savedRecord = wearRecordRepository.save(wearRecord);

        clothesCommandService.updateLastWornAt(
                request.clothesId(),
                wornAt
        );
        log.debug("옷 마지막 착용 일시 업데이트 완료 - 옷 ID: {}", request.clothesId());

        return WearRecordCreateResponse.from(savedRecord.getId());
    }

    // 미래 혹은 한달 이전 검증 로직
    private void validateWornAt(LocalDateTime wornAt) {
        LocalDateTime now = LocalDateTime.now();

        // 미래는 예외를 던짐
        if (wornAt.isAfter(now)) {
            throw new WearRecordException(WearRecordErrorCode.WORN_AT_IN_FUTURE);
        }

        if (wornAt.isBefore(now.minusMonths(1))) {
            throw new WearRecordException(WearRecordErrorCode.WORN_AT_OUT_OF_RANGE);
        }
    }

    // 기준일에 이미 등록을 했다면 예외처리
    private void validateDuplicateWearRecord(
            Long userId,
            Long clothesId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    ) {

        boolean exists = wearRecordRepository.existsByUserIdAndClothesIdAndWornAtBetween(
                userId,
                clothesId,
                startOfDay,
                endOfDay
        );

        if (exists) {
            throw new WearRecordException(WearRecordErrorCode.DUPLICATE_WEAR_RECORD_SAME_DAY);
        }
    }
}