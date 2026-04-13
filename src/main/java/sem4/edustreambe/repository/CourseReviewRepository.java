package sem4.edustreambe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.CourseReview;

import java.util.Optional;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, String> {
    Page<CourseReview> findByCourseId(String courseId, Pageable pageable);
    Optional<CourseReview> findByUserIdAndCourseId(String userId, String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
}
