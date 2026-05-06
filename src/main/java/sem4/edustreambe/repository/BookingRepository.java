package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.Booking;
import sem4.edustreambe.enums.BookingStatus;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b JOIN b.items i WHERE b.user.id = :userId AND i.course.id = :courseId AND b.status = :status")
    Optional<Booking> findByUserIdAndCourseIdAndStatus(@org.springframework.data.repository.query.Param("userId") UUID userId, @org.springframework.data.repository.query.Param("courseId") String courseId, @org.springframework.data.repository.query.Param("status") BookingStatus status);
}
