package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Course;
import sem4.edustreambe.enums.CourseStatus;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByTutorProfileId(String tutorProfileId);
    List<Course> findByStatus(CourseStatus status);

    // Dành cho Public Search & Pagination
    org.springframework.data.domain.Page<Course> findByStatus(CourseStatus status, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Course> findByStatusAndTitleContainingIgnoreCase(CourseStatus status, String title, org.springframework.data.domain.Pageable pageable);
}
