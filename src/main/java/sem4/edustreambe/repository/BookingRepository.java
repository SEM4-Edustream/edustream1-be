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

    Optional<Booking> findByUserIdAndCourseIdAndStatus(UUID userId, String courseId, BookingStatus status);
}
