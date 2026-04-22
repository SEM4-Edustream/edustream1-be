package sem4.edustreambe.service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sem4.edustreambe.constant.PredefinedRole;
import sem4.edustreambe.dto.user.request.UserCreationRequest;
import sem4.edustreambe.dto.user.request.UserUpdateRequest;
import sem4.edustreambe.dto.user.response.SyncUserResponse;
import sem4.edustreambe.dto.user.response.UserResponse;
import sem4.edustreambe.entity.Role;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.UserMapper;
import sem4.edustreambe.repository.RoleRepository;
import sem4.edustreambe.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    FileService fileService;

    public UserResponse createUser(UserCreationRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Self-register must always be assigned the default STUDENT role.
        Role role = roleRepository.findByName(PredefinedRole.STUDENT_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRole(role);

        user = userRepository.save(user);
        log.info("User created successfully with id: {}", user.getId());

        return userMapper.toUserResponse(user);
    }

    public SyncUserResponse syncUserFromSocial(String email, String fullName, String avatarUrl) {
        log.info("Syncing social user: {}", email);

        final boolean[] isNewUser = {false};

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating new social user for email: {}", email);
            isNewUser[0] = true;
            
            // Create a unique username from email
            String username = email.split("@")[0];
            if (userRepository.existsByUsername(username)) {
                username = username + "_" + System.currentTimeMillis();
            }

            Role studentRole = roleRepository.findByName(PredefinedRole.STUDENT_ROLE)
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

            return User.builder()
                    .email(email)
                    .username(username)
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .password(passwordEncoder.encode("")) // Empty password for social users
                    .role(studentRole)
                    .status(sem4.edustreambe.constant.UserStatus.ACTIVE)
                    .build();
        });

        // Always update full name and avatar if provided
        if (fullName != null) user.setFullName(fullName);
        if (avatarUrl != null) user.setAvatarUrl(avatarUrl);

        User savedUser = userRepository.save(user);
        
        return SyncUserResponse.builder()
                .user(userMapper.toUserResponse(savedUser))
                .isNewUser(isNewUser[0])
                .build();
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse updateAvatar(sem4.edustreambe.dto.user.request.AvatarUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String oldAvatarUrl = user.getAvatarUrl();
        
        // Delete old avatar if it's different and belongs to our S3
        if (oldAvatarUrl != null && !oldAvatarUrl.equals(request.getAvatarUrl()) 
                && oldAvatarUrl.contains(".amazonaws.com")) {
            fileService.deleteFile(oldAvatarUrl);
        }

        user.setAvatarUrl(request.getAvatarUrl());
        return userMapper.toUserResponse(userRepository.save(user));
    }
}