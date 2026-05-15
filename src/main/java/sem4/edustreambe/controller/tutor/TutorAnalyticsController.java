package sem4.edustreambe.controller.tutor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.tutor.response.TutorAnalyticsResponse;
import sem4.edustreambe.dto.tutor.response.TutorStudentResponse;
import sem4.edustreambe.service.TutorAnalyticsService;

@RestController
@RequestMapping("/api/tutor/analytics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('TUTOR')")
public class TutorAnalyticsController {

    TutorAnalyticsService tutorAnalyticsService;

    @GetMapping
    public ApiResponse<TutorAnalyticsResponse> getTutorAnalytics() {
        return ApiResponse.<TutorAnalyticsResponse>builder()
                .result(tutorAnalyticsService.getTutorAnalytics())
                .build();
    }

    @GetMapping("/students")
    public ApiResponse<sem4.edustreambe.dto.common.PageMeta<TutorStudentResponse>> getTutorStudents(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String courseId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = 
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("enrolledAt").descending());
                
        return ApiResponse.<sem4.edustreambe.dto.common.PageMeta<TutorStudentResponse>>builder()
                .result(tutorAnalyticsService.getTutorStudents(courseId, pageable))
                .build();
    }
}
