package sem4.edustreambe.dto.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    String id;
    String courseId;
    String courseTitle;
    String courseSubtitle;
    String courseThumbnail;
    String tutorName;
    BigDecimal coursePrice;
    Float courseRating;
    Integer courseReviewCount;
}
