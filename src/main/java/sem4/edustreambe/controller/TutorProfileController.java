package sem4.edustreambe.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.tutor.request.TutorDocumentRequest;
import sem4.edustreambe.dto.tutor.request.TutorProfileCreationRequest;
import sem4.edustreambe.dto.tutor.request.TutorProfileUpdateRequest;
import sem4.edustreambe.dto.tutor.response.TutorDocumentResponse;
import sem4.edustreambe.dto.tutor.response.TutorProfileResponse;
import sem4.edustreambe.service.TutorProfileService;


@RestController
@RequestMapping("/api/tutor-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TutorProfileController {

    TutorProfileService tutorProfileService;


    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<TutorProfileResponse> createProfile(
            @Valid @RequestBody TutorProfileCreationRequest request) {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.createProfile(request))
                .message("Tutor profile created successfully")
                .build();
    }


    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    public ApiResponse<TutorProfileResponse> getMyProfile() {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.getMyProfile())
                .build();
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    public ApiResponse<TutorProfileResponse> updateMyProfile(
            @RequestBody TutorProfileUpdateRequest request) {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.updateMyProfile(request))
                .message("Profile updated successfully")
                .build();
    }


    @PostMapping("/documents")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    public ApiResponse<TutorDocumentResponse> addDocument(
            @Valid @RequestBody TutorDocumentRequest request) {
        return ApiResponse.<TutorDocumentResponse>builder()
                .result(tutorProfileService.addDocument(request))
                .message("Document added successfully")
                .build();
    }


    @PostMapping("/submit-verification")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    public ApiResponse<TutorProfileResponse> submitForVerification() {
        return ApiResponse.<TutorProfileResponse>builder()
                .result(tutorProfileService.submitForVerification())
                .message("Profile submitted for verification successfully")
                .build();
    }
}
