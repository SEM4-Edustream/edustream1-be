package sem4.edustreambe.dto.tutor.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.dto.course.response.CourseResponse;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicTutorResponse {
    String id;
    String fullName;
    String avatarUrl;
    String headline;
    String bio;
    String videoIntroduction;
    
    // Stats
    long totalCourses;
    float averageRating;
    int totalReviews;
    
    // Published Courses
    List<CourseResponse> courses;
}
