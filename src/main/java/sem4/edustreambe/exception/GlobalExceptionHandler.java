package sem4.edustreambe.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sem4.edustreambe.dto.common.ApiResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(Exception exception) {
        log.error("Unhandled Exception: ", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        String message = messageSource.getMessage(errorCode.getMessageKey(), null, LocaleContextHolder.getLocale());
        
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message + " | DETAIL: " + exception.getMessage())
                        .build());
    }

    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(org.springframework.security.access.AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        String message = messageSource.getMessage(errorCode.getMessageKey(), null, LocaleContextHolder.getLocale());
        
        log.warn("Access Denied: {}", exception.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handlingValidationException(MethodArgumentNotValidException exception) {
        String enumKey = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("UNCATEGORIZED_EXCEPTION");

        ErrorCode errorCode;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown validation error key: {}", enumKey);
            errorCode = ErrorCode.INVALID_KEY;
        }

        String message = messageSource.getMessage(errorCode.getMessageKey(), null, LocaleContextHolder.getLocale());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        String message = messageSource.getMessage(errorCode.getMessageKey(), null, LocaleContextHolder.getLocale());
        
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build());
    }
}