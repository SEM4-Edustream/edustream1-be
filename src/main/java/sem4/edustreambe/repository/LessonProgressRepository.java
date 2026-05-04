package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.LessonProgress;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.entity.Course;

import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, String> {

    Optional<LessonProgress> findByUserIdAndLessonId(java.util.UUID userId, String lessonId);
    
    java.util.List<LessonProgress> findByUserIdAndLesson_Module_CourseIdAndIsCompletedTrue(java.util.UUID userId, String courseId);

    long countByUserAndLesson_Module_CourseAndIsCompletedTrue(User user, Course course);
}
