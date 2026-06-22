package sem4.edustreambe.dto.admin.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminAnalyticsOverviewResponse {
    BigDecimal totalRevenue;
    long totalStudents;
    long totalTutors;
    long pendingCourses;
}
