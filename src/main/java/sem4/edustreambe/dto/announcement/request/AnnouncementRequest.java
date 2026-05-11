package sem4.edustreambe.dto.announcement.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnnouncementRequest {
    @NotBlank(message = "TITLE_REQUIRED")
    String title;
    
    @NotBlank(message = "CONTENT_REQUIRED")
    String content;
}
