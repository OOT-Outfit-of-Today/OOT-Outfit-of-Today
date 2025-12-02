package org.example.ootoutfitoftoday.domain.recommendation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.donation.dto.response.DonationCenterSearchResponse;
import org.example.ootoutfitoftoday.domain.recommendation.dto.request.RecommendationSalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.recommendation.dto.response.RecommendationGetMyResponse;
import org.example.ootoutfitoftoday.domain.recommendation.exception.RecommendationSuccessCode;
import org.example.ootoutfitoftoday.domain.recommendation.service.command.RecommendationCommandService;
import org.example.ootoutfitoftoday.domain.recommendation.service.query.RecommendationQueryService;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.SalePostCreateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recommendations")
public class RecommendationControllerImpl implements RecommendationController {

    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationQueryService recommendationQueryService;

    @Override
    @GetMapping
    public ResponseEntity<PageResponse<RecommendationGetMyResponse>> getMyRecommendations(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("추천 목록 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}, 정렬: {}, 방향: {}",
                authUser.getUserId(), page, size, sort, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sort)
        );

        Page<RecommendationGetMyResponse> responsePage = recommendationQueryService.getMyRecommendations(
                authUser.getUserId(),
                pageable
        );

        log.info("추천 목록 조회 완료 - 조회 건수: {}, 사용자: {}, 전체 건수: {}, 전체 페이지: {}",
                responsePage.getContent().size(), authUser.getUserId(),
                responsePage.getTotalElements(), responsePage.getTotalPages());

        return PageResponse.success(responsePage, RecommendationSuccessCode.RECOMMENDATION_GET_OK);
    }

    @Override
    @PostMapping("/{recommendationId}/sale-posts")
    public ResponseEntity<Response<SalePostCreateResponse>> createSalePostFromRecommendation(
            @PathVariable Long recommendationId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody RecommendationSalePostCreateRequest request
    ) {
        log.info("추천 기반 판매글 생성 요청 - 추천ID: {}, 사용자: {}", recommendationId, authUser.getUserId());
        log.debug("판매글 요청 상세 - 제목: {}, 가격: {}, 카테고리ID: {}",
                request.title(), request.price(), request.categoryId());

        SalePostCreateResponse response = recommendationCommandService.createSalePostFromRecommendation(
                recommendationId,
                authUser.getUserId(),
                request
        );

        log.info("추천 기반 판매글 생성 완료 - 추천ID: {}, 판매글ID: {}",
                recommendationId, response.getSalePostId());

        return Response.success(response, RecommendationSuccessCode.SALE_POST_FROM_RECOMMENDATION_CREATED);
    }

    @Override
    @GetMapping("/{recommendationId}/donation-centers")
    public ResponseEntity<Response<List<DonationCenterSearchResponse>>> searchDonationCentersFromRecommendation(
            @PathVariable Long recommendationId,
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String keyword
    ) {
        log.info("추천 기반 기부처 검색 요청 - 추천ID: {}, 사용자: {}, 반경: {}, 키워드: {}",
                recommendationId, authUser.getUserId(), radius, keyword);

        List<DonationCenterSearchResponse> donationCenters = recommendationQueryService.searchDonationCentersFromRecommendation(
                recommendationId,
                authUser.getUserId(),
                radius,
                keyword
        );

        log.info("기부처 검색 완료 - 검색 건수: {}, 추천ID: {}", donationCenters.size(), recommendationId);

        return Response.success(donationCenters, RecommendationSuccessCode.DONATION_CENTER_SEARCH_FROM_RECOMMENDATION_OK);
    }
}