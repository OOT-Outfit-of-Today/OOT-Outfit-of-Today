package org.example.ootoutfitoftoday.domain.image.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.image.dto.request.ImageSaveRequest;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.ImageSaveResponse;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;
import org.example.ootoutfitoftoday.domain.image.exception.ImageSuccessCode;
import org.example.ootoutfitoftoday.domain.image.service.command.ImageCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class ImageControllerImpl implements ImageController {

    private final ImageCommandService imageCommandService;

    @Override
    @PostMapping("/presigned-urls")
    public ResponseEntity<Response<PresignedUrlResponse>> generatePresignedUrl(
            AuthUser authUser,
            PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = imageCommandService.generatePresignedUrl(
                authUser.getUserId(),
                request
        );

        return Response.success(response, ImageSuccessCode.PRESIGNED_URL_CREATED);
    }

    @Override
    @PostMapping
    public ResponseEntity<Response<ImageSaveResponse>> saveImage(
            ImageSaveRequest request
    ) {
        ImageSaveResponse response = imageCommandService.saveImage(request);

        return Response.success(response, ImageSuccessCode.IMAGE_SAVED);
    }
}