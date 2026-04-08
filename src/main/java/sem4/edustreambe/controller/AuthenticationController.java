package sem4.edustreambe.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.auth.request.AuthenticationRequest;
import sem4.edustreambe.dto.auth.response.AuthenticationResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.user.request.UserCreationRequest;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.service.AuthenticationService;
import sem4.edustreambe.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    UserService userService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Received login request for user: {}", request.getUsername());

        var result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<User> createUser(@RequestBody UserCreationRequest request) {
        log.info("Received registration request: {}", request.getUsername());
        var result = userService.createUser(request);

        return ApiResponse.<User>builder()
                .result(result)
                .build();
    }
}