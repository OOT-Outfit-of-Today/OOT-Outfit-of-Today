package org.example.ootoutfitoftoday.domain.closet.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import org.example.ootoutfitoftoday.domain.closet.entity.Closet;
import org.example.ootoutfitoftoday.domain.image.entity.Image;

import java.time.LocalDateTime;

@Builder(access = AccessLevel.PRIVATE)
public record ClosetGetResponse(
        Long closetId,
        String name,
        String description,
        ClosetImageResponse closetImage,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ClosetGetResponse from(Closet closet) {
        ClosetImageResponse closetImage = null;

        Image image = closet.getImage();
        if (image != null) {
            closetImage = ClosetImageResponse.from(image);
        }

        return from(closet, closetImage);
    }

    public static ClosetGetResponse from(
            Closet closet,
            ClosetImageResponse closetImage
    ) {

        return ClosetGetResponse.builder()
                .closetId(closet.getId())
                .name(closet.getName())
                .description(closet.getDescription())
                .closetImage(closetImage)
                .isPublic(closet.getIsPublic())
                .createdAt(closet.getCreatedAt())
                .updatedAt(closet.getUpdatedAt())
                .build();
    }
}