package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sem4.edustreambe.dto.user.request.UserCreationRequest;
import sem4.edustreambe.dto.user.response.UserResponse;
import sem4.edustreambe.entity.User;

@Mapper(componentModel = "spring") // Để Inject vào Service bằng @RequiredArgsConstructor
public interface UserMapper {

    // Map từ Request lúc Register sang Entity
    User toUser(UserCreationRequest request);

    // Map từ Entity sang Response (Tự động bỏ qua password vì DTO không có)
    UserResponse toUserResponse(User user);
}