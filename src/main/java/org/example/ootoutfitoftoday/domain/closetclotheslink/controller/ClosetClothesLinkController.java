package org.example.ootoutfitoftoday.domain.closetclotheslink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.PageResponse;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.request.ClosetClothesLinkRequest;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkDeleteResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkGetResponse;
import org.example.ootoutfitoftoday.domain.closetclotheslink.dto.response.ClosetClothesLinkResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "옷장-옷 관리", description = "옷장에 옷을 등록, 조회, 삭제하는 API")
@SecurityRequirement(name = "bearerAuth")
public interface ClosetClothesLinkController {

    @Operation(
            summary = "옷장에 옷 등록",
            description = "선택한 옷장에 이미 등록된 옷을 추가합니다. 같은 옷을 중복으로 등록할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "등록 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 등록된 옷 또는 잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장 또는 옷을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<ClosetClothesLinkResponse>> createClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            ClosetClothesLinkRequest closetClothesLinkRequest
    );

    @Operation(
            summary = "옷장에 등록된 옷 리스트 조회",
            description = "선택한 옷장에 등록된 모든 옷을 조회합니다. 최근 등록순으로 정렬됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    ResponseEntity<PageResponse<ClosetClothesLinkGetResponse>> getClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            int page,
            int size,
            String sort,
            String direction
    );

    @Operation(
            summary = "옷장에서 옷 제거",
            description = "선택한 옷장에 등록된 옷을 제거합니다. 실제 옷 데이터는 삭제되지 않고 연결만 해제됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "연결되지 않은 옷"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "해당 옷장에 대한 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "옷장을 찾을 수 없음")
            }
    )
    ResponseEntity<Response<ClosetClothesLinkDeleteResponse>> deleteClosetClothesLink(
            AuthUser authUser,
            Long closetId,
            Long clothesId
    );
}