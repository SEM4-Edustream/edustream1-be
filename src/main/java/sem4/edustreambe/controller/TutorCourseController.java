package sem4.edustreambe.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.course.request.CourseCreationRequest;
import sem4.edustreambe.dto.course.request.CourseModuleRequest;
import sem4.edustreambe.dto.course.request.CourseUpdateRequest;
import sem4.edustreambe.dto.course.request.LessonRequest;
import sem4.edustreambe.dto.course.response.CourseModuleResponse;
import sem4.edustreambe.dto.course.response.CourseResponse;
import sem4.edustreambe.dto.course.response.LessonResponse;
import sem4.edustreambe.service.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/tutor-courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('TUTOR')")
public class TutorCourseController {

    CourseService courseService;

    @PostMapping
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseCreationRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.createCourse(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<CourseResponse>> getMyCourses() {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(courseService.getMyCourses())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getCourseDetail(@PathVariable String id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getCourseDetail(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseUpdateRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.updateCourse(id, request))
                .build();
    }

    @PostMapping("/{id}/modules")
    public ApiResponse<CourseModuleResponse> addModule(
            @PathVariable String id,
            @Valid @RequestBody CourseModuleRequest request) {
        return ApiResponse.<CourseModuleResponse>builder()
                .result(courseService.addModule(id, request))
                .build();
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ApiResponse<LessonResponse> addLesson(
            @PathVariable String moduleId,
            @Valid @RequestBody LessonRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(courseService.addLesson(moduleId, request))
                .build();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<CourseResponse> submitCourse(@PathVariable String id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.submitCourse(id))
                .build();
    }
}
