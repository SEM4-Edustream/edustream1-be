package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.TutorProfile;
import sem4.edustreambe.enums.VerificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TutorProfileRepository extends JpaRepository<TutorProfile, String> {
    Optional<TutorProfile> findByUserId(UUID userId);
    List<TutorProfile> findAllByStatus(VerificationStatus status);
}
