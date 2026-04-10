package sem4.edustreambe.dto.tutor.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO dùng để cập nhật thông tin hồ sơ Tutor.
 * Tất cả các trường đều optional — chỉ cập nhật những field không null.
 * Chỉ cho phép khi profile đang ở trạng thái DRAFT hoặc REJECTED.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TutorProfileUpdateRequest {

    String headline;
    String bio;
    String videoIntroduction;
}
