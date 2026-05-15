package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.entity.Notification;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.NotificationType;
import sem4.edustreambe.exception.AppException;
import sem4.edustreambe.exception.ErrorCode;
import sem4.edustreambe.repository.NotificationRepository;
import sem4.edustreambe.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // Gửi thông báo cho 1 người dùng
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void sendNotification(User user, String title, String message, NotificationType type, String referenceUrl) {
        try {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceUrl(referenceUrl)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);

            // Đẩy thông báo thời gian thực qua WebSocket
            sem4.edustreambe.dto.notification.NotificationResponse response = sem4.edustreambe.dto.notification.NotificationResponse.builder()
                    .id(notification.getId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .type(notification.getType())
                    .referenceUrl(notification.getReferenceUrl())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();

            messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                response
            );
        } catch (Exception e) {
            // Log but never throw — notification failure must NOT affect the caller's transaction
            org.slf4j.LoggerFactory.getLogger(getClass()).error("sendNotification failed for user {}: {}", 
                user.getUsername(), e.getMessage(), e);
        }
    }

    public Page<sem4.edustreambe.dto.notification.NotificationResponse> getMyNotifications(Pageable pageable) {
        User currentUser = getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(n -> sem4.edustreambe.dto.notification.NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .referenceUrl(n.getReferenceUrl())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        
        User currentUser = getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsReadByUserId(currentUser.getId());
    }

    public long countUnread() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUser.getId());
    }
}
