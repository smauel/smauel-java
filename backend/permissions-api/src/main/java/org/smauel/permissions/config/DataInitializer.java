package org.smauel.permissions.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {
            if (permissionRepository.count() > 0) {
                log.info("Database already populated. Skipping initialization.");
                return;
            }

            log.info("Initializing permissions and roles...");

            // Create system permissions
            Permission viewUsers =
                    createPermission("VIEW_USERS", "View all users", PermissionType.RESOURCE, "user", Action.READ);
            Permission createUser =
                    createPermission("CREATE_USER", "Create new users", PermissionType.RESOURCE, "user", Action.CREATE);
            Permission updateUser = createPermission(
                    "UPDATE_USER", "Update existing users", PermissionType.RESOURCE, "user", Action.UPDATE);
            Permission deleteUser =
                    createPermission("DELETE_USER", "Delete users", PermissionType.RESOURCE, "user", Action.DELETE);

            // Create admin permission
            Permission adminAccess =
                    createPermission("ADMIN_ACCESS", "Full administrative access", PermissionType.SYSTEM, null, null);

            // Create roles
            Role userRole = Role.builder()
                    .name("USER")
                    .description("Regular user with limited access")
                    .permissions(new HashSet<>(List.of(viewUsers)))
                    .build();

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Administrator with full access")
                    .permissions(
                            new HashSet<>(Arrays.asList(viewUsers, createUser, updateUser, deleteUser, adminAccess)))
                    .build();

            Role moderatorRole = Role.builder()
                    .name("MODERATOR")
                    .description("Moderator with user management capabilities")
                    .permissions(new HashSet<>(Arrays.asList(viewUsers, updateUser)))
                    .build();

            roleRepository.saveAll(List.of(userRole, adminRole, moderatorRole));

            log.info("Data initialization completed.");
        };
    }

    private Permission createPermission(
            String name, String description, PermissionType type, String resource, Action action) {
        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .type(type)
                .resource(resource)
                .action(action)
                .build();

        return permissionRepository.save(permission);
    }
}
