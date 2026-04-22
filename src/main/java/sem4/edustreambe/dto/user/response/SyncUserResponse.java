package sem4.edustreambe.dto.user.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncUserResponse {
    UserResponse user;
    boolean isNewUser;
}
