package sem4.edustreambe.dto.admin.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEnrollmentDetailResponse {
    String studentName;
    String studentEmail;
    String courseTitle;
    String tutorName;
    Integer progressPercentage;
    LocalDateTime enrolledAt;
}
