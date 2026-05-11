package sem4.edustreambe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    // Lấy Q&A theo khóa học (cho học viên)
    Page<Question> findByCourseIdAndIsDeletedFalseOrderByCreatedAtDesc(String courseId, Pageable pageable);

    // Lấy Q&A theo nhiều khóa học (cho tutor xem tất cả khóa học của mình)
    @Query("SELECT q FROM Question q WHERE q.course.id IN :courseIds AND q.isDeleted = false ORDER BY q.createdAt DESC")
    Page<Question> findByCourseIdsOrderByCreatedAtDesc(@Param("courseIds") java.util.List<String> courseIds, Pageable pageable);

    // Lấy Q&A chưa có câu trả lời nào
    @Query("SELECT q FROM Question q WHERE q.course.id IN :courseIds AND q.isDeleted = false AND q.answerCount = 0 ORDER BY q.createdAt DESC")
    Page<Question> findNoAnswerByCourseIds(@Param("courseIds") java.util.List<String> courseIds, Pageable pageable);

    // Lấy Q&A chưa có câu trả lời từ giảng viên
    @Query("""
        SELECT q FROM Question q 
        WHERE q.course.id IN :courseIds 
        AND q.isDeleted = false 
        AND NOT EXISTS (
            SELECT a FROM QuestionAnswer a WHERE a.question = q AND a.isInstructorAnswer = true AND a.isDeleted = false
        )
        ORDER BY q.createdAt DESC
    """)
    Page<Question> findNoInstructorAnswerByCourseIds(@Param("courseIds") java.util.List<String> courseIds, Pageable pageable);

    long countByCourseIdAndIsDeletedFalse(String courseId);
}
