package sem4.edustreambe.dto.course.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VideoDurationWebhookRequest {
    String lessonId;
    Integer durationSeconds;
    String secretKey;
}
