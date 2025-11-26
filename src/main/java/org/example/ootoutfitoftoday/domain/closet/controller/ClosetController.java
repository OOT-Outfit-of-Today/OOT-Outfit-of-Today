package org.example.ootoutfitoftoday.domain.closet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closet.dto.request.ClosetRequest;
import org.example.ootoutfitoftoday.domain.closet.dto.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "옷장 관리", description = "옷장관련 API")
public interface ClosetController {

    @Operation(
            summary = "옷장 등록",
            description = "회원이 자신의 옷장을 등록합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<Response<ClosetCreateResponse>> createCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ClosetRequest closetRequest
    );

    @Operation(
            summary = "내 옷장 전체 조회",
            description = "회원이 자신의 전체 옷장을 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<PageResponse<ClosetGetResponse>> getMyClosets(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    );

    @Operation(
            summary = "공개 옷장 전체 조회",
            description = "공개 옷장 전체를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    ResponseEntity<PageResponse<ClosetGetResponse>> getPublicClosets(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction
    );

    @Operation(
            summary = "옷장 상세 조회",
            description = "회원이 옷장의 상세 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<ClosetGetResponse>> getCloset(
            @PathVariable Long closetId
    );

    @Operation(
            summary = "내 옷장 정보 수정",
            description = "회원이 자신의 옷장 정보를 수정합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<ClosetUpdateResponse>> updateCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId,
            @Valid @RequestBody ClosetRequest closetRequest
    );

    @Operation(
            summary = "내 옷장 삭제",
            description = "회원이 자신의 옷장을 삭제합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<ClosetDeleteResponse>> deleteCloset(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long closetId
    );
}