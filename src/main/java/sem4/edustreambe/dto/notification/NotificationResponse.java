package sem4.edustreambe.dto.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    String title;
    String message;
    NotificationType type;
    String referenceUrl;
    Boolean isRead;
    LocalDateTime createdAt;
}
