package org.example.ootoutfitoftoday.domain.wearrecord.dto.response;

import org.example.ootoutfitoftoday.domain.wearrecord.entity.WearRecord;

import java.time.LocalDateTime;

public record WearRecordGetMyResponse(

        Long wearRecordId,
        LocalDateTime wornAt,
        Long clothesId,
        String clothesName
) {
    public static WearRecordGetMyResponse from(WearRecord wearRecord) {

        return new WearRecordGetMyResponse(
                wearRecord.getId(),
                wearRecord.getWornAt(),
                wearRecord.getClothes().getId(),
                wearRecord.getClothes().getDescription()
        );
    }
}