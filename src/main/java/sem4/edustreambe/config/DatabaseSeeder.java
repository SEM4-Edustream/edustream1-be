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
import sem4.edustreambe.repository.RoleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseSeeder implements ApplicationRunner {

    RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking predefined roles in the database...");
        
        seedRoleIfNotExists(PredefinedRole.ADMIN_ROLE, "Administrator role");
        seedRoleIfNotExists(PredefinedRole.STUDENT_ROLE, "Student role");
        seedRoleIfNotExists(PredefinedRole.INSTRUCTOR_ROLE, "Instructor role");
        
        log.info("Database seeding completed.");
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
