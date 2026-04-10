package sem4.edustreambe.dto.booking.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrollmentResponse {
    String id;
    String courseId;
    String courseTitle;
    String courseThumbnail;
    Integer progressPercentage;
    LocalDateTime enrolledAt;
}
