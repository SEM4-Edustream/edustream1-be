package sem4.edustreambe.dto.booking.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String id;
    String courseId;
    String courseTitle;
    String courseThumbnail;
    BookingStatus status;
    BigDecimal amount;
    LocalDateTime createdAt;
}
