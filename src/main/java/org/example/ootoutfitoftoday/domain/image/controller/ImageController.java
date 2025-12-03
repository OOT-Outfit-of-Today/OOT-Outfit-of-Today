package org.example.ootoutfitoftoday.domain.image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.image.dto.request.ImageSaveRequest;
import org.example.ootoutfitoftoday.domain.image.dto.request.PresignedUrlRequest;
import org.example.ootoutfitoftoday.domain.image.dto.response.ImageSaveResponse;
import org.example.ootoutfitoftoday.domain.image.dto.response.PresignedUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "이미지 관리", description = "S3 이미지 업로드 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface ImageController {

    @Operation(
            summary = "Presigned URL 생성",
            description = "S3에 이미지 업로드를 위한 Presigned URL을 생성합니다. 생성된 URL로 5분 이내에 이미지를 업로드할 수 있습니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 파일명 또는 파일 형식"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "500", description = "Presigned URL 생성 실패")
            }
    )
    ResponseEntity<Response<PresignedUrlResponse>> generatePresignedUrl(
            AuthUser authUser,
            PresignedUrlRequest request
    );

    @Operation(
            summary = "이미지 메타데이터 저장",
            description = "S3에 업로드된 이미지의 메타데이터를 DB에 저장합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "저장 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "이미지가 이미 존재함")
            }
    )
    ResponseEntity<Response<ImageSaveResponse>> saveImage(
            @Valid @RequestBody ImageSaveRequest request
    );
}