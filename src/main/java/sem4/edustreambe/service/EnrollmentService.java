package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.booking.response.EnrollmentResponse;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.mapper.BookingMapper;
import sem4.edustreambe.repository.EnrollmentRepository;
import sem4.edustreambe.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnrollmentService {

    UserRepository userRepository;
    EnrollmentRepository enrollmentRepository;
    BookingMapper bookingMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public List<EnrollmentResponse> getMyEnrollments() {
        User student = getCurrentUser();
        return enrollmentRepository.findByUserId(student.getId()).stream()
                .map(bookingMapper::toEnrollmentResponse)
                .toList();
    }
}
