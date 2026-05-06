package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.wishlist.WishlistItemResponse;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.entity.WishlistItem;
import sem4.edustreambe.enums.CourseStatus;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.CourseRepository;
import sem4.edustreambe.repository.UserRepository;
import sem4.edustreambe.repository.WishlistItemRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WishlistService {

    WishlistItemRepository wishlistItemRepository;
    CourseRepository courseRepository;
    UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<WishlistItemResponse> getWishlistItems() {
        User user = getCurrentUser();
        return wishlistItemRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public WishlistItemResponse addToWishlist(String courseId) {
        User user = getCurrentUser();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new AppException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        if (wishlistItemRepository.findByUserIdAndCourseId(user.getId(), courseId).isPresent()) {
            throw new AppException(ErrorCode.ALREADY_IN_WISHLIST);
        }

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .course(course)
                .build();

        WishlistItem saved = wishlistItemRepository.save(item);
        return toResponse(saved);
    }

    public void removeFromWishlist(String courseId) {
        User user = getCurrentUser();
        if (wishlistItemRepository.findByUserIdAndCourseId(user.getId(), courseId).isEmpty()) {
            throw new AppException(ErrorCode.WISHLIST_ITEM_NOT_FOUND);
        }
        wishlistItemRepository.deleteByUserIdAndCourseId(user.getId(), courseId);
    }

    public int getWishlistCount() {
        User user = getCurrentUser();
        return wishlistItemRepository.findByUserId(user.getId()).size();
    }

    private WishlistItemResponse toResponse(WishlistItem item) {
        Course course = item.getCourse();
        String tutorName = course.getTutorProfile() != null
                && course.getTutorProfile().getUser() != null
                ? course.getTutorProfile().getUser().getFullName()
                : "Unknown";

        return WishlistItemResponse.builder()
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
