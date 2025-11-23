package org.example.ootoutfitoftoday.domain.clothes.controller;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesImageUnlinkRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.exception.ClothesSuccessCode;
import org.example.ootoutfitoftoday.domain.clothes.service.command.ClothesCommandService;
import org.example.ootoutfitoftoday.domain.clothes.service.query.ClothesQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
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
            AuthUser authUser,
            ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.createClothes(authUser.getUserId(), clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_CREATED);
    }

    @Override
    @GetMapping
    public ResponseEntity<SliceResponse<ClothesResponse>> getClothes(
            AuthUser authUser,
            Long categoryId,
            ClothesColor clothesColor,
            ClothesSize clothesSize,
            Long lastClothesId,
            int size
    ) {
        Slice<ClothesResponse> clothes = clothesQueryService.getClothes(
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
    public ResponseEntity<Response<ClothesResponse>> getClothesById(
            AuthUser authUser,
            Long clothesId
    ) {
        ClothesResponse clothesResponse = clothesQueryService.getClothesById(authUser.getUserId(), clothesId);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_OK);
    }


    @Override
    @PutMapping("/{clothesId}")
    public ResponseEntity<Response<ClothesResponse>> updateClothes(
            AuthUser authUser,
            Long clothesId,
            ClothesRequest clothesRequest
    ) {
        ClothesResponse clothesResponse = clothesCommandService.updateClothes(authUser.getUserId(), clothesId, clothesRequest);

        return Response.success(clothesResponse, ClothesSuccessCode.CLOTHES_UPDATE);
    }

    @Override
    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Response<Void>> deleteClothes(
            AuthUser authUser,
            Long clothesId
    ) {
        clothesCommandService.deleteClothes(authUser.getUserId(), clothesId);

        return Response.success(null, ClothesSuccessCode.CLOTHES_DELETE);
    }

    @Override
    @PostMapping("/{clothesId}/images/remove")
    public ResponseEntity<Response<Void>> removeClothesImages(
            AuthUser authUser,
            Long clothesId,
            ClothesImageUnlinkRequest clothesImageUnlinkRequest
    ) {
        clothesCommandService.removeClothesImages(
                authUser.getUserId(),
                clothesId,
                clothesImageUnlinkRequest
        );

        return Response.success(null, ClothesSuccessCode.CLOTHES_IMAGE_REMOVE);
    }
}
