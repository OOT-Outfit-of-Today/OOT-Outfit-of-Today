package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record ClosetUpdateResponse(
        Long closetId,
        String name,
        String description,
        ClosetImageResponse image,
        Boolean isPublic,
        LocalDateTime updatedAt
) {
    public static ClosetUpdateResponse from(Closet closet) {
        ClosetImageResponse image = ClosetImageResponse.from(closet.getImage());

        return ClosetUpdateResponse.builder()
                .closetId(closet.getId())
                .name(closet.getName())
                .description(closet.getDescription())
                .image(image)
                .isPublic(closet.getIsPublic())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}