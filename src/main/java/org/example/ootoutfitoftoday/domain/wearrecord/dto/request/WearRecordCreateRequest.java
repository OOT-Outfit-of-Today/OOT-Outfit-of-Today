package org.example.ootoutfitoftoday.domain.wearrecord.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WearRecordCreateRequest(

        @NotNull(message = "옷 ID는 필수입니다.")
        Long clothesId,

        LocalDate wornDate
) {
}