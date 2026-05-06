package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.BookingItem;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, String> {
    List<BookingItem> findByBookingId(String bookingId);
}
