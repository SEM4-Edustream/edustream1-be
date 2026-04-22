package sem4.edustreambe.controller.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.course.response.CourseResponse;
import sem4.edustreambe.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    CourseService courseService;

    @GetMapping("/pending")
    public ApiResponse<List<CourseResponse>> getPendingCourses() {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.getPendingCourses())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseDetail(@PathVariable String id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getCourseDetail(id))
                .build();
    }

    @PostMapping("/{id}/verify")
    public ApiResponse<CourseResponse> verifyCourse(
            @PathVariable String id,
            @RequestParam boolean isApprove) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.reviewCourse(id, isApprove))
                .build();
    }
}
