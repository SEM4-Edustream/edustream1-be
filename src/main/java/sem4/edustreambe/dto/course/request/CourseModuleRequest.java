package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseModuleRequest {

    @NotBlank(message = "Title is required")
    String title;

    String description;

    @NotNull(message = "OrderIndex is required")
    @Min(value = 0, message = "OrderIndex must be positive")
    Integer orderIndex;
}
