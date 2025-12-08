package org.example.ootoutfitoftoday.domain.wearrecord.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WearRecordCreateRequest(

        @NotNull(message = "옷 ID는 필수입니다.")
        Long clothesId,

        // baseDate 기준 값
        LocalDateTime wornAt
) {
}