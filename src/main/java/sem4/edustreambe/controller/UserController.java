package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.user.request.AvatarUpdateRequest;
import sem4.edustreambe.dto.user.response.UserResponse;
import sem4.edustreambe.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/my-info")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('TUTOR')")
    public ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PatchMapping("/avatar")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN') or hasRole('TUTOR')")
    public ApiResponse<UserResponse> updateAvatar(@RequestBody AvatarUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateAvatar(request))
                .message("Avatar updated successfully")
                .build();
    }
}