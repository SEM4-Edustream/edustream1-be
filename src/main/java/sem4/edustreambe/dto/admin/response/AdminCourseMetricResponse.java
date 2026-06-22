package sem4.edustreambe.dto.admin.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCourseMetricResponse {
    String courseTitle;
    String tutorName;
    Long totalStudents;
    Double averageProgress;
}
