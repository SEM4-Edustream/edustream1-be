package sem4.edustreambe.dto.quiz.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.QuestionType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionRequest {
    @NotBlank(message = "Question content is required")
    String content;

    @NotNull(message = "Question type is required")
    QuestionType type;

    Integer orderIndex;

    List<QuizAnswerChoiceRequest> choices;
}
