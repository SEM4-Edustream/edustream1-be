package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.booking.response.EnrollmentResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.EnrollmentService;

import java.util.List;

@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
public class StudentEnrollmentController {

    EnrollmentService enrollmentService;

    @GetMapping("/my-courses")
    public ApiResponse<List<EnrollmentResponse>> getMyEnrollments() {
        return ApiResponse.<List<EnrollmentResponse>>builder()
                .result(enrollmentService.getMyEnrollments())
                .build();
    }
}
