package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.booking.request.BookingRequest;
import sem4.edustreambe.dto.booking.response.BookingItemResponse;
import sem4.edustreambe.dto.booking.response.BookingResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.enums.BookingStatus;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.BookingMapper;
import sem4.edustreambe.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    CartItemRepository cartItemRepository;
    BookingMapper bookingMapper;
    CartService cartService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    /**
     * Create a booking for a single course (legacy support / direct buy).
     */
    public BookingResponse createBooking(BookingRequest request) {
        User student = getCurrentUser();

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        if (enrollmentRepository.existsByUserIdAndCourseId(student.getId(), course.getId())) {
            throw new AppException(ErrorCode.ALREADY_ENROLLED);
        }

        Optional<Booking> existingBooking = bookingRepository.findByUserIdAndCourseIdAndStatus(
                student.getId(), course.getId(), BookingStatus.PENDING);
        if (existingBooking.isPresent()) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_EXISTS);
        }

        BigDecimal totalAmount = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

        Booking booking = Booking.builder()
                .user(student)
                .amount(totalAmount)
                .status(BookingStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        BookingItem bookingItem = BookingItem.builder()
                .booking(booking)
                .course(course)
                .price(totalAmount)
                .build();
        booking.getItems().add(bookingItem);

        // Auto-enrollment for free courses
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
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
        return toBookingResponse(saved);
    }

    /**
     * Create a booking from the user's entire cart (multi-course checkout).
     */
    public BookingResponse createBookingFromCart() {
        User student = getCurrentUser();

        List<CartItem> cartItems = cartItemRepository.findByUserId(student.getId());
        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> bookingItems = new ArrayList<>();

        Booking booking = Booking.builder()
                .user(student)
                .status(BookingStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        for (CartItem cartItem : cartItems) {
            Course course = cartItem.getCourse();

            if (course.getStatus() != CourseStatus.PUBLISHED) continue;

            if (enrollmentRepository.existsByUserIdAndCourseId(student.getId(), course.getId())) continue;

            BigDecimal price = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(price);

            BookingItem bookingItem = BookingItem.builder()
                    .booking(booking)
                    .course(course)
                    .price(price)
                    .build();
            bookingItems.add(bookingItem);
        }

        if (bookingItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        booking.setAmount(totalAmount);
        booking.setItems(bookingItems);

        // Auto-enrollment for all-free carts
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            booking.setStatus(BookingStatus.PAID);
            for (BookingItem item : bookingItems) {
                if (!enrollmentRepository.existsByUserIdAndCourseId(student.getId(), item.getCourse().getId())) {
                    Enrollment enrollment = Enrollment.builder()
                            .user(student)
                            .course(item.getCourse())
                            .enrolledAt(LocalDateTime.now())
                            .progressPercentage(0)
                            .build();
                    enrollmentRepository.save(enrollment);
                }
            }
        }

        Booking saved = bookingRepository.save(booking);

        // Clear the cart after successful booking
        cartService.clearCart(student);

        return toBookingResponse(saved);
    }

    public List<BookingResponse> getMyBookings() {
        User student = getCurrentUser();
        return bookingRepository.findByUserId(student.getId()).stream()
                .map(this::toBookingResponse)
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

    private BookingResponse toBookingResponse(Booking booking) {
        List<BookingItemResponse> itemResponses = booking.getItems() != null
                ? booking.getItems().stream()
                    .map(item -> BookingItemResponse.builder()
                            .id(item.getId())
                            .courseId(item.getCourse().getId())
                            .courseTitle(item.getCourse().getTitle())
                            .courseThumbnail(item.getCourse().getThumbnailUrl())
                            .price(item.getPrice())
                            .build())
                    .toList()
                : List.of();

        return BookingResponse.builder()
                .id(booking.getId())
                .items(itemResponses)
                .status(booking.getStatus())
                .amount(booking.getAmount())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
