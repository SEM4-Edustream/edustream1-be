package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.booking.response.EnrollmentResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.BookingMapper;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnrollmentService {

    UserRepository userRepository;
    EnrollmentRepository enrollmentRepository;
    BookingMapper bookingMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<EnrollmentResponse> getMyEnrollments() {
        User student = getCurrentUser();
        return enrollmentRepository.findByUserId(student.getId()).stream()
                .map(bookingMapper::toEnrollmentResponse)
                .toList();
    }

    /**
     * Create enrollments for all courses in a paid booking.
     * Uses REQUIRES_NEW so this transaction is fully independent from the caller's transaction.
     * This ensures the PAID status commit is not affected by any enrollment failure.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enrollAfterPayment(Booking booking) {
        log.info("=== enrollAfterPayment START for Booking: {} ===", booking.getId());
        
        if (booking.getItems() == null || booking.getItems().isEmpty()) {
            log.warn("Booking {} has NO items! Cannot enroll.", booking.getId());
            return;
        }

        log.info("Booking {} has {} item(s)", booking.getId(), booking.getItems().size());

        for (BookingItem item : booking.getItems()) {
            try {
                Course course = item.getCourse();
                UUID userId = booking.getUser().getId();
                String courseId = course.getId();

                log.info("Checking enrollment: userId={}, courseId={}", userId, courseId);

                if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
                    Enrollment enrollment = Enrollment.builder()
                            .user(booking.getUser())
                            .course(course)
                            .enrolledAt(LocalDateTime.now())
                            .progressPercentage(0)
                            .build();
                    enrollmentRepository.save(enrollment);
                    log.info("SUCCESS: Enrolled userId={} to courseId={} ({})", userId, courseId, course.getTitle());
                } else {
                    log.info("Already enrolled: userId={}, courseId={}", userId, courseId);
                }
            } catch (Exception e) {
                log.error("Failed to enroll for item in booking {}: {}", booking.getId(), e.getMessage(), e);
            }
        }

        log.info("=== enrollAfterPayment END for Booking: {} ===", booking.getId());
    }
}
