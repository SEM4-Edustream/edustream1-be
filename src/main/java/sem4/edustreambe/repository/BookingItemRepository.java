package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.BookingItem;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, String> {
    List<BookingItem> findByBookingId(String bookingId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(bi.price) FROM BookingItem bi WHERE bi.course.tutorProfile.id = :tutorProfileId AND bi.booking.status = 'PAID' AND bi.createdAt >= :startDate AND bi.createdAt <= :endDate")
    java.math.BigDecimal sumRevenueByTutorAndDateRange(
            @org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId, 
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);
}
