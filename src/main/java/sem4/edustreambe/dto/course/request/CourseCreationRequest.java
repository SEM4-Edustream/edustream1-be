package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseCreationRequest {

    @NotBlank(message = "Title is required")
    String title;

    String description;

    String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    BigDecimal price;
}
