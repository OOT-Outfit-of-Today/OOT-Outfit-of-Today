package org.example.ootoutfitoftoday.domain.wearrecord.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.request.WearRecordCreateRequest;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordCreateResponse;
import org.example.ootoutfitoftoday.domain.wearrecord.dto.response.WearRecordGetMyResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "착용 기록 관리", description = "옷 착용 기록 및 이력 관련 API")
public interface WearRecordController {

    @Operation(
            summary = "착용 기록 등록",
            description = "사용자가 특정 옷을 착용했음을 기록하고, 해당 옷의 마지막 착용 일시를 업데이트합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 (clothesId 누락 등)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 옷에 기록 시도)"),
                    @ApiResponse(responseCode = "404", description = "옷을 찾을 수 없음 (ID 오류 또는 삭제된 옷)")
            }
    )
    ResponseEntity<Response<WearRecordCreateResponse>> createWearRecord(
            AuthUser authUser,
            WearRecordCreateRequest request
    );

    @Operation(
            summary = "내 착용 기록 리스트 조회",
            description = "로그인한 사용자의 전체 착용 기록을 최신순(wornAt DESC)으로 페이징하여 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    ResponseEntity<PageResponse<WearRecordGetMyResponse>> getMyWearRecords(
            AuthUser authUser,
            int page,
            int size,
            String sort,
            String direction
    );
}