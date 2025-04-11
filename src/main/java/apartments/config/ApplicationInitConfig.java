package apartments.config;

import apartments.constant.PredefinedRole;
import apartments.constant.PredefinedStatus;
import lombok.AccessLevel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import apartments.dao.RoleRepository;
import apartments.dao.UserRepository;
import apartments.entity.Role;
import apartments.entity.User;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RoleRepository roleRepository;

    @NonFinal
    static final String ADMIN_USER_EMAIL = "admin@gmail.com";
    @NonFinal
    static final String ADMIN_USER_NAME = "admin";
    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner() {
        log.info("Initializing application.....");
        return args -> {
            // Tạo các role nếu chưa tồn tại (sử dụng phương thức helper)
            Role userRole = getOrCreateRole(PredefinedRole.USER_ROLE);
            Role adminRole = getOrCreateRole(PredefinedRole.ADMIN_ROLE);
            Role posterRole = getOrCreateRole(PredefinedRole.POSTER_ROLE);

            // Tạo admin user nếu chưa tồn tại
            if (userRepository.findByEmail(ADMIN_USER_EMAIL).isEmpty()) {
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                adminRoles.add(userRole);
                adminRoles.add(posterRole);

                User adminUser = User.builder()
                        .email(ADMIN_USER_EMAIL)
                        .userName(ADMIN_USER_NAME)
                        .passWord(passwordEncoder.encode(ADMIN_PASSWORD))
                        .roles(adminRoles)
                        .enabled(true)
//                        .status(PredefinedStatus.USER_ACTIVE)
                        .build();

                userRepository.save(adminUser);
                log.warn("Admin user created with default password: admin, please change it");
            }
            log.info("Application initialization completed");
        };
    }

    // Thêm phương thức helper này vào class
    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    log.info("Creating new role: {}", roleName);
                    return roleRepository.save(Role.builder().name(roleName).build());
                });
    }
}