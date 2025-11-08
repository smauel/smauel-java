package org.smauel.permissions.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.UserRoleAssignment;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(showSql = false)
@DisplayName("UserRoleAssignment Repository Acceptance Tests")
class UserRoleAssignmentRepositoryAT {

    @Autowired
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private final Long userWithAdminRole = 111L;
    private final Long userWithUserRole = 222L;
    private final Long userWithExpiredRole = 333L;
    private final Long userWithMultipleRoles = 444L;
    private Role adminRole, userRole;
    private UserRoleAssignment assignment1, expiredAssignment;

    @BeforeEach
    void setUp() {
        // Clear all repositories to ensure a clean state
        userRoleAssignmentRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create and save permissions
        Permission createUserPerm = permissionRepository.save(Permission.builder()
                .name("user:create")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.CREATE)
                .build());
        Permission readUserPerm = permissionRepository.save(Permission.builder()
                .name("user:read")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.READ)
                .build());

        // Create and save roles
        adminRole = roleRepository.save(Role.builder()
                .name("ADMIN")
                .permissions(Set.of(createUserPerm, readUserPerm))
                .build());
        userRole = roleRepository.save(
                Role.builder().name("USER").permissions(Set.of(readUserPerm)).build());

        // Create and save role assignments
        Long assignedById = 999L;
        assignment1 = userRoleAssignmentRepository.save(UserRoleAssignment.builder()
                .userId(userWithAdminRole)
                .role(adminRole)
                .assignedBy(assignedById)
                .expiresAt(null)
                .build());

        userRoleAssignmentRepository.save(UserRoleAssignment.builder()
                .userId(userWithUserRole)
                .role(userRole)
                .assignedBy(assignedById)
                .expiresAt(null)
                .build());

        userRoleAssignmentRepository.save(UserRoleAssignment.builder()
                .userId(userWithMultipleRoles)
                .role(userRole)
                .assignedBy(assignedById)
                .expiresAt(null)
                .build());
        userRoleAssignmentRepository.save(UserRoleAssignment.builder()
                .userId(userWithMultipleRoles)
                .role(adminRole)
                .assignedBy(assignedById)
                .expiresAt(null)
                .build());

        userRoleAssignmentRepository.save(UserRoleAssignment.builder()
                .userId(userWithExpiredRole)
                .role(userRole)
                .assignedBy(assignedById)
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                .build());
    }

    @Test
    @DisplayName("findByUserId should return all assignments for a user")
    void whenFindByUserId_thenReturnAllAssignments() {
        List<UserRoleAssignment> assignments = userRoleAssignmentRepository.findByUserId(userWithMultipleRoles);
        assertThat(assignments).hasSize(2);
        assertThat(assignments)
                .extracting(UserRoleAssignment::getRole)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("findActiveRoleAssignmentsByUserId should return only active assignments")
    void whenFindActiveRoleAssignmentsByUserId_thenReturnOnlyActive() {
        List<UserRoleAssignment> activeAssignments =
                userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(userWithAdminRole, LocalDateTime.now());
        assertThat(activeAssignments).hasSize(1);
        assertThat(activeAssignments.getFirst().getRole().getName()).isEqualTo("ADMIN");
        assertThat(activeAssignments.getFirst().getExpiresAt()).isNull();
    }

    @Test
    @DisplayName("existsActiveByUserIdAndRoleId should return true for active assignment")
    void whenExistsActiveByUserIdAndRoleId_withActiveAssignment_thenReturnTrue() {
        boolean exists = userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                userWithAdminRole, adminRole.getId(), LocalDateTime.now());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsActiveByUserIdAndRoleId should return false for expired assignment")
    void whenExistsActiveByUserIdAndRoleId_withExpiredAssignment_thenReturnFalse() {
        boolean exists = userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                userWithExpiredRole, userRole.getId(), LocalDateTime.now());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("hasPermissionThroughRoles should return true for user with active permission")
    void whenHasPermissionThroughRoles_withValidPermission_thenReturnTrue() {
        boolean hasPermission =
                userRoleAssignmentRepository.hasPermissionThroughRoles(userWithAdminRole, "user:create");
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("hasPermissionThroughRoles should return false for user without permission")
    void whenHasPermissionThroughRoles_withoutPermission_thenReturnFalse() {
        boolean hasPermission = userRoleAssignmentRepository.hasPermissionThroughRoles(userWithUserRole, "user:create");
        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("hasPermissionThroughRoles should return false for user with expired role permission")
    void whenHasPermissionThroughRoles_withExpiredRolePermission_thenReturnFalse() {
        assertThat(userRoleAssignmentRepository.hasPermissionThroughRoles(userWithExpiredRole, "user:read"))
                .isFalse();
    }

    @Test
    @Transactional
    @DisplayName("deleteByUserIdAndRole_Id should remove the specific assignment")
    void whenDeleteByUserIdAndRoleId_thenAssignmentIsRemoved() {
        long countBeforeDelete = userRoleAssignmentRepository.count();

        userRoleAssignmentRepository.deleteByUserIdAndRole_Id(userWithMultipleRoles, adminRole.getId());
        userRoleAssignmentRepository.flush();

        assertThat(userRoleAssignmentRepository.count()).isEqualTo(countBeforeDelete - 1);

        assertThat(userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                        userWithMultipleRoles, adminRole.getId(), LocalDateTime.now()))
                .isFalse();

        assertThat(userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                        userWithMultipleRoles, userRole.getId(), LocalDateTime.now()))
                .isTrue();
    }

    @Test
    @Transactional
    @DisplayName("deleteByUserId should remove all assignments for that user")
    void whenDeleteByUserId_thenAllUserAssignmentsAreRemoved() {
        long user2Assignments =
                userRoleAssignmentRepository.findByUserId(userWithUserRole).size();
        assertThat(user2Assignments).isEqualTo(1);

        userRoleAssignmentRepository.deleteByUserId(userWithAdminRole);
        userRoleAssignmentRepository.flush();

        assertThat(userRoleAssignmentRepository.findByUserId(userWithAdminRole)).isEmpty();
        assertThat(userRoleAssignmentRepository.findByUserId(userWithUserRole))
                .hasSize((int) user2Assignments); // Ensure user2 assignments are untouched
    }
}
