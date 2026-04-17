package sem4.edustreambe.dto.course.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.CourseStatus;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseResponse {
    String id;
    String tutorProfileId;
    String tutorName;
    String title;
    String subtitle;
    String description;
    String language;
    String level;
    String category;
    List<String> learningObjectives;
    List<String> prerequisites;
    List<String> targetAudiences;
    String thumbnailUrl;
    BigDecimal price;
    CourseStatus status;
    Float averageRating;
    Integer reviewCount;
    List<CourseModuleResponse> modules;
}
