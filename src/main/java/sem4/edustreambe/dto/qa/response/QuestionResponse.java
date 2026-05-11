package sem4.edustreambe.dto.qa.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {
    String id;
    String courseId;
    String courseTitle;
    String lessonId;
    String lessonTitle;
    String studentId;
    String studentName;
    String studentAvatar;
    String title;
    String body;
    Boolean isResolved;
    Integer answerCount;
    List<AnswerResponse> answers;
    LocalDateTime createdAt;
}
