package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.username = :login OR LOWER(u.email) = LOWER(:login)")
    Optional<User> findByUsernameOrEmail(String login);

    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByRoleName(String roleName);
}