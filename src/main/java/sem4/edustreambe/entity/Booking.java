package sem4.edustreambe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sem4.edustreambe.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BookingItem> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    BookingStatus status = BookingStatus.PENDING;

    @Column(precision = 12, scale = 2)
    BigDecimal amount;
}
