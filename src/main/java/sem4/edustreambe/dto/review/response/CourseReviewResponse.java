package sem4.edustreambe.dto.review.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseReviewResponse {
    String id;
    String userId;
    String fullName;
    String avatarUrl;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
}
