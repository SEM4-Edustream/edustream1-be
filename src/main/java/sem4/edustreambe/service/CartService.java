package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.cart.CartItemResponse;
import sem4.edustreambe.entity.CartItem;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.CartItemRepository;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartItemRepository cartItemRepository;
    CourseRepository courseRepository;
    UserRepository userRepository;
    EnrollmentRepository enrollmentRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<CartItemResponse> getCartItems() {
        User user = getCurrentUser();
        return cartItemRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public CartItemResponse addToCart(String courseId) {
        User user = getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Validate course is published
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        // Check if already enrolled
        if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            throw new AppException(ErrorCode.ALREADY_ENROLLED);
        }

        // Check if already in cart
        if (cartItemRepository.findByUserIdAndCourseId(user.getId(), courseId).isPresent()) {
            throw new AppException(ErrorCode.ALREADY_IN_CART);
        }

        CartItem cartItem = CartItem.builder()
                .user(user)
                .course(course)
                .build();

        CartItem saved = cartItemRepository.save(cartItem);
        return toResponse(saved);
    }

    public void removeFromCart(String courseId) {
        User user = getCurrentUser();
        if (cartItemRepository.findByUserIdAndCourseId(user.getId(), courseId).isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteByUserIdAndCourseId(user.getId(), courseId);
    }

    public void clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
    }

    public int getCartCount() {
        User user = getCurrentUser();
        return cartItemRepository.findByUserId(user.getId()).size();
    }

    private CartItemResponse toResponse(CartItem item) {
        Course course = item.getCourse();
        String tutorName = course.getTutorProfile() != null
                && course.getTutorProfile().getUser() != null
                ? course.getTutorProfile().getUser().getFullName()
                : "Unknown";

        return CartItemResponse.builder()
                .id(item.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseSubtitle(course.getSubtitle())
                .courseThumbnail(course.getThumbnailUrl())
                .tutorName(tutorName)
                .coursePrice(course.getPrice())
                .courseRating(course.getAverageRating())
                .courseReviewCount(course.getReviewCount())
                .build();
    }
}
