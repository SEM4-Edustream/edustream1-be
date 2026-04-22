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
    String description;
    String thumbnailUrl;
    BigDecimal price;
    CourseStatus status;
    Float averageRating;
    Integer reviewCount;
    CategoryResponse category;
    List<CourseModuleResponse> modules;
    List<String> learningObjectives;
    List<String> prerequisites;
    List<String> targetAudiences;
}
