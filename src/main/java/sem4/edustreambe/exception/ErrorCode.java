package sem4.edustreambe.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // SYSTEM & COMMON (99xx)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "Invalid message key", HttpStatus.BAD_REQUEST),

    // AUTH & SECURITY (10xx)
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You do not have permission", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1003, "Role does not exist", HttpStatus.NOT_FOUND),

    // USER & PROFILE (20xx)
    USER_EXISTED(2001, "User already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2002, "Email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(2003, "User not found", HttpStatus.NOT_FOUND),

    // VALIDATION (80xx)
    INVALID_PASSWORD(8001, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(8002, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(8003, "Invalid email format", HttpStatus.BAD_REQUEST)
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}