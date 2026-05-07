package sem4.edustreambe.dto.note.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoteResponse {
    String id;
    String lessonId;
    String lessonTitle;
    String content;
    Integer timestampSeconds;
    LocalDateTime createdDate;
}
