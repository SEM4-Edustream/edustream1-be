package sem4.edustreambe.dto.assignment.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.dto.user.response.UserResponse;
import sem4.edustreambe.enums.AssignmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentSubmissionResponse {
    String id;
    String content;
    String fileUrl;
    AssignmentStatus status;
    Float grade;
    String feedback;
    UserResponse student;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
