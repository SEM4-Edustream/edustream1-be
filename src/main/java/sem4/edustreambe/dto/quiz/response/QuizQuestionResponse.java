package sem4.edustreambe.dto.quiz.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.QuestionType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizQuestionResponse {
    String id;
    String content;
    QuestionType type;
    Integer orderIndex;
    List<QuizAnswerChoiceResponse> choices;
}
