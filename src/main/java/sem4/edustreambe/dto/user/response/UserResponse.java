package sem4.edustreambe.dto.user.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sem4.edustreambe.dto.role.response.RoleResponse;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String email;
    String fullName;
    LocalDate dob;
    Set<RoleResponse> roles;
}
