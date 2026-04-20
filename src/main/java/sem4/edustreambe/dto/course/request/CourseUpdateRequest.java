package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseUpdateRequest {

    String title;

    String description;

    String thumbnailUrl;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    BigDecimal price;

    String categoryId;
}
