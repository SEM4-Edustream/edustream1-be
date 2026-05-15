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

    @org.springframework.data.jpa.repository.Query("SELECT SUM(bi.price) FROM BookingItem bi WHERE bi.course.tutorProfile.id = :tutorProfileId AND bi.booking.status = 'PAID'")
    java.math.BigDecimal sumTotalRevenueByTutor(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId);

    @org.springframework.data.jpa.repository.Query("SELECT bi.course.id, bi.course.title, SUM(bi.price), COUNT(bi.id) FROM BookingItem bi " +
            "WHERE bi.course.tutorProfile.id = :tutorProfileId AND bi.booking.status = 'PAID' " +
            "GROUP BY bi.course.id, bi.course.title")
    java.util.List<Object[]> getRevenueByCourse(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId);

    @org.springframework.data.jpa.repository.Query("SELECT EXTRACT(YEAR FROM bi.booking.createdAt), EXTRACT(MONTH FROM bi.booking.createdAt), SUM(bi.price) " +
            "FROM BookingItem bi WHERE bi.course.tutorProfile.id = :tutorProfileId AND bi.booking.status = 'PAID' " +
            "AND bi.booking.createdAt >= :startDate " +
            "GROUP BY EXTRACT(YEAR FROM bi.booking.createdAt), EXTRACT(MONTH FROM bi.booking.createdAt) " +
            "ORDER BY EXTRACT(YEAR FROM bi.booking.createdAt) ASC, EXTRACT(MONTH FROM bi.booking.createdAt) ASC")
    java.util.List<Object[]> getMonthlyRevenue(@org.springframework.data.repository.query.Param("tutorProfileId") String tutorProfileId, 
                                               @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
}
