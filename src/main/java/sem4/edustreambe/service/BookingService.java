package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.booking.request.BookingRequest;
import sem4.edustreambe.dto.booking.response.BookingResponse;
import sem4.edustreambe.entity.Booking;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.Enrollment;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.BookingStatus;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.BookingMapper;
import sem4.edustreambe.repository.BookingRepository;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.PaymentTransactionRepository;
import sem4.edustreambe.repository.UserRepository;
import sem4.edustreambe.entity.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {

    UserRepository userRepository;
    CourseRepository courseRepository;
    BookingRepository bookingRepository;
    EnrollmentRepository enrollmentRepository;
    PaymentTransactionRepository paymentTransactionRepository;
    BookingMapper bookingMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public BookingResponse createBooking(BookingRequest request) {
        User student = getCurrentUser();

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // 1. Validate if course is published
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        // 2. Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(student.getId(), course.getId())) {
            throw new AppException(ErrorCode.ALREADY_ENROLLED);
        }

        // 3. Check if an active booking already exists
        Optional<Booking> existingBooking = bookingRepository.findByUserIdAndCourseIdAndStatus(
                student.getId(), course.getId(), BookingStatus.PENDING);
        if (existingBooking.isPresent()) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }

        Booking booking = Booking.builder()
                .user(student)
                .course(course)
                .amount(course.getPrice())
                .status(BookingStatus.PENDING)
                .build();

        // 4. Auto-enrollment for free courses ($0 or null)
        if (course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            booking.setStatus(BookingStatus.PAID);
            
            Enrollment enrollment = Enrollment.builder()
                    .user(student)
                    .course(course)
                    .enrolledAt(LocalDateTime.now())
                    .progressPercentage(0)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(saved);
    }

    public List<BookingResponse> getMyBookings() {
        User student = getCurrentUser();
        return bookingRepository.findByUserId(student.getId()).stream()
                .map(bookingMapper::toBookingResponse)
                .toList();
    }

    public void deleteBooking(String bookingId) {
        User student = getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(student.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }

        Optional<PaymentTransaction> tx = paymentTransactionRepository.findByBookingId(bookingId);
        tx.ifPresent(paymentTransactionRepository::delete);

        bookingRepository.delete(booking);
    }
}
