package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorAnalyticsResponse {
    long totalStudents;
    BigDecimal revenueThisMonth;
    BigDecimal revenueLastMonth;
    BigDecimal revenueGrowth; // Percentage
    double averageProgress;
    double averageRating;
    BigDecimal totalLifetimeRevenue;
    List<CourseStat> topCourses;
    List<ActivityLog> recentActivities;
    List<CourseRevenue> revenueByCourse;
    List<ChartData> chartData;
    List<DailyChartData> dailyChartData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        int month;
        int year;
        BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyChartData {
        java.time.LocalDate date;
        BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStat {
        String courseId;
        String title;
        long enrollmentCount;
        double averageRating;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        String type; // ENROLLMENT, REVIEW
        String studentName;
        String courseTitle;
        String detail; // "Rated 5 stars", "Enrolled", etc.
        java.time.LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseRevenue {
        String courseId;
        String title;
        BigDecimal totalRevenue;
        long totalSales;
    }
}
