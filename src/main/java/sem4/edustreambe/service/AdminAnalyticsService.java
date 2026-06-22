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
    sem4.edustreambe.repository.EnrollmentRepository enrollmentRepository;

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

    @Transactional(readOnly = true)
    public sem4.edustreambe.dto.common.PageMeta<sem4.edustreambe.dto.admin.response.AdminEnrollmentDetailResponse> getEnrollmentDetails(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<sem4.edustreambe.entity.Enrollment> enrollmentPage = enrollmentRepository.findAllEnrollmentDetails(pageable);

        List<sem4.edustreambe.dto.admin.response.AdminEnrollmentDetailResponse> responses = enrollmentPage.getContent().stream()
                .map(e -> sem4.edustreambe.dto.admin.response.AdminEnrollmentDetailResponse.builder()
                        .studentName(e.getUser().getUsername())
                        .studentEmail(e.getUser().getEmail())
                        .courseTitle(e.getCourse().getTitle())
                        .tutorName(e.getCourse().getTutorProfile() != null && e.getCourse().getTutorProfile().getUser() != null 
                                ? e.getCourse().getTutorProfile().getUser().getUsername() : "UNKNOWN")
                        .progressPercentage(e.getProgressPercentage())
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();

        return sem4.edustreambe.dto.common.PageMeta.<sem4.edustreambe.dto.admin.response.AdminEnrollmentDetailResponse>builder()
                .content(responses)
                .pageNumber(enrollmentPage.getNumber())
                .pageSize(enrollmentPage.getSize())
                .totalElements(enrollmentPage.getTotalElements())
                .totalPages(enrollmentPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public sem4.edustreambe.dto.common.PageMeta<sem4.edustreambe.dto.admin.response.AdminCourseMetricResponse> getCourseMetrics(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<sem4.edustreambe.dto.admin.response.AdminCourseMetricResponse> metricPage = enrollmentRepository.getCourseMetrics(pageable);

        return sem4.edustreambe.dto.common.PageMeta.<sem4.edustreambe.dto.admin.response.AdminCourseMetricResponse>builder()
                .content(metricPage.getContent())
                .pageNumber(metricPage.getNumber())
                .pageSize(metricPage.getSize())
                .totalElements(metricPage.getTotalElements())
                .totalPages(metricPage.getTotalPages())
                .build();
    }
}
