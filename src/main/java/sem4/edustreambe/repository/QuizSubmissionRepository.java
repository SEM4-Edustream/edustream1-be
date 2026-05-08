package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.QuizSubmission;

import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, String> {
    Optional<QuizSubmission> findByLessonIdAndStudentId(String lessonId, String studentId);
}
