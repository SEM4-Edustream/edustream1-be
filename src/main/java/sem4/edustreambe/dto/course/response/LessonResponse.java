package sem4.edustreambe.dto.course.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.LessonType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {
    String id;
    String title;
    String content;
    LessonType type;
    String videoUrl;
    Integer durationSeconds;
    Integer orderIndex;
}
