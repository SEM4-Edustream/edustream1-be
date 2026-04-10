package sem4.edustreambe.dto.tutor.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.enums.VerificationStatus;

/**
 * Request DTO dành cho Admin khi duyệt hồ sơ Tutor.
 * action: chỉ chấp nhận APPROVED hoặc REJECTED.
 * reviewComment: bắt buộc khi REJECT để thông báo lý do cho Tutor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationReviewRequest {

    @NotNull(message = "Review action is required (APPROVED or REJECTED)")
    VerificationStatus action;

    String reviewComment;
}
