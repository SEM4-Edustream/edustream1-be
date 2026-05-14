package sem4.edustreambe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sem4.edustreambe.entity.PaymentTransaction;
import sem4.edustreambe.enums.TransactionStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    @org.springframework.data.jpa.repository.Query("SELECT t FROM PaymentTransaction t " +
            "LEFT JOIN FETCH t.booking b " +
            "LEFT JOIN FETCH b.user " +
            "LEFT JOIN FETCH b.items i " +
            "LEFT JOIN FETCH i.course " +
            "WHERE t.orderCode = :orderCode")
    Optional<PaymentTransaction> findByOrderCode(Long orderCode);

    Optional<PaymentTransaction> findByBookingId(String bookingId);

    List<PaymentTransaction> findAllByBookingId(String bookingId);

    List<PaymentTransaction> findAllByBookingIdAndStatus(String bookingId, TransactionStatus status);
}
