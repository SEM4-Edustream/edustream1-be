package sem4.edustreambe.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sem4.edustreambe.constant.PredefinedRole;
import sem4.edustreambe.entity.Role;
import sem4.edustreambe.entity.User;
import sem4.edustreambe.repository.RoleRepository;
import sem4.edustreambe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseSeeder implements ApplicationRunner {

    RoleRepository roleRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking predefined roles in the database...");
        
        seedRoleIfNotExists(PredefinedRole.ADMIN_ROLE, "Administrator role");
        seedRoleIfNotExists(PredefinedRole.STUDENT_ROLE, "Student role");
        seedRoleIfNotExists(PredefinedRole.TUTOR_ROLE, "Tutor role");

        seedAdminAccount();
        
        log.info("Database seeding completed.");
    }

    private void seedAdminAccount() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName(PredefinedRole.ADMIN_ROLE)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                    .username("admin")
                    .email("admin@edustream.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("EduStream Admin")
                    .role(adminRole)
                    .status(sem4.edustreambe.constant.UserStatus.ACTIVE)
                    .build();
            
            userRepository.save(admin);
            log.info("Created default admin account: admin / admin123");
        }
    }

    private void seedRoleIfNotExists(String roleName, String description) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created new role: {}", roleName);
        }
    }
}
