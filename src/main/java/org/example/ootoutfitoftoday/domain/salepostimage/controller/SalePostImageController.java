package org.example.ootoutfitoftoday.domain.salepostimage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepostimage.dto.request.SalePostImageRequest;
import org.example.ootoutfitoftoday.domain.salepostimage.dto.response.SalePostImageResponse;
import org.example.ootoutfitoftoday.domain.salepostimage.exception.SalePostImageSuccessCode;
import org.example.ootoutfitoftoday.domain.salepostimage.service.command.SalePostImageCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "판매글 이미지 관리", description = "판매글 이미지 추가/수정/삭제 API (하이브리드 방식)")
@RestController
@RequestMapping("/v1/sale-posts/{salePostId}/images")
@RequiredArgsConstructor
public class SalePostImageController {

    private final SalePostImageCommandService salePostImageCommandService;

    // 이미지 추가
    @Operation(summary = "이미지 추가", description = "판매글에 새 이미지를 추가합니다.(기존 이미지 유지)")
    @PostMapping
    public ResponseEntity<Response<List<SalePostImageResponse>>> addImages(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid SalePostImageRequest request
    ) {
        List<SalePostImageResponse> response = salePostImageCommandService.addImages(
                salePostId,
                authUser.getUserId(),
                request.getImageIds()
        );

        return Response.success(response, SalePostImageSuccessCode.SALE_POST_IMAGES_ADDED);
    }
    // 이미지 전체 교체
    @Operation(summary = "이미지 전체 교체", description = "판매글의 모든 이미지를 새 이미지로 교체합니다.")
    @PutMapping
    public ResponseEntity<Response<List<SalePostImageResponse>>> replaceImages(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid SalePostImageRequest request
    ) {
        List<SalePostImageResponse> response = salePostImageCommandService.replaceImages(
                salePostId,
                authUser.getUserId(),
                request.getImageIds()
        );

        return Response.success(response, SalePostImageSuccessCode.SALE_POST_IMAGES_REPLACED);
    }

    // 이미지 개별 삭제(최소 1개 남김)
    @Operation(summary = "이미지 삭제", description = "판매글의 특정 이미지를 삭제합니다.(최소 1개는 남아야 함)")
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Response<Void>> deleteImage(
            @PathVariable Long salePostId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        salePostImageCommandService.deleteImage(
                salePostId,
                authUser.getUserId(),
                imageId
        );

        return Response.success(null, SalePostImageSuccessCode.SALE_POST_IMAGE_DELETED);
    }

    // 메인 이미지 변경
    @Operation(summary = "메인 이미지 변경", description = "판매글의 메인 이미지를 변경합니다.")
    @PatchMapping("/{imageId}/main")
    public ResponseEntity<Response<Void>> updateMainImage(
            @PathVariable Long salePostId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        salePostImageCommandService.updateMainImage(
                salePostId,
                authUser.getUserId(),
                imageId
        );

        return Response.success(null, SalePostImageSuccessCode.MAIN_SALE_POST_IMAGE_UPDATED);
    }

    // 이미지 순서 변경
    @Operation(summary = "이미지 순서 변경", description = "판매글 이미지의 표시 순서를 변경합니다")
    @PatchMapping("/order")
    public ResponseEntity<Response<Void>> reorderImages(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid SalePostImageRequest request
    ) {
        salePostImageCommandService.reorderImages(
                salePostId,
                authUser.getUserId(),
                request.getImageIds()
        );

        return Response.success(null, SalePostImageSuccessCode.SALE_POST_IMAGES_REPLACED);
    }
}