package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.review.request.CourseReviewRequest;
import sem4.edustreambe.dto.review.response.CourseReviewResponse;
import sem4.edustreambe.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Course Review API", description = "Đánh giá Khóa học (Rating & Comments)")
public class ReviewController {

    ReviewService reviewService;

    @GetMapping("/courses/{courseId}")
    @Operation(summary = "Xem danh sách Evaluate (Review) của một khóa học (Public)")
    public ApiResponse<Page<CourseReviewResponse>> getCourseReviews(
            @PathVariable String courseId,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ApiResponse.<Page<CourseReviewResponse>>builder()
                .result(reviewService.getCourseReviews(courseId, pageable))
                .build();
    }

    @PostMapping("/courses/{courseId}")
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    @Operation(summary = "Viết đánh giá Khóa học (Yêu cầu Đã mua khóa học)", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<CourseReviewResponse> createReview(
            @PathVariable String courseId,
            @Valid @RequestBody CourseReviewRequest request) {
        return ApiResponse.<CourseReviewResponse>builder()
                .result(reviewService.createReview(courseId, request))
                .message("Review submitted successfully!")
                .build();
    }
}
