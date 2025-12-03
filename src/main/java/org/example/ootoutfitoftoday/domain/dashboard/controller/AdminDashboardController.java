package org.example.ootoutfitoftoday.domain.dashboard.controller;

import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "관리자 대시보드", description = "관리자가 확인하는 통계 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface AdminDashboardController {

    @Operation(
            summary = "대시보드 유저 통계자료 조회",
            description = "관리자는 누적 가입자 수, 활성/비활성 사용자 수 및 일간/ 주간/ 월간 신규 가입자를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<AdminUserStatisticsResponse>> adminUserStatistics(
            @Parameter(description = "기준 날짜 (기본값: 오늘)") LocalDate baseDate
    );

    @Operation(
            summary = "대시보드 옷 통계자료 조회",
            description = "관리자는 유저가 등록한 옷의 통계자료를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<AdminClothesStatisticsResponse>> adminClothesStatistics();

    @Operation(
            summary = "대시보드 판매글 통계자료 조회",
            description = "관리자는 등록된 판매글 수 및 거래현황을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<AdminSalePostStatisticsResponse>> adminSalePostStatistics(
            @Parameter(description = "기준 날짜 (기본값: 오늘)") LocalDate baseDate
    );

    @Operation(
            summary = "대시보드 카테고리 통계자료 조회",
            description = "관리자는 인기 카테고리를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<AdminTopCategoryStatisticsResponse>> adminTopCategoryStatistics();
}
