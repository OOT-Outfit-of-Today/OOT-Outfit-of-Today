package org.example.ootoutfitoftoday.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserPasswordVerificationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateInfoRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateProfileImageRequest;
import org.example.ootoutfitoftoday.domain.user.dto.request.UserUpdateTradeLocationRequest;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserGetMyInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateInfoResponse;
import org.example.ootoutfitoftoday.domain.user.dto.response.UserUpdateProfileImageResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "회원 관리", description = "회원 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface UserController {

    @Operation(
            summary = "내 정보 조회",
            description = "토큰을 기반으로 회원 자신의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
            })
    ResponseEntity<Response<UserGetMyInfoResponse>> getMyInfo(AuthUser authUser);

    @Operation(
            summary = "회원정보 수정 전 비밀번호 검증",
            description = """
                    회원정보 수정 전 비밀번호를 검증합니다.
                    
                    - 일반 로그인 사용자: 비밀번호 검증 필수
                    - 소셜 로그인 사용자: 비밀번호 검증 통과
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    ResponseEntity<Response<Void>> verifyPassword(
            UserPasswordVerificationRequest request,
            AuthUser authUser
    );

    @Operation(
            summary = "회원 정보 수정",
            description = "기존 회원의 정보를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
            })
    ResponseEntity<Response<UserUpdateInfoResponse>> updateInfo(
            UserUpdateInfoRequest request,
            AuthUser authUser
    );

    @Operation(
            summary = "프로필 이미지 수정",
            description = "회원의 프로필 이미지를 업데이트합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음"),
            })
    ResponseEntity<Response<UserUpdateProfileImageResponse>> updateProfileImage(
            UserUpdateProfileImageRequest request,
            AuthUser authUser
    );

    @Operation(
            summary = "프로필 이미지 삭제",
            description = "회원의 프로필 이미지를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "찾을 수 없음"),
            })
    ResponseEntity<Response<Void>> deleteProfileImage(
            AuthUser authUser
    );

    @Operation(
            summary = "회원 위치 수정",
            description = "회원의 위치(주소지, 위도, 경도)를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")    // TODO: 추가할 것
            })
    ResponseEntity<Response<Void>> updateUserTradeLocation(
            UserUpdateTradeLocationRequest request,
            AuthUser authUser
    );
}