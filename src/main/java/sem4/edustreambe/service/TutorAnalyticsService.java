package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.tutor.response.TutorAnalyticsResponse;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.TutorProfile;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

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

        // 1. Total unique students
        long totalStudents = enrollmentRepository.countUniqueStudentsByTutor(tutorId);

        // 2. Revenue (This month vs Last month)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayThisMonth = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime firstDayLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        LocalDateTime lastDayLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);

        BigDecimal revenueThisMonth = bookingItemRepository.sumRevenueByTutorAndDateRange(tutorId, firstDayThisMonth, now);
        BigDecimal revenueLastMonth = bookingItemRepository.sumRevenueByTutorAndDateRange(tutorId, firstDayLastMonth, lastDayLastMonth);

        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;

        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = revenueThisMonth.subtract(revenueLastMonth)
                    .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 3. Top Course
        List<Course> courses = courseRepository.findByTutorProfileId(tutorId);
        String topCourseName = "N/A";
        long topCourseEnrollments = 0;
        
        if (!courses.isEmpty()) {
            Course topCourse = null;
            long maxEnrollments = -1;
            
            for (Course course : courses) {
                long enrollmentCount = enrollmentRepository.findByCourseId(course.getId()).size();
                if (enrollmentCount > maxEnrollments) {
                    maxEnrollments = enrollmentCount;
                    topCourse = course;
                }
            }
            
            if (topCourse != null) {
                topCourseName = topCourse.getTitle();
                topCourseEnrollments = maxEnrollments;
            }
        }

        // 4. Average Progress
        Double averageProgress = enrollmentRepository.getAverageProgressByTutor(tutorId);

        // 5. Average Rating
        Double averageRating = courseReviewRepository.getAverageRatingByTutor(tutorId);

        return TutorAnalyticsResponse.builder()
                .totalStudents(totalStudents)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .revenueGrowth(revenueGrowth)
                .topCourseName(topCourseName)
                .topCourseEnrollments(topCourseEnrollments)
                .averageProgress(averageProgress != null ? averageProgress : 0.0)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .build();
    }

    public sem4.edustreambe.dto.common.PageMeta<TutorStudentResponse> getTutorStudents(String courseId, org.springframework.data.domain.Pageable pageable) {
        TutorProfile tutorProfile = getCurrentTutorProfile();
        String tutorId = tutorProfile.getId();

        org.springframework.data.domain.Page<sem4.edustreambe.entity.Enrollment> enrollmentPage = 
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

        return sem4.edustreambe.dto.common.PageMeta.<TutorStudentResponse>builder()
                .content(students)
                .pageSize(enrollmentPage.getSize())
                .totalElements(enrollmentPage.getTotalElements())
                .totalPages(enrollmentPage.getTotalPages())
                .number(enrollmentPage.getNumber())
                .first(enrollmentPage.isFirst())
                .last(enrollmentPage.isLast())
                .build();
    }
}
