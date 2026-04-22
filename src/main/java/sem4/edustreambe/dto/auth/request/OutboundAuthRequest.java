package sem4.edustreambe.dto.auth.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboundAuthRequest {
    String token;
    String email;
    String fullName;
    String avatarUrl;
}
