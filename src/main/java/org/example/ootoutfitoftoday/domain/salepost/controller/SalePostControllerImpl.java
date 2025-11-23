package org.example.ootoutfitoftoday.domain.salepost.controller;

import com.ootcommon.salepost.enums.SaleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SaleStatusUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
import org.example.ootoutfitoftoday.domain.salepost.exception.SalePostSuccessCode;
import org.example.ootoutfitoftoday.domain.salepost.service.command.SalePostCommandService;
import org.example.ootoutfitoftoday.domain.salepost.service.query.SalePostQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/sale-posts")
@RequiredArgsConstructor
public class SalePostControllerImpl implements SalePostController {

    private final SalePostCommandService salePostCommandService;
    private final SalePostQueryService salePostQueryService;

    @Override
    @PostMapping
    public ResponseEntity<Response<SalePostCreateResponse>> createSalePost(
            AuthUser authUser,
            SalePostCreateRequest request
    ) {
        SalePostCreateResponse response = salePostCommandService.createSalePost(
                authUser.getUserId(),
                request
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_CREATED);
    }

    @Override
    @GetMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> getSalePostDetail(Long salePostId) {

        SalePostDetailResponse response = salePostQueryService.getSalePostDetail(salePostId);

        return Response.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    @Override
    @GetMapping
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getSalePosts(
            Long categoryId,
            SaleStatus status,
            String keyword,
            int page,
            int size,
            String sort,
            Sort.Direction direction,
            AuthUser authUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        log.info("[GET] /v1/sale-posts: categoryId={}, status={}, keyword={}, pageable={}", categoryId, status, keyword, pageable);

        Slice<SalePostListResponse> salePosts = salePostQueryService.getSalePostList(
                authUser.getUserId(),
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }

    @Override
    @PutMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> updateSalePost(
            Long salePostId,
            AuthUser authUser,
            SalePostUpdateRequest request
    ) {
        SalePostDetailResponse response = salePostCommandService.updateSalePost(
                salePostId,
                authUser.getUserId(),
                request
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_UPDATED);
    }

    @Override
    @DeleteMapping("/{salePostId}")
    public ResponseEntity<Response<Void>> deleteSalePost(
            Long salePostId,
            AuthUser authUser
    ) {
        salePostCommandService.deleteSalePost(salePostId, authUser.getUserId());

        return Response.success(null, SalePostSuccessCode.SALE_POST_DELETED);
    }

    @Override
    @PatchMapping("/{salePostId}/status")
    public ResponseEntity<Response<SalePostDetailResponse>> updateSaleStatus(
            Long salePostId,
            AuthUser authUser,
            SaleStatusUpdateRequest request
    ) {
        SalePostDetailResponse response = salePostCommandService.updateSaleStatus(
                salePostId,
                authUser.getUserId(),
                request.getStatus()
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_STATUS_UPDATED);
    }

    @Override
    @GetMapping("/my")
    public ResponseEntity<Response<Slice<SalePostSummaryResponse>>> getMySalePosts(
            AuthUser authUser,
            SaleStatus status,
            int page,
            int size,
            String sort,
            Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostSummaryResponse> response = salePostQueryService.findMySalePosts(
                authUser.getUserId(),
                status,
                pageable
        );

        return Response.success(response, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }

    @Override
    @GetMapping("/public")
    public ResponseEntity<Response<Slice<SalePostPublicListResponse>>> getNotAuthSalePosts(
            Long categoryId,
            SaleStatus status,
            String keyword,
            int page,
            int size,
            String sort,
            Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostPublicListResponse> salePosts = salePostQueryService.getNotAuthSalePostList(
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }
}