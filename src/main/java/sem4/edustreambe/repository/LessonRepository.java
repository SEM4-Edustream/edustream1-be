package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Lesson;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findByModuleIdOrderByOrderIndexAsc(String moduleId);
    
    long countByModule_Course(sem4.edustreambe.entity.Course course);
}
