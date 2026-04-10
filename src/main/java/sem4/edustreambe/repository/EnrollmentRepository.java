package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Enrollment;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    List<Enrollment> findByUserId(UUID userId);
    boolean existsByUserIdAndCourseId(UUID userId, String courseId);
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, String courseId);
}
