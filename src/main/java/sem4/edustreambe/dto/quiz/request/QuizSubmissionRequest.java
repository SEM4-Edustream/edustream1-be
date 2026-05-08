package sem4.edustreambe.dto.quiz.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmissionRequest {
    // Map of questionId -> List of selected choiceIds
    @NotNull(message = "Answers are required")
    Map<String, List<String>> answers;
}
