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
    List<Enrollment> findByCourseId(String courseId);
    boolean existsByUserIdAndCourseId(UUID userId, String courseId);
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, String courseId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.tutorProfile.id = :tutorProfileId")
    long countUniqueStudentsByTutor(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(AVG(e.progressPercentage), 0) FROM Enrollment e WHERE e.course.tutorProfile.id = :tutorProfileId")
    Double getAverageProgressByTutor(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId);
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Enrollment e WHERE e.course.tutorProfile.id = :tutorId " +
            "AND (:courseId IS NULL OR e.course.id = :courseId)")
    org.springframework.data.domain.Page<Enrollment> findStudentsByTutorAndCourse(
            @org.springframework.data.repository.query.Param("tutorId") String tutorId,
            @org.springframework.data.repository.query.Param("courseId") String courseId,
            org.springframework.data.domain.Pageable pageable);

    java.util.List<Enrollment> findTop5ByCourseTutorProfileIdOrderByEnrolledAtDesc(String tutorId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT e FROM Enrollment e " +
            "JOIN FETCH e.user " +
            "JOIN FETCH e.course c " +
            "JOIN FETCH c.tutorProfile tp " +
            "JOIN FETCH tp.user",
            countQuery = "SELECT COUNT(e) FROM Enrollment e")
    org.springframework.data.domain.Page<Enrollment> findAllEnrollmentDetails(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT new sem4.edustreambe.dto.admin.response.AdminCourseMetricResponse(" +
            "c.title, u.username, COUNT(e.id), AVG(e.progressPercentage)) " +
            "FROM Enrollment e " +
            "JOIN e.course c " +
            "JOIN c.tutorProfile tp " +
            "JOIN tp.user u " +
            "GROUP BY c.id, c.title, u.username")
    org.springframework.data.domain.Page<sem4.edustreambe.dto.admin.response.AdminCourseMetricResponse> getCourseMetrics(org.springframework.data.domain.Pageable pageable);
}
