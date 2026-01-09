package org.example.ootoutfitoftoday.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.auth.dto.request.*;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthFieldAvailabilityResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.response.AuthLoginResponse;
import org.example.ootoutfitoftoday.domain.auth.dto.response.DeviceInfoResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "회원 관리", description = "회원 관련 API")
public interface AuthController {

    @Operation(
            summary = "회원 생성",
            description = "새로운 회원을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "409", description = "중복 요청")
            })
    ResponseEntity<Response<Void>> signup(
            AuthSignupRequest request
    );

    // 실시간 중복 체크 API
    @Operation(
            summary = "로그인 ID 중복 체크",
            description = "회원가입 시 로그인 ID의 중복 여부를 실시간으로 확인합니다.\n\n" +
                    "- 사용자가 아이디 입력 완료 시 호출\n" +
                    "- 즉각적인 중복 여부 피드백 제공으로 사용자 경험 개선\n" +
                    "- 최종 회원가입 시에도 서버에서 재검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공 (available: true/false)"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    ResponseEntity<Response<AuthFieldAvailabilityResponse>> checkLoginId(
            AuthLoginIdCheckRequest request
    );

    @Operation(
            summary = "이메일 중복 체크",
            description = "회원가입 시 이메일의 중복 여부를 실시간으로 확인합니다.\n\n" +
                    "- 이메일 입력 완료 시 호출\n" +
                    "- 즉각적인 중복 여부 피드백 제공으로 사용자 경험 개선\n" +
                    "- 최종 회원가입 시에도 서버에서 재검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공 (available: true/false)"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    ResponseEntity<Response<AuthFieldAvailabilityResponse>> checkEmail(
            AuthEmailCheckRequest request
    );

    @Operation(
            summary = "닉네임 중복 체크",
            description = "회원가입 시 닉네임의 중복 여부를 실시간으로 확인합니다.\n\n" +
                    "- 닉네임 입력 완료 시 호출\n" +
                    "- 즉각적인 중복 여부 피드백 제공으로 사용자 경험 개선\n" +
                    "- 최종 회원가입 시에도 서버에서 재검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공 (available: true/false)"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    ResponseEntity<Response<AuthFieldAvailabilityResponse>> checkNickname(
            AuthNicknameCheckRequest request
    );

    @Operation(
            summary = "전화번호 중복 체크",
            description = "회원가입 시 전화번호의 중복 여부를 실시간으로 확인합니다.\n\n" +
                    "- 전화번호 입력 완료 시 호출\n" +
                    "- 즉각적인 중복 여부 피드백 제공으로 사용자 경험 개선\n" +
                    "- 최종 회원가입 시에도 서버에서 재검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공 (available: true/false)"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            })
    ResponseEntity<Response<AuthFieldAvailabilityResponse>> checkPhoneNumber(
            AuthPhoneNumberCheckRequest request
    );

    @Operation(
            summary = "회원 로그인",
            description = "아이디와 비밀번호를 사용하여 로그인합니다.\n\n" +
                    "- Access Token: 응답 바디에 포함 (60분)\n" +
                    "- Refresh Token: 응답 바디에 포함 (7일)\n" +
                    "- Device ID: 클라이언트가 생성한 UUID 필수 전송",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 생성"),
                    @ApiResponse(responseCode = "401", description = "로그인 실패(잘못된 아이디 또는 비밀번호)")
            })
    ResponseEntity<Response<AuthLoginResponse>> login(
            AuthLoginRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(
            summary = "내 디바이스 목록",
            description = "현재 로그인된 모든 디바이스 목록을 조회합니다.\n\n" +
                    "- 디바이스 ID, 이름, 마지막 사용 시간 등 포함\n" +
                    "- 최근 사용 순으로 정렬",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<List<DeviceInfoResponse>>> getDevices(
            AuthUser authUser,
            String currentDeviceId
    );

    @Operation(
            summary = "토큰 재발급",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.(RTR)\n\n" +
                    "- 리프레시 토큰은 Body로 전송\n" +
                    "- Device ID도 함께 전송하여 디바이스 검증",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
                    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
            })
    ResponseEntity<Response<AuthLoginResponse>> refresh(
            RefreshTokenRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(
            summary = "OAuth2 임시 코드 교환",
            description = "OAuth2 로그인 후 발급된 임시 코드를 JWT 토큰으로 교환합니다.\n\n" +
                    "- 임시 코드는 3분간 유효\n" +
                    "- 1회용(사용 후 자동 삭제)\n" +
                    "- Redis에서 토큰 정보 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토큰 교환 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않거나 만료된 코드")
            })
    ResponseEntity<Response<AuthLoginResponse>> exchangeOAuthToken(
            TokenExchangeRequest request,
            HttpServletRequest httpRequest
    );

    @Operation(
            summary = "로그아웃",
            description = "특정 디바이스에서 로그아웃하고 리프레시 토큰을 무효화합니다.\n\n" +
                    "- DB에서 해당 디바이스의 리프레시 토큰 삭제\n" +
                    "- 다른 디바이스는 계속 로그인 상태 유지",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<Void>> logout(
            AuthUser authUser,
            String deviceId
    );

    @Operation(
            summary = "전체 로그아웃",
            description = "모든 디바이스에서 로그아웃합니다.\n\n" +
                    "- 모든 디바이스의 리프레시 토큰 삭제\n" +
                    "- 보안 위협 발생 시 사용",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "전체 로그아웃 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<Void>> logoutAll(
            AuthUser authUser
    );

    @Operation(
            summary = "디바이스 제거",
            description = "다른 디바이스를 원격으로 로그아웃합니다.\n\n" +
                    "- 현재 사용 중인 디바이스는 제거 불가 (로그아웃 사용)\n" +
                    "- 분실한 디바이스 또는 의심스러운 디바이스 제거\n" +
                    "- 해당 디바이스의 리프레시 토큰 삭제",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "디바이스 제거 성공"),
                    @ApiResponse(responseCode = "400", description = "현재 디바이스는 제거 불가"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "디바이스를 찾을 수 없음")
            })
    ResponseEntity<Response<Void>> removeDevice(
            AuthUser authUser,
            String deviceId,
            String currentDeviceId
    );

    @Operation(
            summary = "회원 삭제",
            description = "특정 회원을 삭제합니다.\n\n" +
                    "- 일반 로그인 사용자: 비밀번호 검증 필수\n" +
                    "- 소셜 로그인 사용자: 비밀번호 검증 통과",
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 회원"),
            })
    ResponseEntity<Response<Void>> withdraw(
            AuthWithdrawRequest request,
            AuthUser authUser
    );
}