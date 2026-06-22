package sem4.edustreambe.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sem4.edustreambe.annotation.LogAdminAction;
import sem4.edustreambe.entity.AuditLog;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.repository.AuditLogRepository;
import sem4.edustreambe.repository.UserRepository;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @AfterReturning(pointcut = "@annotation(logAdminAction)", returning = "result")
    public void logAction(JoinPoint joinPoint, LogAdminAction logAdminAction, Object result) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) return;

            String username = authentication.getName();
            User admin = userRepository.findByUsername(username).orElse(null);
            if (admin == null) return;

            String entityId = "UNKNOWN";
            
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameterNames.length; i++) {
                if ("id".equals(parameterNames[i]) || "userId".equals(parameterNames[i]) || "courseId".equals(parameterNames[i])) {
                    if (args[i] != null) {
                        entityId = args[i].toString();
                        break;
                    }
                }
            }

            String ipAddress = "UNKNOWN";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
            }

            AuditLog auditLog = AuditLog.builder()
                    .admin(admin)
                    .action(logAdminAction.action())
                    .entityType(logAdminAction.entityType())
                    .entityId(entityId)
                    .details("Method: " + signature.getName())
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved: Admin {} {} {} {}", admin.getUsername(), logAdminAction.action(), logAdminAction.entityType(), entityId);

        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
