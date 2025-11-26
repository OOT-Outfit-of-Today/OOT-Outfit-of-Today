package org.example.ootoutfitoftoday.domain.closet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record ClosetRequest(

        @NotBlank(message = "옷장 이름은 필수입니다.")
        @Size(max = 100, message = "옷장 이름은 100자를 초과할 수 없습니다.")
        String name,

        @Size(max = 255, message = "옷장 설명은 255자를 초과할 수 없습니다.")
        String description,

        Long imageId,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic
) {
    public static ClosetRequest of(
            String name,
            String description,
            Long imageId,
            Boolean isPublic
    ) {

        return ClosetRequest.builder()
                .name(name)
                .description(description)
                .imageId(imageId)
                .isPublic(isPublic)
                .build();
    }
}