package org.example.ootoutfitoftoday.domain.clothes.controller;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesDetailResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSummaryResponse;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clothes")
public class ClothesControllerImpl implements ClothesController {

    private final ClothesCommandService clothesCommandService;
    private final ClothesQueryService clothesQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<ClothesResponse>> createClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.createClothes(authUser.getUserId(), clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<SliceResponse<ClothesSummaryResponse>> getClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ClothesColor clothesColor,
            @RequestParam(required = false) ClothesSize clothesSize,
            @RequestParam(required = false) Long lastClothesId,
            @RequestParam(defaultValue = "10") int size
    ) {
        Slice<ClothesSummaryResponse> clothes = clothesQueryService.getClothes(
                authUser.getUserId(),
                categoryId,
                clothesColor,
                clothesSize,
                lastClothesId,
                size
        );

        return SliceResponse.success(clothes, ClothesSuccessCode.CLOTHES_OK);
    }

    @Override
    @GetMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesDetailResponse>> getClothesById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {
        ClothesDetailResponse clothesDetailResponse = clothesQueryService.getClothesById(authUser.getUserId(), clothesId);

        return Response.success(clothesDetailResponse, ClothesSuccessCode.CLOTHES_OK);
    }

    @Override
    @PutMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> updateClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId,
            @Valid @RequestBody ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.updateClothes(authUser.getUserId(), clothesId, clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_UPDATE);
    }

    @Override
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<Void>> deleteClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long clothesId
    ) {
        clothesCommandService.deleteClothes(authUser.getUserId(), clothesId);

        return Response.success(null, ClothesSuccessCode.CLOTHES_DELETE);
    }
}
