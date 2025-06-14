package org.smauel.permissions.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.smauel.permissions.dto.PermissionDto;
import org.smauel.permissions.dto.UserRoleAssignmentDto;
import org.smauel.permissions.dto.request.GrantRoleRequest;
import org.smauel.permissions.exception.RoleAlreadyAssignedException;
import org.smauel.permissions.exception.RoleNotFoundException;
import org.smauel.permissions.mapper.PermissionMapper;
import org.smauel.permissions.mapper.PermissionMapperImpl;
import org.smauel.permissions.mapper.RoleMapperImpl;
import org.smauel.permissions.mapper.UserRoleAssignmentMapper;
import org.smauel.permissions.mapper.UserRoleAssignmentMapperImpl;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.UserRoleAssignment;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.smauel.permissions.repository.UserRoleAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {UserRoleAssignmentMapperImpl.class, RoleMapperImpl.class, PermissionMapperImpl.class})
@DisplayName("UserRoleService Tests")
class UserRoleServiceTest implements WithAssertions {

    @Mock
    private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleAssignmentMapper userRoleAssignmentMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleService(
                userRoleAssignmentRepository,
                roleRepository,
                permissionRepository,
                userRoleAssignmentMapper,
                permissionMapper);
    }

    @Nested
    @DisplayName("Assign Role to User")
    class AssignRoleToUserTests {

        @Test
        @DisplayName("should assign role to user successfully")
        void shouldAssignRoleToUser() {
            // Given
            Long userId = 1L;
            Long grantorUserId = 2L;
            var request = new GrantRoleRequest("ADMIN", grantorUserId, null);
            var role = Role.builder().id(10L).name("ADMIN").build();
            var assignment =
                    UserRoleAssignment.builder().userId(userId).role(role).build();

            when(roleRepository.findByNameWithPermissions(request.getRoleName()))
                    .thenReturn(Optional.of(role));
            when(userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                            eq(userId), eq(role.getId()), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(userRoleAssignmentRepository.save(any(UserRoleAssignment.class)))
                    .thenReturn(assignment);

            // When
            UserRoleAssignmentDto result = userRoleService.assignRoleToUser(userId, request);

            // Then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getRole().getName()).isEqualTo("ADMIN");
            verify(userRoleAssignmentRepository).save(any(UserRoleAssignment.class));
        }

        @Test
        @DisplayName("should throw RoleNotFoundException when role does not exist")
        void shouldThrowRoleNotFoundException() {
            // Given
            Long userId = 1L;
            Long grantorUserId = 2L;
            var request = new GrantRoleRequest("FAKE_ROLE", grantorUserId, null);
            when(roleRepository.findByNameWithPermissions("FAKE_ROLE")).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(RoleNotFoundException.class)
                    .isThrownBy(() -> userRoleService.assignRoleToUser(userId, request))
                    .withMessage("Role not found: FAKE_ROLE");
        }

        @Test
        @DisplayName("should throw RoleAlreadyAssignedException when role is already assigned")
        void shouldThrowRoleAlreadyAssignedException() {
            // Given
            Long userId = 1L;
            Long grantorUserId = 2L;
            var request = new GrantRoleRequest("ADMIN", grantorUserId, null);
            var role = Role.builder().id(10L).name("ADMIN").build();

            when(roleRepository.findByNameWithPermissions(request.getRoleName()))
                    .thenReturn(Optional.of(role));
            when(userRoleAssignmentRepository.existsActiveByUserIdAndRoleId(
                            eq(userId), eq(role.getId()), any(LocalDateTime.class)))
                    .thenReturn(true);

            // When & Then
            assertThatExceptionOfType(RoleAlreadyAssignedException.class)
                    .isThrownBy(() -> userRoleService.assignRoleToUser(userId, request))
                    .withMessage("Role 'ADMIN' is already assigned to user with ID 1");
        }
    }

    @Nested
    @DisplayName("Check User Permission")
    class HasPermissionTests {

        @Test
        @DisplayName("should return true if user has permission")
        void shouldReturnTrueForPermission() {
            // Given
            Long userId = 1L;
            String permissionName = "perm.read";
            when(userRoleAssignmentRepository.hasPermissionThroughRoles(userId, permissionName))
                    .thenReturn(true);

            // When
            boolean result = userRoleService.hasPermission(userId, permissionName);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false if user does not have permission")
        void shouldReturnFalseForNoPermission() {
            // Given
            Long userId = 1L;
            String permissionName = "perm.delete";
            when(userRoleAssignmentRepository.hasPermissionThroughRoles(userId, permissionName))
                    .thenReturn(false);

            // When
            boolean result = userRoleService.hasPermission(userId, permissionName);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Get User Permissions")
    class GetUserPermissionsTests {

        @Test
        @DisplayName("should return a list of unique permissions")
        void shouldReturnUniquePermissions() {
            // Given
            Long userId = 1L;
            var p1 = Permission.builder().id(101L).name("perm.read").build();
            var p2 = Permission.builder().id(102L).name("perm.write").build();
            var role1 =
                    Role.builder().id(1L).name("READER").permissions(Set.of(p1)).build();
            var role2 = Role.builder()
                    .id(2L)
                    .name("WRITER")
                    .permissions(Set.of(p1, p2))
                    .build();

            var assignment1 =
                    UserRoleAssignment.builder().userId(userId).role(role1).build();
            var assignment2 =
                    UserRoleAssignment.builder().userId(userId).role(role2).build();

            when(userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(List.of(assignment1, assignment2));
            when(roleRepository.findByIdWithPermissions(1L)).thenReturn(Optional.of(role1));
            when(roleRepository.findByIdWithPermissions(2L)).thenReturn(Optional.of(role2));

            // When
            List<PermissionDto> permissions = userRoleService.getUserPermissions(userId);

            // Then
            assertThat(permissions).hasSize(2);
            assertThat(permissions)
                    .extracting(PermissionDto::getName)
                    .containsExactlyInAnyOrder("perm.read", "perm.write");
        }

        @Test
        @DisplayName("should return an empty list if user has no roles")
        void shouldReturnEmptyListForNoRoles() {
            // Given
            Long userId = 1L;
            when(userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<PermissionDto> permissions = userRoleService.getUserPermissions(userId);

            // Then
            assertThat(permissions).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get User Roles")
    class GetUserRolesTests {

        @Test
        @DisplayName("should return a list of active roles for a user")
        void shouldReturnUserRoles() {
            // Given
            Long userId = 1L;
            var role = Role.builder().id(10L).name("ADMIN").build();
            var assignment =
                    UserRoleAssignment.builder().userId(userId).role(role).build();
            when(userRoleAssignmentRepository.findActiveRoleAssignmentsByUserId(eq(userId), any(LocalDateTime.class)))
                    .thenReturn(List.of(assignment));

            // When
            List<UserRoleAssignmentDto> roles = userRoleService.getUserRoles(userId);

            // Then
            assertThat(roles).hasSize(1);
            assertThat(roles.get(0).getRole().getName()).isEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("Revoke Roles")
    class RevokeRolesTests {

        @Test
        @DisplayName("should revoke a specific role from a user")
        void shouldRevokeRole() {
            // Given
            Long userId = 1L;
            Long roleId = 10L;

            // When
            userRoleService.revokeRole(userId, roleId);

            // Then
            verify(userRoleAssignmentRepository).deleteByUserIdAndRole_Id(userId, roleId);
        }

        @Test
        @DisplayName("should revoke all roles from a user")
        void shouldRevokeAllRoles() {
            // Given
            Long userId = 1L;

            // When
            userRoleService.revokeAllRoles(userId);

            // Then
            verify(userRoleAssignmentRepository).deleteByUserId(userId);
        }
    }
}
