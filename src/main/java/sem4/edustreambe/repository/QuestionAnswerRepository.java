package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.QuestionAnswer;

import java.util.List;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, String> {

    List<QuestionAnswer> findByQuestionIdAndIsDeletedFalseOrderByIsTopAnswerDescCreatedAtAsc(String questionId);
}
