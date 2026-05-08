package sem4.edustreambe.dto.assignment.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentGradeRequest {
    @NotNull(message = "Grade is required")
    @Min(value = 0, message = "Grade must be between 0 and 100")
    @Max(value = 100, message = "Grade must be between 0 and 100")
    Float grade;

    String feedback;
}
