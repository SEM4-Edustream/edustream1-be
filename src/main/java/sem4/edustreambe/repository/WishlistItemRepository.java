package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.WishlistItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {
    List<WishlistItem> findByUserId(UUID userId);
    Optional<WishlistItem> findByUserIdAndCourseId(UUID userId, String courseId);
    void deleteByUserIdAndCourseId(UUID userId, String courseId);
}
