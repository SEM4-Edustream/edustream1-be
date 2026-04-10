package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.CourseModule;

import java.util.List;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, String> {
    List<CourseModule> findByCourseIdOrderByOrderIndexAsc(String courseId);
}
