package sem4.edustreambe.dto.quiz.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizSubmissionResponse {
    String id;
    Float score;
    Boolean passed;
}
