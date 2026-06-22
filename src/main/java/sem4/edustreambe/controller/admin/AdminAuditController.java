package sem4.edustreambe.controller.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.admin.response.AuditLogResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.common.PageMeta;
import sem4.edustreambe.entity.AuditLog;
import sem4.edustreambe.repository.AuditLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    AuditLogRepository auditLogRepository;

    @GetMapping
    public ApiResponse<PageMeta<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        List<AuditLogResponse> responses = logPage.getContent().stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .adminUsername(log.getAdmin() != null ? log.getAdmin().getUsername() : "UNKNOWN")
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .details(log.getDetails())
                        .ipAddress(log.getIpAddress())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();

        PageMeta<AuditLogResponse> pageMeta = PageMeta.<AuditLogResponse>builder()
                .content(responses)
                .pageNumber(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .build();

        return ApiResponse.<PageMeta<AuditLogResponse>>builder()
                .result(pageMeta)
                .build();
    }
}
