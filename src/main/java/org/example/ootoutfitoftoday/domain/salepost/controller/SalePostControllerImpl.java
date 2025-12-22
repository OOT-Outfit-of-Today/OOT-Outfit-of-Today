package org.example.ootoutfitoftoday.domain.salepost.controller;

import com.ootcommon.salepost.enums.SaleStatus;
import jakarta.validation.Valid;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostCreateRequest request
    ) {
        SalePostCreateResponse response = salePostCommandService.createSalePost(
                authUser.getUserId(),
                request
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_CREATED);
    }

    @Override
    @GetMapping("/{salePostId}")
    public ResponseEntity<Response<SalePostDetailResponse>> getSalePostDetail(
            @PathVariable Long salePostId
    ) {
        SalePostDetailResponse response = salePostQueryService.getSalePostDetail(salePostId);

        return Response.success(response, SalePostSuccessCode.SALE_POST_RETRIEVED);
    }

    // TODO: 빈 배열 반환 문제 해결 필요
    @Override
    @GetMapping
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getSalePosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

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
    @PutMapping("/{salePostId}")    // TODO: Patch 고려
    public ResponseEntity<Response<SalePostUpdateResponse>> updateSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SalePostUpdateRequest request
    ) {
        SalePostUpdateResponse response = salePostCommandService.updateSalePost(
                salePostId,
                authUser.getUserId(),
                request
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_UPDATED);
    }

    @Override
    @DeleteMapping("/{salePostId}")
    public ResponseEntity<Response<Void>> deleteSalePost(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        salePostCommandService.deleteSalePost(salePostId, authUser.getUserId());

        return Response.success(null, SalePostSuccessCode.SALE_POST_DELETED);
    }

    @Override
    @PatchMapping("/{salePostId}/status")
    public ResponseEntity<Response<SalePostUpdateResponse>> updateSaleStatus(
            @PathVariable Long salePostId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SaleStatusUpdateRequest request
    ) {
        SalePostUpdateResponse response = salePostCommandService.updateSaleStatus(
                salePostId,
                authUser.getUserId(),
                request.getStatus()
        );

        return Response.success(response, SalePostSuccessCode.SALE_POST_STATUS_UPDATED);
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getMySalePosts(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostListResponse> response = salePostQueryService.findMySalePosts(
                authUser.getUserId(),
                status,
                pageable
        );

        return Response.success(response, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }

    // TODO: 빈 배열 반환 문제 해결 필요
    @Override
    @GetMapping("/public")
    public ResponseEntity<Response<Slice<SalePostListResponse>>> getNotAuthSalePosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        Slice<SalePostListResponse> salePosts = salePostQueryService.getNotAuthSalePostList(
                categoryId,
                status,
                keyword,
                pageable
        );

        return Response.success(salePosts, SalePostSuccessCode.SALE_POSTS_RETRIEVED);
    }
}