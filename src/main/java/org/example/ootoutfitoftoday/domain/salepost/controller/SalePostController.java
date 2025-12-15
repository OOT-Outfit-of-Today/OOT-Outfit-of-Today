package org.example.ootoutfitoftoday.domain.salepost.controller;

import com.ootcommon.salepost.enums.SaleStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostCreateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SalePostUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.request.SaleStatusUpdateRequest;
import org.example.ootoutfitoftoday.domain.salepost.dto.response.*;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "판매글 관리", description = "판매글 관련 API")
public interface SalePostController {

    @Operation(
            summary = "판매글 생성",
            description = "새로운 판매글을 등록합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<SalePostCreateResponse>> createSalePost(
            AuthUser authUser,
            SalePostCreateRequest request
    );

    @Operation(
            summary = "판매글 상세 조회",
            description = "판매글의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<SalePostDetailResponse>> getSalePostDetail(
            Long salePostId
    );

    @Operation(
            summary = "판매글 전체 조회",
            description = "카테고리/상태/키워드로 필터링 된 전체 판매글을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<Response<Slice<SalePostListResponse>>> getSalePosts(
            @Parameter(description = "카테고리 ID") Long categoryId,
            @Parameter(description = "판매 상태 (SELLING, RESERVED, SOLD_OUT)") SaleStatus status,
            @Parameter(description = "검색어 (제목/내용 검색)") String keyword,
            @Parameter(description = "해당 페이지") int page,
            @Parameter(description = "한 페이지 판매글 수") int size,
            @Parameter(description = "정렬 기준") String sort,
            @Parameter(description = "정렬 순서") Sort.Direction direction,
            @AuthenticationPrincipal AuthUser authUser
    );

    @Operation(
            summary = "판매글 수정",
            description = "기존 판매글을 수정합니다.(이미지 제외)",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<SalePostDetailResponse>> updateSalePost(
            Long salePostId,
            AuthUser authUser,
            SalePostUpdateRequest request
    );

    @Operation(
            summary = "판매글 삭제",
            description = "판매글을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<Void>> deleteSalePost(
            Long salePostId,
            AuthUser authUser
    );

    @Operation(
            summary = "판매글 상태 변경",
            description = "판매글의 판매 상태를 변경합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "변경 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<SalePostDetailResponse>> updateSaleStatus(
            Long salePostId,
            AuthUser authUser,
            SaleStatusUpdateRequest request
    );

    @Operation(
            summary = "내 판매글 조회",
            description = "내가 작성한 판매글들을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "요청 리소스를 찾을 수 없음")
            }
    )
    ResponseEntity<Response<Slice<SalePostListResponse>>> getMySalePosts(
            AuthUser authUser,
            @Parameter(description = "판매 상태 (SELLING, RESERVED, SOLD_OUT)") SaleStatus status,
            int page,
            int size,
            String sort,
            Sort.Direction direction
    );

    @Operation(
            summary = "비회원 판매글 전체 조회",
            description = "카테고리/상태/키워드로 필터링 된 전체 판매글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    ResponseEntity<Response<Slice<SalePostListResponse>>> getNotAuthSalePosts(
            @Parameter(description = "카테고리 ID") Long categoryId,
            @Parameter(description = "판매 상태 (SELLING, RESERVED, SOLD_OUT)") SaleStatus status,
            @Parameter(description = "검색어 (제목/내용 검색)") String keyword,
            int page,
            int size,
            String sort,
            Sort.Direction direction
    );
}