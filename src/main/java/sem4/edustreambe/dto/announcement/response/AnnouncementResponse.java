package sem4.edustreambe.dto.announcement.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnnouncementResponse {
    String id;
    String title;
    String content;
    String authorName;
    String authorAvatar;
    LocalDateTime createdAt;
}
