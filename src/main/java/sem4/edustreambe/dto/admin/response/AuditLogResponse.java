package sem4.edustreambe.dto.admin.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogResponse {
    String id;
    String adminUsername;
    String action;
    String entityType;
    String entityId;
    String details;
    String ipAddress;
    LocalDateTime createdAt;
}
