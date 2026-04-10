package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.TutorDocument;

@Repository
public interface TutorDocumentRepository extends JpaRepository<TutorDocument, String> {

    // Đếm số lượng tài liệu theo profile — dùng để validate submit-verification
    long countByTutorProfileId(String tutorProfileId);
}

