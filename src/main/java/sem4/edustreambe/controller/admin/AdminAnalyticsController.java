package sem4.edustreambe.controller.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.admin.response.AdminAnalyticsOverviewResponse;
import sem4.edustreambe.dto.admin.response.AdminRevenueChartResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.AdminAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/overview")
    public ApiResponse<AdminAnalyticsOverviewResponse> getOverview() {
        return ApiResponse.<AdminAnalyticsOverviewResponse>builder()
                .result(adminAnalyticsService.getOverview())
                .build();
    }

    @GetMapping("/revenue-chart")
    public ApiResponse<List<AdminRevenueChartResponse>> getRevenueChart(
            @RequestParam(defaultValue = "30") int days) {
        return ApiResponse.<List<AdminRevenueChartResponse>>builder()
                .result(adminAnalyticsService.getRevenueChart(days))
                .build();
    }
}
