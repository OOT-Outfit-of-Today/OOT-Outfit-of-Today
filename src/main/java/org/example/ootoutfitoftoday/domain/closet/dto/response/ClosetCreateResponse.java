package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record ClosetCreateResponse(
        Long closetId,
        Long userId,
        String name,
        String description,
        ClosetImageResponse closetImage,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ClosetCreateResponse from(
            Closet closet,
            ClosetImageResponse closetImage
    ) {

        return ClosetCreateResponse.builder()
                .closetId(closet.getId())
                .userId(closet.getUserId())
                .name(closet.getName())
                .description(closet.getDescription())
                .closetImage(closetImage)
                .isPublic(closet.getIsPublic())
                .createdAt(closet.getCreatedAt())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}