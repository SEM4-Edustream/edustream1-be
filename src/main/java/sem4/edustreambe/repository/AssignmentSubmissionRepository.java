package sem4.edustreambe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.AssignmentSubmission;

import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, String> {
    Optional<AssignmentSubmission> findByLessonIdAndStudentId(String lessonId, String studentId);
    Page<AssignmentSubmission> findByLessonId(String lessonId, Pageable pageable);
}
