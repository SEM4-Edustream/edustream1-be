package sem4.edustreambe.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.common.PageMeta;
import sem4.edustreambe.dto.assignment.request.AssignmentGradeRequest;
import sem4.edustreambe.dto.assignment.request.AssignmentSubmissionRequest;
import sem4.edustreambe.dto.assignment.response.AssignmentSubmissionResponse;
import sem4.edustreambe.service.AssignmentService;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentController {

    AssignmentService assignmentService;

    // --- Student Endpoints ---
    @PostMapping("/{lessonId}/submit")
    public ApiResponse<AssignmentSubmissionResponse> submitAssignment(
            @PathVariable String lessonId,
            @RequestBody @Valid AssignmentSubmissionRequest request) {
        return ApiResponse.<AssignmentSubmissionResponse>builder()
                .result(assignmentService.submitAssignment(lessonId, request))
                .build();
    }

    @GetMapping("/{lessonId}/my-submission")
    public ApiResponse<AssignmentSubmissionResponse> getMySubmission(@PathVariable String lessonId) {
        return ApiResponse.<AssignmentSubmissionResponse>builder()
                .result(assignmentService.getMySubmission(lessonId))
                .build();
    }

    // --- Tutor Endpoints ---
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @GetMapping("/{lessonId}/submissions")
    public ApiResponse<PageMeta<AssignmentSubmissionResponse>> getSubmissionsForTutor(
            @PathVariable String lessonId,
            Pageable pageable) {
        return ApiResponse.<PageMeta<AssignmentSubmissionResponse>>builder()
                .result(assignmentService.getSubmissionsForTutor(lessonId, pageable))
                .build();
    }

    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    @PostMapping("/submissions/{submissionId}/grade")
    public ApiResponse<AssignmentSubmissionResponse> gradeSubmission(
            @PathVariable String submissionId,
            @RequestBody @Valid AssignmentGradeRequest request) {
        return ApiResponse.<AssignmentSubmissionResponse>builder()
                .result(assignmentService.gradeSubmission(submissionId, request))
                .build();
    }
}
