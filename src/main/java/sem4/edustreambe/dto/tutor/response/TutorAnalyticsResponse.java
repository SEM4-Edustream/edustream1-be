package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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
    String topCourseName;
    long topCourseEnrollments;
    double averageProgress;
    double averageRating;
}
