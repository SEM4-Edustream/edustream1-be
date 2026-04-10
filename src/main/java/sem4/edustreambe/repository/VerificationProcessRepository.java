package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.VerificationProcess;

@Repository
public interface VerificationProcessRepository extends JpaRepository<VerificationProcess, String> {
}
