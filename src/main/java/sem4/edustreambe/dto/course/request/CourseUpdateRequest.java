package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.CourseLevel;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseUpdateRequest {

    String title;

    String subtitle;

    String description;

    String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    BigDecimal price;

    @NotBlank(message = "Category is required")
    String categoryId;

    CourseLevel level;

    List<String> learningObjectives;

    List<String> prerequisites;

    List<String> targetAudiences;
}
