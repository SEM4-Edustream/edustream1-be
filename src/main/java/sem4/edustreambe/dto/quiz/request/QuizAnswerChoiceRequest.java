package sem4.edustreambe.dto.quiz.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizAnswerChoiceRequest {
    @NotBlank(message = "Answer content is required")
    String content;

    Boolean isCorrect;

    Integer orderIndex;
}
