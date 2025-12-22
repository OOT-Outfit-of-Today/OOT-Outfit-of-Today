package org.example.ootoutfitoftoday.domain.clothesImage.controller;

import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.request.ClothesImageRequest;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageChangeMainResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.dto.response.ClothesImageLinkResponse;
import org.example.ootoutfitoftoday.domain.clothesImage.exception.ClothesImageSuccessCode;
import org.example.ootoutfitoftoday.domain.clothesImage.service.command.ClothesImageCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes/{clothesId}/images")
public class ClothesImageControllerImpl implements ClothesImageController {
    private final ClothesImageCommandService clothesImageCommandService;

    // 이미지 연결
    @PostMapping
    public ResponseEntity<Response<ClothesImageLinkResponse>> linkImages(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @RequestBody ClothesImageRequest clothesImageRequest
    ) {
        ClothesImageLinkResponse clothesImageLinkResponse = clothesImageCommandService.saveClothesImages(
                authUser.getUserId(),
                clothesId,
                clothesImageRequest
        );

        return Response.success(clothesImageLinkResponse, ClothesImageSuccessCode.CLOTHES_IMAGE_LINK);
    }

    // 메인 이미지 변경 clothesImageId 라고 한 이유는 이미 연결된 옷 이미지의 상태를 변환시킨다고 생각해서..
    @PatchMapping("/{clothesImageId}/main")
    public ResponseEntity<Response<ClothesImageChangeMainResponse>> changeMainImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @PathVariable Long clothesImageId
    ) {
        ClothesImageChangeMainResponse clothesImageChangeMainResponse = clothesImageCommandService.changeMainImage(
                authUser.getUserId(),
                clothesId,
                clothesImageId
        );

        return Response.success(clothesImageChangeMainResponse, ClothesImageSuccessCode.CLOTHES_IMAGE_LINK);
    }

    // 이미지 제거
    @DeleteMapping
    public ResponseEntity<Response<Void>> removeImages(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @RequestBody ClothesImageRequest clothesImageRequest
    ) {
        clothesImageCommandService.removeClothesImages(
                authUser.getUserId(),
                clothesId,
                clothesImageRequest
        );

        return Response.success(null, ClothesImageSuccessCode.CLOTHES_IMAGE_REMOVE);
    }
}
