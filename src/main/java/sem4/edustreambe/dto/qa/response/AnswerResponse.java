package sem4.edustreambe.dto.qa.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerResponse {
    String id;
    String questionId;
    String authorId;
    String authorName;
    String authorAvatar;
    Boolean isInstructor;
    String body;
    Boolean isTopAnswer;
    Boolean isInstructorAnswer;
    LocalDateTime createdAt;
}
