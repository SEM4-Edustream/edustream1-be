package sem4.edustreambe.dto.note.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoteRequest {
    @NotBlank(message = "COURSE_ID_REQUIRED")
    String courseId;
    
    @NotBlank(message = "LESSON_ID_REQUIRED")
    String lessonId;
    
    @NotBlank(message = "CONTENT_REQUIRED")
    String content;
    
    Integer timestampSeconds;
}
