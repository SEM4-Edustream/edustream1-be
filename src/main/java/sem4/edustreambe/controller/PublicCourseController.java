package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.course.response.CourseResponse;
import sem4.edustreambe.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Public Course API", description = "Các API mở dành cho khách và học viên duyệt khóa học trên Marketplace")
public class PublicCourseController {

    CourseService courseService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả các Khóa học đã được PUBLISHED (Mở bán)")
    public ApiResponse<org.springframework.data.domain.Page<CourseResponse>> getAllPublishedCourses(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
        return ApiResponse.<org.springframework.data.domain.Page<CourseResponse>>builder()
                .result(courseService.getPublicCourses(keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một Khóa học đã được PUBLISHED")
    public ApiResponse<CourseResponse> getPublishedCourseDetail(@PathVariable String id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getPublicCourseDetail(id))
                .build();
    }
}
