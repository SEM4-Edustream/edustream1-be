package sem4.edustreambe.dto.quiz.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizAnswerChoiceResponse {
    String id;
    String content;
    Boolean isCorrect; // May be null when returned to students
    Integer orderIndex;
}
