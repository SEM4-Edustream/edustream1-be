package sem4.edustreambe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.CourseReview;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, String> {
    Page<CourseReview> findByCourseId(String courseId, Pageable pageable);
    Optional<CourseReview> findByUserIdAndCourseId(UUID userId, String courseId);
    boolean existsByUserIdAndCourseId(UUID userId, String courseId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(AVG(cr.rating), 0) FROM CourseReview cr WHERE cr.course.tutorProfile.id = :tutorProfileId")
    Double getAverageRatingByTutor(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId);
}
