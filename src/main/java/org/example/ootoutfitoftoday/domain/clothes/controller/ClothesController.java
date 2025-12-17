package org.example.ootoutfitoftoday.domain.clothes.controller;

import com.ootcommon.clothes.enums.ClothesColor;
import com.ootcommon.clothes.enums.ClothesSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.common.response.SliceResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.clothes.dto.request.ClothesRequest;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesDetailResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesResponse;
import org.example.ootoutfitoftoday.domain.clothes.dto.response.ClothesSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "옷 관리", description = "옷관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface ClothesController {

    @Operation(
            summary = "옷 등록",
            description = "회원이 자신의 옷을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    ResponseEntity<Response<ClothesResponse>> createClothes(
            AuthUser authUser,
            ClothesRequest clothesRequest
    );

    @Operation(
            summary = "옷 전체 조회",
            description = "회원이 자신의 옷을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<SliceResponse<ClothesSummaryResponse>> getClothes(
            @AuthenticationPrincipal AuthUser authUser,
            @Parameter(description = "카테고리 ID") Long categoryId,
            @Parameter(description = "옷 색상") ClothesColor clothesColor,
            @Parameter(description = "옷 사이즈") ClothesSize clothesSize,
            @Parameter(description = "마지막 조회 옷 아이디") Long lastClothesId,
            @Parameter(description = "페이지당 개수") int size
    );

    @Operation(
            summary = "해당 옷 조회",
            description = "회원이 자신의 옷을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    ResponseEntity<Response<ClothesDetailResponse>> getClothesById(
            AuthUser authUser,
            Long clothesId
    );

    @Operation(
            summary = "해당 옷 수정",
            description = "회원이 자신의 옷을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    ResponseEntity<Response<ClothesResponse>> updateClothes(
            AuthUser authUser,
            Long clothesId,
            ClothesRequest clothesRequest
    );

    @Operation(
            summary = "해당 옷 삭제",
            description = "회원이 자신의 옷을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음")
            })
    ResponseEntity<Response<Void>> deleteClothes(
            AuthUser authUser,
            Long clothesId
    );
}
