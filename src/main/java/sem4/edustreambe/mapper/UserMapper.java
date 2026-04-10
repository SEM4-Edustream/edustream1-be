package sem4.edustreambe.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sem4.edustreambe.dto.user.request.UserCreationRequest;
import sem4.edustreambe.dto.user.request.UserUpdateRequest;
import sem4.edustreambe.dto.user.response.UserResponse;
import sem4.edustreambe.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Sẽ encode thủ công trong Service
    @Mapping(target = "role", ignore = true)     // Sẽ gán thủ công trong Service
    @Mapping(target = "status", ignore = true)   // Default ACTIVE được set bởi @Builder.Default
    @Mapping(target = "dob", ignore = true)      // Không có trong request đăng ký
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true) // Không cho sửa username
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "email", ignore = true)    // Email không được sửa qua flow này
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "roleName", source = "role.name")
    UserResponse toUserResponse(User user);

}