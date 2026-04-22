package sem4.edustreambe.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sem4.edustreambe.dto.auth.request.AuthenticationRequest;
import sem4.edustreambe.dto.auth.request.IntrospectRequest;
import sem4.edustreambe.dto.auth.request.LogoutRequest;
import sem4.edustreambe.dto.auth.request.RefreshRequest;
import sem4.edustreambe.dto.auth.response.AuthenticationResponse;
import sem4.edustreambe.dto.auth.response.IntrospectResponse;
import sem4.edustreambe.dto.common.ApiResponse;
import sem4.edustreambe.dto.user.request.UserCreationRequest;
import sem4.edustreambe.dto.user.response.UserResponse;
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
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Received registration request: {}", request.getUsername());
        var result = userService.createUser(request);

        return ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/outbound/authentication")
    public ApiResponse<AuthenticationResponse> outboundAuthenticate(@RequestBody sem4.edustreambe.dto.auth.request.OutboundAuthRequest request) {
        log.info("Outbound authentication for email: {}", request.getEmail());
        
        // Sync user using data sent from the frontend (extracted from Supabase session)
        var syncResponse = userService.syncUserFromSocial(
                request.getEmail(), 
                request.getFullName(), 
                request.getAvatarUrl()
        );
        
        return ApiResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder()
                        .token(request.getToken())
                        .authenticated(true)
                        .isNewUser(syncResponse.isNewUser())
                        .build())
                .build();
    }
}