package sem4.edustreambe.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sem4.edustreambe.entity.Notification;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.enums.NotificationType;
import sem4.edustreambe.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationDispatchService {

    NotificationRepository notificationRepository;
    NotificationService notificationService;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Notification dispatch(User user, String title, String message, NotificationType type, String referenceUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceUrl(referenceUrl)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification persisted for user {} with id {}", user.getUsername(), saved.getId());

        notificationService.sendRealtimeNotification(
                user,
                title,
                message,
                type,
                referenceUrl,
                saved.getId(),
                saved.getCreatedAt()
        );

        return saved;
    }
}
