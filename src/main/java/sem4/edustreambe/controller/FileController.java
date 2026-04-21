package sem4.edustreambe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.service.FileService;
import sem4.edustreambe.dto.common.FileResponse;
import sem4.edustreambe.constant.PredefinedRole;
import sem4.edustreambe.enums.BucketType;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "File Upload API", description = "Quản lý và cấp quyền upload file bằng AWS S3")
public class FileController {

    FileService fileService;

    @GetMapping("/presigned-url")
    @Operation(summary = "Lấy URL cấp quyền upload file trực tiếp lên AWS S3 (hạn 15 phút)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN', 'STUDENT')")
    public ApiResponse<FileResponse> getPresignedUrl(
            @RequestParam String fileName, 
            @RequestParam String contentType,
            @RequestParam(required = false, defaultValue = "VIDEO") BucketType type) {
        FileResponse response = fileService.generatePresignedUploadUrl(fileName, contentType, type);
        return ApiResponse.<FileResponse>builder()
                .message("URL Upload file được sinh thành công")
                .result(response)
                .build();
    }
}
