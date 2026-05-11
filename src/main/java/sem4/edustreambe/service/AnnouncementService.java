package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.dto.announcement.request.AnnouncementRequest;
import sem4.edustreambe.dto.announcement.response.AnnouncementResponse;
import sem4.edustreambe.entity.*;
import sem4.edustreambe.enums.NotificationType;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnnouncementService {

    AnnouncementRepository announcementRepository;
    CourseRepository courseRepository;
    UserRepository userRepository;
    EnrollmentRepository enrollmentRepository;
    TutorProfileRepository tutorProfileRepository;
    NotificationService notificationService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public AnnouncementResponse createAnnouncement(String courseId, AnnouncementRequest request) {
        User author = getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

        // Kiểm tra quyền: Chỉ Tutor của khóa học mới được đăng thông báo
        TutorProfile tutorProfile = tutorProfileRepository.findByUserId(author.getId()).orElse(null);
        boolean isInstructor = tutorProfile != null 
                && course.getTutorProfile() != null
                && course.getTutorProfile().getId().equals(tutorProfile.getId());

        if (!isInstructor && !author.getRole().getName().equals("ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Announcement announcement = Announcement.builder()
                .course(course)
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Announcement saved = announcementRepository.save(announcement);

        // GỬI THÔNG BÁO CHO TẤT CẢ HỌC VIÊN ENROLLED
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        for (Enrollment enrollment : enrollments) {
            try {
                notificationService.sendNotification(
                    enrollment.getUser(),
                    "Thông báo mới từ khóa học: " + course.getTitle(),
                    request.getTitle(),
                    NotificationType.ANNOUNCEMENT,
                    "/learning/" + courseId + "?tab=announcements"
                );
            } catch (Exception e) {
                // Log and continue to next user
                System.err.println("Failed to notify user " + enrollment.getUser().getUsername());
            }
        }

        return mapToResponse(saved);
    }

    public List<AnnouncementResponse> getCourseAnnouncements(String courseId) {
        // Kiểm tra quyền: Phải là Tutor của khóa hoặc học viên đã mua mới được xem
        // (Tạm thời cho phép xem công khai nếu cần, nhưng tốt nhất là restrict)
        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .authorName(a.getAuthor().getFullName() != null ? a.getAuthor().getFullName() : a.getAuthor().getUsername())
                .authorAvatar(a.getAuthor().getAvatarUrl())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
