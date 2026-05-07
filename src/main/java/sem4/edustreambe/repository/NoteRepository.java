package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Note;
import sem4.edustreambe.entity.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> findAllByUserIdAndCourseIdOrderByCreatedAtDesc(UUID userId, String courseId);
}
