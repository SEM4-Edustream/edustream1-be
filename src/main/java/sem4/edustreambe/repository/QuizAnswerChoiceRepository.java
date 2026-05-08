package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.QuizAnswerChoice;

import java.util.List;

@Repository
public interface QuizAnswerChoiceRepository extends JpaRepository<QuizAnswerChoice, String> {
    List<QuizAnswerChoice> findByQuestionIdOrderByOrderIndexAsc(String questionId);
}
