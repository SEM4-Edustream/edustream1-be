package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.admin.response.AdminAnalyticsOverviewResponse;
import sem4.edustreambe.dto.admin.response.AdminRevenueChartResponse;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.PaymentTransactionRepository;
import sem4.edustreambe.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminAnalyticsService {

    UserRepository userRepository;
    CourseRepository courseRepository;
    PaymentTransactionRepository paymentTransactionRepository;

    @Transactional(readOnly = true)
    public AdminAnalyticsOverviewResponse getOverview() {
        BigDecimal totalRevenue = paymentTransactionRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalStudents = userRepository.countByRoleName("STUDENT");
        long totalTutors = userRepository.countByRoleName("TUTOR");
        long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING);

        return AdminAnalyticsOverviewResponse.builder()
                .totalRevenue(totalRevenue)
                .totalStudents(totalStudents)
                .totalTutors(totalTutors)
                .pendingCourses(pendingCourses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminRevenueChartResponse> getRevenueChart(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return paymentTransactionRepository.getRevenueByDateRange(startDate);
    }
}
