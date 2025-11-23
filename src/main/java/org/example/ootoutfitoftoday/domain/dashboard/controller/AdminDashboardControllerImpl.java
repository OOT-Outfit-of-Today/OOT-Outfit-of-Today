package org.example.ootoutfitoftoday.domain.dashboard.controller;

import com.ootcommon.dashboard.response.AdminClothesStatisticsResponse;
import com.ootcommon.dashboard.response.AdminSalePostStatisticsResponse;
import com.ootcommon.dashboard.response.AdminTopCategoryStatisticsResponse;
import com.ootcommon.dashboard.response.AdminUserStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.example.ootoutfitoftoday.common.response.Response;
import org.example.ootoutfitoftoday.domain.dashboard.exception.DashboardSuccessCode;
import org.example.ootoutfitoftoday.domain.dashboard.service.query.admin.DashboardAdminQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/dashboards")
public class AdminDashboardControllerImpl implements AdminDashboardController {

    private final DashboardAdminQueryService dashboardAdminQueryService;

    @Override
    @GetMapping("/users/statistics")
    public ResponseEntity<Response<AdminUserStatisticsResponse>> adminUserStatistics(
            LocalDate baseDate
    ) {

        return Response.success(dashboardAdminQueryService.adminUserStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_USER_STATISTICS_OK);
    }

    @Override
    @GetMapping("/clothes/statistics")
    public ResponseEntity<Response<AdminClothesStatisticsResponse>> adminClothesStatistics() {

        return Response.success(dashboardAdminQueryService.adminClothesStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_CLOTHES_STATISTICS_OK);
    }

    @Override
    @GetMapping("/sale-posts/statistics")
    public ResponseEntity<Response<AdminSalePostStatisticsResponse>> adminSalePostStatistics(
            LocalDate baseDate
    ) {

        return Response.success(dashboardAdminQueryService.adminSalePostStatistics(baseDate), DashboardSuccessCode.DASHBOARD_ADMIN_SALE_POST_STATISTICS_OK);
    }

    @Override
    @GetMapping("/popular")
    public ResponseEntity<Response<AdminTopCategoryStatisticsResponse>> adminTopCategoryStatistics() {

        return Response.success(dashboardAdminQueryService.adminTopCategoryStatistics(), DashboardSuccessCode.DASHBOARD_ADMIN_TOP10_CATEGORY_STATISTICS_OK);
    }
}