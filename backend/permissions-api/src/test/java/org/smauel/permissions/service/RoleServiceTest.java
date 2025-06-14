package org.smauel.permissions.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
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
import org.smauel.permissions.dto.RoleDto;
import org.smauel.permissions.dto.request.CreateRoleRequest;
import org.smauel.permissions.exception.PermissionNotFoundException;
import org.smauel.permissions.exception.RoleNotFoundException;
import org.smauel.permissions.mapper.RoleMapper;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.repository.PermissionRepository;
import org.smauel.permissions.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = RoleServiceTest.Config.class)
@DisplayName("RoleService Tests")
class RoleServiceTest implements WithAssertions {

    @Configuration
    @ComponentScan(basePackageClasses = RoleMapper.class)
    static class Config {}

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleMapper roleMapper;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository, permissionRepository, roleMapper);
    }

    @Nested
    @DisplayName("Create Role")
    class CreateRoleTests {

        @Test
        @DisplayName("should create a role without permissions")
        void shouldCreateRoleWithoutPermissions() {
            // Given
            var request = new CreateRoleRequest("ADMIN", "Administrator Role", Collections.emptySet());
            var roleToSave = Role.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .permissions(new HashSet<>())
                    .build();
            var savedRole = Role.builder()
                    .id(1L)
                    .name(request.getName())
                    .description(request.getDescription())
                    .permissions(new HashSet<>())
                    .build();
            when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

            // When
            RoleDto result = roleService.createRole(request);

            // Then
            assertThat(result.getName()).isEqualTo(request.getName());
            assertThat(result.getDescription()).isEqualTo(request.getDescription());
            assertThat(result.getPermissions()).isEmpty();
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("should create a role with permissions")
        void shouldCreateRoleWithPermissions() {
            // Given
            var p1 = Permission.builder().id(101L).name("perm.read").build();
            var p2 = Permission.builder().id(102L).name("perm.write").build();
            var request = new CreateRoleRequest("EDITOR", "Editor Role", Set.of(p1.getId(), p2.getId()));
            var savedRole = Role.builder()
                    .id(1L)
                    .name(request.getName())
                    .permissions(Set.of(p1, p2))
                    .build();

            when(permissionRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
            when(permissionRepository.findById(p2.getId())).thenReturn(Optional.of(p2));
            when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

            // When
            RoleDto result = roleService.createRole(request);

            // Then
            assertThat(result.getName()).isEqualTo(request.getName());
            assertThat(result.getPermissions()).hasSize(2);
            verify(roleRepository).save(any(Role.class));
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException for invalid permission ID")
        void shouldThrowWhenPermissionNotFound() {
            // Given
            var request = new CreateRoleRequest("TEST_ROLE", "Test Role", Set.of(999L));
            when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> roleService.createRole(request))
                    .withMessage("Permission not found with id: 999");
            verify(roleRepository, never()).save(any(Role.class));
        }
    }

    @Nested
    @DisplayName("Get Role By ID")
    class GetRoleByIdTests {

        @Test
        @DisplayName("should return role DTO when found by ID")
        void shouldReturnRoleWhenFoundById() {
            // Given
            long id = 1L;
            var role = Role.builder().id(id).name("ADMIN").build();
            when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.of(role));

            // When
            RoleDto result = roleService.getRoleById(id);

            // Then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getName()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("should throw RoleNotFoundException when not found by ID")
        void shouldThrowRoleNotFoundExceptionWhenNotFoundById() {
            // Given
            long id = 1L;
            when(roleRepository.findByIdWithPermissions(id)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(RoleNotFoundException.class)
                    .isThrownBy(() -> roleService.getRoleById(id))
                    .withMessage("Role not found with id: 1");
        }
    }

    @Nested
    @DisplayName("Get Role By Name")
    class GetRoleByNameTests {

        @Test
        @DisplayName("should return role DTO when found by name")
        void shouldReturnRoleWhenFoundByName() {
            // Given
            String name = "ADMIN";
            var role = Role.builder().id(1L).name(name).build();
            when(roleRepository.findByNameWithPermissions(name)).thenReturn(Optional.of(role));

            // When
            RoleDto result = roleService.getRoleByName(name);

            // Then
            assertThat(result.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("should throw RoleNotFoundException when not found by name")
        void shouldThrowRoleNotFoundExceptionWhenNotFoundByName() {
            // Given
            String name = "GUEST";
            when(roleRepository.findByNameWithPermissions(name)).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(RoleNotFoundException.class)
                    .isThrownBy(() -> roleService.getRoleByName(name))
                    .withMessage("Role not found: GUEST");
        }
    }

    @Nested
    @DisplayName("Get All Roles")
    class GetAllRolesTests {

        @Test
        @DisplayName("should return a list of all role DTOs")
        void shouldReturnAllRoles() {
            // Given
            var role1 = Role.builder().id(1L).name("ADMIN").build();
            var role2 = Role.builder().id(2L).name("USER").build();
            when(roleRepository.findAll()).thenReturn(List.of(role1, role2));

            // When
            List<RoleDto> result = roleService.getAllRoles();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("ADMIN");
            assertThat(result.get(1).getName()).isEqualTo("USER");
        }

        @Test
        @DisplayName("should return an empty list when no roles exist")
        void shouldReturnEmptyListWhenNoRolesExist() {
            // Given
            when(roleRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<RoleDto> result = roleService.getAllRoles();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Add Permission To Role")
    class AddPermissionToRoleTests {
        private Role role;
        private Permission permission;

        @BeforeEach
        void innerSetup() {
            role = Role.builder()
                    .id(1L)
                    .name("USER")
                    .permissions(new HashSet<>())
                    .build();
            permission = Permission.builder().id(101L).name("perm.read").build();
        }

        @Test
        @DisplayName("should add permission to role")
        void shouldAddPermissionToRole() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RoleDto result = roleService.addPermissionToRole(role.getId(), permission.getId());

            // Then
            assertThat(result.getPermissions()).hasSize(1);
            assertThat(role.getPermissions()).contains(permission);
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("should throw RoleNotFoundException")
        void shouldThrowWhenRoleNotFound() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(RoleNotFoundException.class)
                    .isThrownBy(() -> roleService.addPermissionToRole(role.getId(), permission.getId()));
            verify(roleRepository, never()).save(any(Role.class));
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException")
        void shouldThrowWhenPermissionNotFound() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permission.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> roleService.addPermissionToRole(role.getId(), permission.getId()));
            verify(roleRepository, never()).save(any(Role.class));
        }
    }

    @Nested
    @DisplayName("Remove Permission From Role")
    class RemovePermissionFromRoleTests {
        private Role role;
        private Permission permission;

        @BeforeEach
        void innerSetup() {
            permission = Permission.builder().id(101L).name("perm.read").build();
            role = Role.builder()
                    .id(1L)
                    .name("USER")
                    .permissions(new HashSet<>(Set.of(permission)))
                    .build();
        }

        @Test
        @DisplayName("should remove permission from role")
        void shouldRemovePermissionFromRole() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
            assertThat(role.getPermissions()).contains(permission); // Pre-condition

            // When
            RoleDto result = roleService.removePermissionFromRole(role.getId(), permission.getId());

            // Then
            assertThat(result.getPermissions()).isEmpty();
            assertThat(role.getPermissions()).doesNotContain(permission);
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("should throw RoleNotFoundException")
        void shouldThrowWhenRoleNotFound() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(RoleNotFoundException.class)
                    .isThrownBy(() -> roleService.removePermissionFromRole(role.getId(), permission.getId()));
            verify(roleRepository, never()).save(any(Role.class));
        }

        @Test
        @DisplayName("should throw PermissionNotFoundException")
        void shouldThrowWhenPermissionNotFound() {
            // Given
            when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
            when(permissionRepository.findById(permission.getId())).thenReturn(Optional.empty());

            // When & Then
            assertThatExceptionOfType(PermissionNotFoundException.class)
                    .isThrownBy(() -> roleService.removePermissionFromRole(role.getId(), permission.getId()));
            verify(roleRepository, never()).save(any(Role.class));
        }
    }
}
