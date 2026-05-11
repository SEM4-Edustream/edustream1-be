package sem4.edustreambe.dto.qa.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRequest {

    @NotBlank(message = "Nội dung câu trả lời không được để trống")
    String body;
}
