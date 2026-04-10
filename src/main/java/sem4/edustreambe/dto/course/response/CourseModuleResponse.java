package sem4.edustreambe.dto.course.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseModuleResponse {
    String id;
    String title;
    String description;
    Integer orderIndex;
    List<LessonResponse> lessons;
}
