package org.example.ootoutfitoftoday.domain.recommendation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "추천 기록 관리", description = "기부/판매 추천 기록 조회 및 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface RecommendationController {

    @Operation(
            summary = "추천 기록 목록 조회",
            description = """
                    로그인한 사용자의 기부/판매 추천 목록을 페이징하여 조회합니다.
                    기본적으로 생성일 기준 최신순으로 정렬됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "추천 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ResponseEntity<PageResponse<RecommendationGetMyResponse>> getMyRecommendations(
            AuthUser authUser,
            int page,
            int size,
            String sort,
            String direction
    );

    @Operation(
            summary = "추천 → 판매글 생성",
            description = """
                    ACCEPTED 상태의 판매 추천을 기반으로 판매글을 생성합니다.
                    이미 판매글이 존재한다면 기존 글을 반환합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "판매글 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 추천 상태"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음"),
                    @ApiResponse(responseCode = "409", description = "이미 판매글 존재")
            }
    )
    ResponseEntity<Response<SalePostCreateResponse>> createSalePostFromRecommendation(
            Long recommendationId,
            AuthUser authUser,
            RecommendationSalePostCreateRequest request
    );

    @Operation(
            summary = "기부 추천 → 주변 기부처 검색",
            description = """
                    ACCEPTED 상태의 기부 추천에서 사용자의 거래 위치 기반으로
                    주변 기부처를 거리순으로 검색합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "기부처 검색 성공"),
                    @ApiResponse(responseCode = "400", description = "기부 타입이 아님"),
                    @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<List<DonationCenterSearchResponse>>> searchDonationCentersFromRecommendation(
            Long recommendationId,
            AuthUser authUser,
            Integer radius,
            String keyword
    );
}