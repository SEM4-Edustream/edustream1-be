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

    // TUTOR PROFILE (30xx)
    TUTOR_PROFILE_EXISTED(3001, "Tutor profile already exists for this user", HttpStatus.BAD_REQUEST),
    TUTOR_PROFILE_NOT_FOUND(3002, "Tutor profile not found", HttpStatus.NOT_FOUND),
    INVALID_PROFILE_STATUS(3003, "Action not allowed for current profile status", HttpStatus.BAD_REQUEST),
    TUTOR_DOCUMENT_NOT_FOUND(3004, "Tutor document not found", HttpStatus.NOT_FOUND),
    PROFILE_MUST_HAVE_DOCUMENT(3005, "Profile must have at least one document before submitting", HttpStatus.BAD_REQUEST),

    // COURSE (40xx)
    COURSE_NOT_FOUND(4001, "Course not found", HttpStatus.NOT_FOUND),
    MODULE_NOT_FOUND(4002, "Module not found", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(4003, "Lesson not found", HttpStatus.NOT_FOUND),
    INVALID_COURSE_STATUS(4004, "Action not allowed for current course status", HttpStatus.BAD_REQUEST),
    COURSE_OWNERSHIP_DENIED(4005, "You do not own this course", HttpStatus.FORBIDDEN),

    // BOOKING & ENROLLMENT (50xx)
    BOOKING_NOT_FOUND(5001, "Booking not found", HttpStatus.NOT_FOUND),
    ALREADY_ENROLLED(5002, "You are already enrolled in this course", HttpStatus.BAD_REQUEST),
    COURSE_NOT_PUBLISHED(5003, "Cannot book a course that is not published", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_EXISTS(5004, "You already have a pending booking for this course", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_FOUND(5005, "You are not enrolled in this course", HttpStatus.FORBIDDEN),
    LESSON_ALREADY_COMPLETED(5006, "You have already completed this lesson", HttpStatus.BAD_REQUEST),

    // PAYMENT & TRANSACTION (60xx)
    PAYMENT_FAILED(6001, "Error creating payment link", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_NOT_FOUND(6002, "Transaction not found", HttpStatus.NOT_FOUND),
    INVALID_WEBHOOK_DATA(6003, "Invalid webhook signature or data", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED(6004, "This payment is already processed", HttpStatus.BAD_REQUEST),

    // VALIDATION (80xx)
    INVALID_PASSWORD(8001, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(8002, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(8003, "Invalid email format", HttpStatus.BAD_REQUEST)
    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}