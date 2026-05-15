package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorStudentResponse {
    String enrollmentId;
    String studentId;
    String fullName;
    String email;
    String avatarUrl;
    String courseId;
    String courseTitle;
    int progressPercentage;
    LocalDateTime enrolledAt;
}
