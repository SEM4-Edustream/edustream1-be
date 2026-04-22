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
    public ApiResponse<org.springframework.data.domain.Page<CourseResponse>> getMyCourses(
            @RequestParam(required = false) sem4.edustreambe.enums.CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort sortOrder = sem4.edustreambe.enums.CourseStatus.DRAFT.name().equals(sortParams[0]) // just a dummy check for import
                ? org.springframework.data.domain.Sort.unsorted() 
                : org.springframework.data.domain.Sort.by(
                    sortParams[1].equalsIgnoreCase("desc") 
                        ? org.springframework.data.domain.Sort.Direction.DESC 
                        : org.springframework.data.domain.Sort.Direction.ASC, 
                    sortParams[0]
                  );
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortOrder);
        
        return ApiResponse.<org.springframework.data.domain.Page<CourseResponse>>builder()
                .result(courseService.getMyCourses(status, pageable))
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

    @PutMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ApiResponse<LessonResponse> updateLesson(
            @PathVariable String moduleId,
            @PathVariable String lessonId,
            @Valid @RequestBody LessonRequest request) {
        return ApiResponse.<LessonResponse>builder()
                .result(courseService.updateLesson(moduleId, lessonId, request))
                .build();
    }

    @DeleteMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ApiResponse<Void> deleteLesson(
            @PathVariable String moduleId,
            @PathVariable String lessonId) {
        courseService.deleteLesson(moduleId, lessonId);
        return ApiResponse.<Void>builder()
                .message("Lesson deleted successfully")
                .build();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<CourseResponse> submitCourse(@PathVariable String id) {
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.submitCourse(id))
                .build();
    }
}
