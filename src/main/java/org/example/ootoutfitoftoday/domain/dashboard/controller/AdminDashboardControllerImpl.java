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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/v1/dashboards")
public class AdminDashboardControllerImpl implements AdminDashboardController {

    private final DashboardAdminQueryService dashboardAdminQueryService;

    @Override
    @GetMapping("/users/statistics")
    public ResponseEntity<Response<AdminUserStatisticsResponse>> adminUserStatistics(@RequestParam(required = false) LocalDate baseDate) {
        AdminUserStatisticsResponse response = dashboardAdminQueryService.adminUserStatistics(baseDate);

        return Response.success(response, DashboardSuccessCode.DASHBOARD_ADMIN_USER_STATISTICS_OK);
    }

    @Override
    @GetMapping("/clothes/statistics")
    public ResponseEntity<Response<AdminClothesStatisticsResponse>> adminClothesStatistics() {
        AdminClothesStatisticsResponse response = dashboardAdminQueryService.adminClothesStatistics();

        return Response.success(response, DashboardSuccessCode.DASHBOARD_ADMIN_CLOTHES_STATISTICS_OK);
    }

    @Override
    @GetMapping("/sale-posts/statistics")
    public ResponseEntity<Response<AdminSalePostStatisticsResponse>> adminSalePostStatistics(@RequestParam(required = false) LocalDate baseDate) {
        AdminSalePostStatisticsResponse response = dashboardAdminQueryService.adminSalePostStatistics(baseDate);

        return Response.success(response, DashboardSuccessCode.DASHBOARD_ADMIN_SALE_POST_STATISTICS_OK);
    }

    @Override
    @GetMapping("/popular")
    public ResponseEntity<Response<AdminTopCategoryStatisticsResponse>> adminTopCategoryStatistics() {
        AdminTopCategoryStatisticsResponse response = dashboardAdminQueryService.adminTopCategoryStatistics();

        return Response.success(response, DashboardSuccessCode.DASHBOARD_ADMIN_TOP10_CATEGORY_STATISTICS_OK);
    }
}