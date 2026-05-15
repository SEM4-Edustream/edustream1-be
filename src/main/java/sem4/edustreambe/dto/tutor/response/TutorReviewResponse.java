package sem4.edustreambe.dto.tutor.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorReviewResponse {
    private String id;
    private String courseId;
    private String courseTitle;
    private String studentName;
    private String studentAvatar;
    private double rating;
    private String comment;
    private LocalDateTime createdAt;
}
