package sem4.edustreambe.controller.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.tutor.request.VerificationReviewRequest;
import sem4.edustreambe.dto.tutor.response.TutorProfileResponse;
import sem4.edustreambe.enums.VerificationStatus;
import sem4.edustreambe.service.TutorProfileService;

import java.util.List;


@RestController
@RequestMapping("/api/admin/tutor-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminTutorController {

    TutorProfileService tutorProfileService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<TutorProfileResponse>> getProfilesByStatus(
            @RequestParam(required = false) VerificationStatus status) {
        return ApiResponse.<List<TutorProfileResponse>>builder()
                .result(tutorProfileService.getProfilesByStatus(status))
                .build();
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TutorProfileResponse> getProfileById(@PathVariable String id) {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.getProfileById(id))
                .build();
    }


    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TutorProfileResponse> reviewProfile(
            @PathVariable String id,
            @Valid @RequestBody VerificationReviewRequest request) {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.reviewProfile(id, request))
                .message("Profile reviewed successfully")
                .build();
    }
}
