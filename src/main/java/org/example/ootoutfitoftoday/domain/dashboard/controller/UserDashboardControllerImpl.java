package org.example.ootoutfitoftoday.domain.dashboard.controller;

import com.ootcommon.dashboard.response.DashboardUserSummaryResponse;
import com.ootcommon.dashboard.response.DashboardUserWearStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.auth.dto.AuthUser;
import org.example.ootoutfitoftoday.domain.dashboard.exception.DashboardSuccessCode;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.user.DashboardUserQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/dashboards/users")
public class UserDashboardControllerImpl implements UserDashboardController {

    private final DashboardUserQueryService dashboardUserQueryService;

    @Override
    @GetMapping("/overview")
    public ResponseEntity<Response<DashboardUserSummaryResponse>> getUserDashboardSummary(
            AuthUser authUser
    ) {

        return Response.success(dashboardUserQueryService.getUserDashboardSummary(authUser.getUserId()), DashboardSuccessCode.DASHBOARD_USER_SUMMARY_OK);
    }

    @Override
    @GetMapping("/statistics")
    public ResponseEntity<Response<DashboardUserWearStatisticsResponse>> getUserWearStatistics(
            AuthUser authUser,
            LocalDate baseDate
    ) {

        return Response.success(dashboardUserQueryService.getUserWearStatistics(authUser.getUserId(), baseDate), DashboardSuccessCode.DASHBOARD_USER_STATISTICS_OK);
    }
}