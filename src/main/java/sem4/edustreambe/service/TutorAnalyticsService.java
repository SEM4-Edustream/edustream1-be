package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.common.PageMeta;
import sem4.edustreambe.dto.tutor.response.TutorAnalyticsResponse;
import sem4.edustreambe.dto.tutor.response.TutorStudentResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorAnalyticsService {

    UserRepository userRepository;
    TutorProfileRepository tutorProfileRepository;
    EnrollmentRepository enrollmentRepository;
    BookingItemRepository bookingItemRepository;
    CourseReviewRepository courseReviewRepository;
    CourseRepository courseRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private TutorProfile getCurrentTutorProfile() {
        User user = getCurrentUser();
        return tutorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.TUTOR_PROFILE_NOT_FOUND));
    }

    public TutorAnalyticsResponse getTutorAnalytics() {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        String tutorId = tutorProfile.getId();

        // 1. Basic Stats
        long totalStudents = enrollmentRepository.countUniqueStudentsByTutor(tutorId);
        Double averageProgress = enrollmentRepository.getAverageProgressByTutor(tutorId);
        Double averageRating = courseReviewRepository.getAverageRatingByTutor(tutorId);

        // 2. Revenue Comparison
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayThisMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime firstDayLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastDayLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        BigDecimal revenueThisMonth = bookingItemRepository.sumRevenueByTutorAndDateRange(tutorId, firstDayThisMonth, now);
        BigDecimal revenueLastMonth = bookingItemRepository.sumRevenueByTutorAndDateRange(tutorId, firstDayLastMonth, lastDayLastMonth);
        BigDecimal totalLifetimeRevenue = bookingItemRepository.sumTotalRevenueByTutor(tutorId);

        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;
        if (totalLifetimeRevenue == null) totalLifetimeRevenue = BigDecimal.ZERO;

        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = revenueThisMonth.subtract(revenueLastMonth)
                    .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 3. Top 3 Courses
        List<Course> myCourses = courseRepository.findByTutorProfileId(tutorId);
        List<TutorAnalyticsResponse.CourseStat> topCourses = myCourses.stream()
                .map(c -> {
                    long enrollmentCount = enrollmentRepository.findByCourseId(c.getId()).size();
                    Double avgRating = courseReviewRepository.findByCourseId(c.getId(), PageRequest.of(0, 100))
                            .getContent().stream().mapToDouble(CourseReview::getRating).average().orElse(0.0);
                    return TutorAnalyticsResponse.CourseStat.builder()
                            .courseId(c.getId())
                            .title(c.getTitle())
                            .enrollmentCount(enrollmentCount)
                            .averageRating(avgRating)
                            .build();
                })
                .sorted(Comparator.comparingLong(TutorAnalyticsResponse.CourseStat::getEnrollmentCount).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 4. Recent Activities
        List<TutorAnalyticsResponse.ActivityLog> activities = new ArrayList<>();
        
        enrollmentRepository.findTop5ByCourseTutorProfileIdOrderByEnrolledAtDesc(tutorId).forEach(e -> 
            activities.add(TutorAnalyticsResponse.ActivityLog.builder()
                    .type("ENROLLMENT")
                    .studentName(e.getUser().getFullName())
                    .courseTitle(e.getCourse().getTitle())
                    .detail("Đã đăng ký khóa học")
                    .timestamp(e.getEnrolledAt())
                    .build())
        );

        courseReviewRepository.findTop5ByCourseTutorProfileIdOrderByCreatedAtDesc(tutorId).forEach(r -> 
            activities.add(TutorAnalyticsResponse.ActivityLog.builder()
                    .type("REVIEW")
                    .studentName(r.getUser().getFullName())
                    .courseTitle(r.getCourse().getTitle())
                    .detail("Đã đánh giá " + r.getRating() + " sao")
                    .timestamp(r.getCreatedAt())
                    .build())
        );

        activities.sort(Comparator.comparing(TutorAnalyticsResponse.ActivityLog::getTimestamp).reversed());
        List<TutorAnalyticsResponse.ActivityLog> recentActivities = activities.stream().limit(5).toList();

        // 5. Revenue by Course
        List<Object[]> revenueData = bookingItemRepository.getRevenueByCourse(tutorId);
        List<TutorAnalyticsResponse.CourseRevenue> revenueByCourse = revenueData.stream()
                .map(row -> TutorAnalyticsResponse.CourseRevenue.builder()
                        .courseId((String) row[0])
                        .title((String) row[1])
                        .totalRevenue((BigDecimal) row[2])
                        .totalSales((long) row[3])
                        .build())
                .sorted(Comparator.comparing(TutorAnalyticsResponse.CourseRevenue::getTotalRevenue).reversed())
                .toList();

        // 6. Chart Data (Last 12 months)
        LocalDateTime twelveMonthsAgo = now.minusMonths(11).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        List<Object[]> monthlyRevenueData = bookingItemRepository.getMonthlyRevenue(tutorId, twelveMonthsAgo);
        List<TutorAnalyticsResponse.ChartData> chartData = monthlyRevenueData.stream()
                .map(row -> TutorAnalyticsResponse.ChartData.builder()
                        .year(((Number) row[0]).intValue())
                        .month(((Number) row[1]).intValue())
                        .revenue((BigDecimal) row[2])
                        .build())
                .toList();

        // 7. Daily Chart Data (Last 30 days)
        LocalDateTime thirtyDaysAgo = now.minusDays(29).with(LocalTime.MIN);
        List<Object[]> dailyRevenueData = bookingItemRepository.getDailyRevenue(tutorId, thirtyDaysAgo);
        List<TutorAnalyticsResponse.DailyChartData> dailyChartData = dailyRevenueData.stream()
                .map(row -> {
                    java.time.LocalDate date;
                    if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else {
                        date = (java.time.LocalDate) row[0];
                    }
                    return TutorAnalyticsResponse.DailyChartData.builder()
                        .date(date)
                        .revenue((BigDecimal) row[1])
                        .build();
                })
                .toList();

        return TutorAnalyticsResponse.builder()
                .totalStudents(totalStudents)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .revenueGrowth(revenueGrowth)
                .totalLifetimeRevenue(totalLifetimeRevenue)
                .averageProgress(averageProgress != null ? averageProgress : 0.0)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .topCourses(topCourses)
                .recentActivities(recentActivities)
                .revenueByCourse(revenueByCourse)
                .chartData(chartData)
                .dailyChartData(dailyChartData)
                .build();
    }

    public PageMeta<TutorStudentResponse> getTutorStudents(String courseId, org.springframework.data.domain.Pageable pageable) {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        String tutorId = tutorProfile.getId();

        org.springframework.data.domain.Page<Enrollment> enrollmentPage = 
                enrollmentRepository.findStudentsByTutorAndCourse(tutorId, courseId, pageable);

        List<TutorStudentResponse> students = enrollmentPage.getContent().stream()
                .map(e -> TutorStudentResponse.builder()
                        .enrollmentId(e.getId())
                        .studentId(e.getUser().getId().toString())
                        .fullName(e.getUser().getFullName())
                        .email(e.getUser().getEmail())
                        .avatarUrl(e.getUser().getAvatarUrl())
                        .courseId(e.getCourse().getId())
                        .courseTitle(e.getCourse().getTitle())
                        .progressPercentage(e.getProgressPercentage())
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();

        return PageMeta.<TutorStudentResponse>builder()
                .content(students)
                .pageSize(enrollmentPage.getSize())
                .totalElements(enrollmentPage.getTotalElements())
                .totalPages(enrollmentPage.getTotalPages())
                .pageNumber(enrollmentPage.getNumber())
                .build();
    }
}
