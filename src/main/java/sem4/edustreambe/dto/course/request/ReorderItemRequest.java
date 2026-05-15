package sem4.edustreambe.dto.course.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReorderItemRequest {

    @NotBlank(message = "ID is required")
    String id;

    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index must be at least 0")
    Integer orderIndex;
}
