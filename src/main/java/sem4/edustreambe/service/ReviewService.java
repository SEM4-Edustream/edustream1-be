package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.review.request.CourseReviewRequest;
import sem4.edustreambe.dto.review.response.CourseReviewResponse;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.CourseReview;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.CourseReviewRepository;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    CourseReviewRepository courseReviewRepository;
    CourseRepository courseRepository;
    EnrollmentRepository enrollmentRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public CourseReviewResponse createReview(String courseId, CourseReviewRequest request) {
        User currentUser = getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        // Chặn: Phải enroll mới được review
        boolean hasEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId);
        if (!hasEnrolled) {
            throw new AppException(ErrorCode.MUST_ENROLL_TO_REVIEW);
        }

        // Chặn: 1 người chỉ 1 review
        boolean hasReviewed = courseReviewRepository.existsByUserIdAndCourseId(currentUser.getId().toString(), courseId);
        if (hasReviewed) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        CourseReview review = CourseReview.builder()
                .user(currentUser)
                .course(course)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        CourseReview savedReview = courseReviewRepository.save(review);
        
        applyRatingMath(course, request.getRating());

        return mapToResponse(savedReview);
    }

    public Page<CourseReviewResponse> getCourseReviews(String courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courseReviewRepository.findByCourseId(courseId, pageable)
                .map(this::mapToResponse);
    }

    private void recalculateCourseRating(Course course) {
        long currentCount = course.getReviewCount() != null ? course.getReviewCount() : 0;
        float currentAverage = course.getAverageRating() != null ? course.getAverageRating() : 0.0f;
        
        // Lấy tất cả review để tính từ database (phòng hờ race condition)
        // Tuy nhiên do chưa có count từ repo, ta tạm tính math
        course.setReviewCount((int) currentCount + 1);
        
        // Tuy nhiên, để chính xác tuyệt đối, ta đếm lại:
        // Vì Hibernate chưa có hàm sum() ở CourseReviewRepository, ta có thể dùng công thức tịnh tiến:
        // rating mới = ((TrungBìnhCũ * SốLượngCũ) + ĐiểmMới) / SốLượngMới
        // Ở hàm createReview bên trên ta đã gọi save, nên ta truyền rating điểm mới từ entity!
    }

    public void applyRatingMath(Course course, int newRating) {
        long currentCount = course.getReviewCount() != null ? course.getReviewCount() : 0;
        float currentAverage = course.getAverageRating() != null ? course.getAverageRating() : 0.0f;

        float totalScore = (currentAverage * currentCount) + newRating;
        int newCount = (int) currentCount + 1;
        float newAverage = totalScore / newCount;

        course.setReviewCount(newCount);
        course.setAverageRating(newAverage);
        courseRepository.save(course);
    }

    private CourseReviewResponse mapToResponse(CourseReview review) {
        return CourseReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId().toString())
                .fullName(review.getUser().getFullName())
                .avatarUrl("https://ui-avatars.com/api/?name=" + review.getUser().getUsername()) // Default avatar
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
