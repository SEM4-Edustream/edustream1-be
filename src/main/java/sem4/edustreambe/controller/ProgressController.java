package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.ProgressService;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Progress API", description = "Quản lý tiến độ học tập của học viên")
public class ProgressController {

    ProgressService progressService;

    @PostMapping("/lessons/{lessonId}/complete")
    @Operation(summary = "Đánh dấu hoàn thành bài học", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    public ApiResponse<Void> completeLesson(@PathVariable String lessonId) {
        progressService.markLessonComplete(lessonId);
        return ApiResponse.<Void>builder()
                .message("Đánh dấu hoàn thành bài học thành công!")
                .build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/courses/{courseId}")
    @Operation(summary = "Lấy danh sách ID các bài học đã hoàn thành của một khóa học", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('USER', 'STUDENT')")
    public ApiResponse<java.util.List<String>> getCourseProgress(@PathVariable String courseId) {
        return ApiResponse.<java.util.List<String>>builder()
                .result(progressService.getCompletedLessonIds(courseId))
                .build();
    }
}
