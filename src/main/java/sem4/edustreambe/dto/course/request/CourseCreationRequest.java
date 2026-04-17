package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCreationRequest {

    @NotBlank(message = "Title is required")
    String title;

    String subtitle;

    String description;

    String language;
    String level;
    String category;

    List<String> learningObjectives;
    List<String> prerequisites;
    List<String> targetAudiences;

    String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    BigDecimal price;
}
