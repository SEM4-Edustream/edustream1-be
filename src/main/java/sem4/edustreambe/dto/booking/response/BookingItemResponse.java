package sem4.edustreambe.dto.booking.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingItemResponse {
    String id;
    String courseId;
    String courseTitle;
    String courseThumbnail;
    BigDecimal price;
}
