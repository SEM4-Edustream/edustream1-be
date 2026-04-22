package sem4.edustreambe.dto.user.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncUserResponse {
    UserResponse userResponse;
    User userEntity; // Needed to generate internal JWT
    boolean isNewUser;
}
