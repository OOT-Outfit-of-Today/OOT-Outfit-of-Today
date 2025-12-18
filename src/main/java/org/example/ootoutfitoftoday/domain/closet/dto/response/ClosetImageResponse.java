package org.example.ootoutfitoftoday.domain.closet.dto.response;

import org.example.ootoutfitoftoday.domain.image.entity.Image;

public record ClosetImageResponse(
        Long imageId,
        String imageUrl
) {
    public static ClosetImageResponse from(Image image) {
        if (image == null) {
            return null;
        }

        return new ClosetImageResponse(
                image.getId(),
                image.getUrl()
        );

    }
}
