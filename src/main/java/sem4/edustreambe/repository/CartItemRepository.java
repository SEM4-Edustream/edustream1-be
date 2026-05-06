package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.CartItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(UUID userId);
    Optional<CartItem> findByUserIdAndCourseId(UUID userId, String courseId);
    void deleteByUserIdAndCourseId(UUID userId, String courseId);
    void deleteByUserId(UUID userId);
}
