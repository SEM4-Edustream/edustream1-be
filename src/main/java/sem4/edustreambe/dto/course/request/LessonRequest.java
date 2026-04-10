package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.LessonType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonRequest {

    @NotBlank(message = "Title is required")
    String title;

    String content;

    @NotNull(message = "Lesson type is required")
    LessonType type;

    String videoUrl;

    Integer durationSeconds;

    @NotNull(message = "OrderIndex is required")
    @Min(value = 0, message = "OrderIndex must be positive")
    Integer orderIndex;
}
