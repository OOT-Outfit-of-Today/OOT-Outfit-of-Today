package org.example.ootoutfitoftoday.domain.dashboard.controller;

import com.ootcommon.dashboard.response.DashboardUserSummaryResponse;
import com.ootcommon.dashboard.response.DashboardUserWearStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Tag(name = "사용자 대시보드", description = "사용자가 확인하는 통계 관련 API")
@SecurityRequirement(name = "bearerAuth")
public interface UserDashboardController {

    @Operation(
            summary = "대시보드 옷 분포 현황 조회",
            description = "사용자는 등록한 옷의 분포 현황을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    @GetMapping("/overview")
    ResponseEntity<Response<DashboardUserSummaryResponse>> getUserDashboardSummary(
            AuthUser authUser
    );

    @Operation(
            summary = "대시보드 옷의 착용 횟수 및 기간 통계 정보 조회",
            description = "사용자는 등록한 옷의 착용 횟수 및 기간 통계를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            })
    ResponseEntity<Response<DashboardUserWearStatisticsResponse>> getUserWearStatistics(
            AuthUser authUser,
            LocalDate baseDate
    );
}
