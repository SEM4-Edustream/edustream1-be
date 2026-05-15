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
    UNCATEGORIZED_EXCEPTION(9999, "uncategorized.exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "invalid.key", HttpStatus.BAD_REQUEST),

    // AUTH & SECURITY (10xx)
    UNAUTHENTICATED(1001, "unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "unauthorized", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXISTED(1003, "role.not_existed", HttpStatus.NOT_FOUND),

    // USER & PROFILE (20xx)
    USER_EXISTED(2001, "user.existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(2002, "email.existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(2003, "user.not_existed", HttpStatus.NOT_FOUND),

    // TUTOR PROFILE (30xx)
    TUTOR_PROFILE_EXISTED(3001, "tutor_profile.existed", HttpStatus.BAD_REQUEST),
    TUTOR_PROFILE_NOT_FOUND(3002, "tutor_profile.not_found", HttpStatus.NOT_FOUND),
    INVALID_PROFILE_STATUS(3003, "invalid.profile.status", HttpStatus.BAD_REQUEST),
    TUTOR_DOCUMENT_NOT_FOUND(3004, "tutor_document.not_found", HttpStatus.NOT_FOUND),
    PROFILE_MUST_HAVE_DOCUMENT(3005, "profile.must_have_document", HttpStatus.BAD_REQUEST),

    // COURSE (40xx)
    COURSE_NOT_FOUND(4001, "course.not_found", HttpStatus.NOT_FOUND),
    MODULE_NOT_FOUND(4002, "module.not_found", HttpStatus.NOT_FOUND),
    LESSON_NOT_FOUND(4003, "lesson.not_found", HttpStatus.NOT_FOUND),
    INVALID_COURSE_STATUS(4004, "invalid.course.status", HttpStatus.BAD_REQUEST),
    COURSE_REVIEW_NOT_ALLOWED(4006, "course.review.not_allowed", HttpStatus.BAD_REQUEST),
    COURSE_OWNERSHIP_DENIED(4005, "course.ownership_denied", HttpStatus.FORBIDDEN),

    // BOOKING & ENROLLMENT (50xx)
    BOOKING_NOT_FOUND(5001, "booking.not_found", HttpStatus.NOT_FOUND),
    ALREADY_ENROLLED(5002, "already_enrolled", HttpStatus.BAD_REQUEST),
    COURSE_NOT_PUBLISHED(5003, "course.not_published", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_EXISTS(5004, "booking.already_exists", HttpStatus.BAD_REQUEST),
    ENROLLMENT_NOT_FOUND(5005, "enrollment.not_found", HttpStatus.FORBIDDEN),
    LESSON_ALREADY_COMPLETED(5006, "lesson.already_completed", HttpStatus.BAD_REQUEST),

    // PAYMENT & TRANSACTION (60xx)
    PAYMENT_FAILED(6001, "payment.failed", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_NOT_FOUND(6002, "transaction.not_found", HttpStatus.NOT_FOUND),
    INVALID_WEBHOOK_DATA(6003, "invalid.webhook_data", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED(6004, "payment.already_processed", HttpStatus.BAD_REQUEST),

    // VALIDATION (80xx)
    INVALID_PASSWORD(8001, "invalid.password", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(8002, "username.invalid", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(8003, "invalid.email", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_WEAK(8004, "password.too_weak", HttpStatus.BAD_REQUEST),
    FIELD_REQUIRED(8005, "field.required", HttpStatus.BAD_REQUEST),
    USERNAME_TOO_SHORT(8006, "username.too_short", HttpStatus.BAD_REQUEST),
    
    // REVIEWS (70xx)
    REVIEW_ALREADY_EXISTS(7001, "review.already_exists", HttpStatus.BAD_REQUEST),
    MUST_ENROLL_TO_REVIEW(7002, "must_enroll_to_review", HttpStatus.FORBIDDEN),
    INVALID_RATING(7003, "invalid.rating", HttpStatus.BAD_REQUEST),
    INVALID_COMMENT(7004, "invalid.comment", HttpStatus.BAD_REQUEST),

    // CART & WISHLIST (90xx)
    ALREADY_IN_CART(9001, "already_in_cart", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(9002, "cart_item.not_found", HttpStatus.NOT_FOUND),
    CART_EMPTY(9003, "cart.empty", HttpStatus.BAD_REQUEST),
    ALREADY_IN_WISHLIST(9004, "already_in_wishlist", HttpStatus.BAD_REQUEST),
    WISHLIST_ITEM_NOT_FOUND(9005, "wishlist_item.not_found", HttpStatus.NOT_FOUND),
    
    // NOTES (95xx)
    NOTE_NOT_FOUND(9501, "note.not_found", HttpStatus.NOT_FOUND),

    // Q&A (85xx)
    QUESTION_NOT_FOUND(8501, "question.not_found", HttpStatus.NOT_FOUND),
    ANSWER_NOT_FOUND(8502, "answer.not_found", HttpStatus.NOT_FOUND),
    QUESTION_OWNERSHIP_DENIED(8503, "question.ownership_denied", HttpStatus.FORBIDDEN),
    MUST_ENROLL_TO_ASK(8504, "must_enroll_to_ask", HttpStatus.FORBIDDEN)
    ;

    int code;
    String messageKey;
    HttpStatusCode statusCode;
}