package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.announcement.request.AnnouncementRequest;
import sem4.edustreambe.dto.announcement.response.AnnouncementResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.AnnouncementService;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/announcements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Announcement API", description = "Quản lý thông báo trong khóa học")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    @Operation(summary = "Tutor đăng thông báo mới cho khóa học")
    public ApiResponse<AnnouncementResponse> createAnnouncement(
            @PathVariable String courseId,
            @RequestBody @Valid AnnouncementRequest request) {
        return ApiResponse.<AnnouncementResponse>builder()
                .result(announcementService.createAnnouncement(courseId, request))
                .build();
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của khóa học")
    public ApiResponse<List<AnnouncementResponse>> getCourseAnnouncements(
            @PathVariable String courseId) {
        return ApiResponse.<List<AnnouncementResponse>>builder()
                .result(announcementService.getCourseAnnouncements(courseId))
                .build();
    }
}
