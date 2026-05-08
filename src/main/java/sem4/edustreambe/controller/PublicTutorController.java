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
import sem4.edustreambe.dto.tutor.response.PublicTutorResponse;
import sem4.edustreambe.service.TutorProfileService;

@RestController
@RequestMapping("/api/public/tutors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Public Tutor API", description = "Các API mở dành cho khách và học viên xem hồ sơ giáo viên")
public class PublicTutorController {

    TutorProfileService tutorProfileService;

    @GetMapping("/{id}")
    @Operation(summary = "Xem hồ sơ công khai của một giáo viên")
    public ApiResponse<PublicTutorResponse> getPublicTutorProfile(@PathVariable String id) {
        return ApiResponse.<PublicTutorResponse>builder()
                .result(tutorProfileService.getPublicTutorProfile(id))
                .build();
    }
}
